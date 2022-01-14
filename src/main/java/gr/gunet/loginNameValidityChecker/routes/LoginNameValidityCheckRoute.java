package gr.gunet.loginNameValidityChecker.routes;
import gr.gunet.loginNameValidityChecker.Conflict;
import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import gr.gunet.loginNameValidityChecker.LoginNameValidityChecker;
import gr.gunet.loginNameValidityChecker.RequestPerson;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import gr.gunet.loginNameValidityChecker.db.HRMSDBView;
import gr.gunet.loginNameValidityChecker.db.SISDBView;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import gr.gunet.loginNameValidityChecker.ldap.LdapManager;
import gr.gunet.loginNameValidityChecker.generator.UserNameGen;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;
import com.google.gson.Gson;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginNameValidityCheckRoute implements Route{
    private String userName;
    private String req;
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    boolean verbose= false;
    String disabledGracePeriod=null;

    public LoginNameValidityCheckRoute() {
    }
    @Override
    public Object handle(Request req, Response res) throws Exception{
        res.type("application/json");
        CustomJsonReader jsonReader = new CustomJsonReader(req.body());
        String CONN_FILE_DIR = "/etc/v_vd/conn/";
        boolean verbose=false;
        String disabledGracePeriod = null;

        RequestPerson reqPerson;
        try{
            reqPerson = new RequestPerson(jsonReader);
        }catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\n  \"error\":\""+e.getMessage()+"\"\n}\n";
        }

        Views= new DBConnectionPool(reqPerson.getInstitution());
        ldapDS= new LdapConnectionPool(reqPerson.getInstitution());
        LoginNameValidityChecker loginChecker = new LoginNameValidityChecker(Views, ldapDS);

        Collection<String> UIDPersons;
        try{
            UIDPersons=loginChecker.getUIDPersons(reqPerson, disabledGracePeriod);
            if (!UIDPersons.isEmpty()){
                String warningJson = "{\n  \"warning\": {\n    \"uids\": [";
                boolean firstElem=true;
                for (String person: UIDPersons){
                    if (firstElem) firstElem=false;
                    else warningJson+= ",";
                    warningJson+="\n      ";
                    warningJson+= "\"" + person + "\"";
                }
                warningJson+="\n    ],\n    \"message\": \"Uids above already appended to a uid record in the DS\"\n  }\n}";
                return warningJson;                
            }
        }
        catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\"error\":\""+e.getMessage()+"\"}";
        }

        Collection<Conflict> conflicts;
        String responseJson = "{";
        try{
            conflicts= loginChecker.checkForValidityConflicts(reqPerson,disabledGracePeriod);
            if (!conflicts.isEmpty()){
                responseJson+=getConflicts(conflicts, reqPerson);
            }
            
            responseJson+=findUserNames(reqPerson, loginChecker, !conflicts.isEmpty());
            if(!conflicts.isEmpty()) responseJson+=proposeUserNames(loginChecker, reqPerson);
            responseJson+="\n}\n";
            res.status(200);
            res.body(new Gson().toJson(responseJson));
            return responseJson;
        }catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\"error\":\""+e.getMessage()+"\"}";
        }
    }

    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }

    public String getConflicts(Collection<Conflict> conflicts, RequestPerson reqPerson){
        String conflictsJson = "\n  \"conflicts\": " ;
        verbose=reqPerson.getVerbose();
        if(verbose){
            boolean firstElem = true;
            for(Conflict conflict : conflicts){
              if(firstElem){
                firstElem = false;
                conflictsJson+="[\n";
               }else{
                conflictsJson += ",\n";
               }
               conflictsJson += conflict.toJson();
            }
            conflictsJson += "\n  ]";
        }else{
            conflictsJson+=" {";
            HashMap<String,HashSet<String>> conflictsPerSource = new HashMap();
            for(Conflict conflict : conflicts){
                if(conflictsPerSource.containsKey(conflict.getConflictSource())){
                    conflictsPerSource.get(conflict.getConflictSource()).add(conflict.getConflictRecordKey());
                }else{
                    HashSet<String> conflRecKeys = new HashSet();
                    conflRecKeys.add(conflict.getConflictRecordKey());
                    conflictsPerSource.put(conflict.getConflictSource(),conflRecKeys);
                }
            }
            boolean firstElem = true;
            for(Map.Entry<String,HashSet<String>> sourceConfls : conflictsPerSource.entrySet()){
                if(firstElem){
                    firstElem = false;
                }else{
                    conflictsJson += ",";
                }
                conflictsJson+="\n    ";
                conflictsJson += "\""+sourceConfls.getKey()+"Conflicts\":"+sourceConfls.getValue().size();
            }
            conflictsJson+="\n  }";
        }
        return conflictsJson;
    }

    public String findUserNames(RequestPerson reqPerson, LoginNameValidityChecker loginChecker, Boolean conflictsExist){

        Collection<AcademicPerson> existingOwners = new Vector<AcademicPerson>();
        Collection<LdapEntry> existingDSOwners = new Vector<LdapEntry>();

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        try{
            sis=Views.getSISConn();
            hrms=Views.getHRMSConn();
            hrms2=Views.getHRMS2Conn();
            ldap=ldapDS.getConn();

            existingOwners.addAll(sis.fetchAll(reqPerson,disabledGracePeriod));
            if (hrms!=null) existingOwners.addAll(hrms.fetchAll(reqPerson,disabledGracePeriod));
            if (hrms2!=null) existingOwners.addAll(hrms2.fetchAll(reqPerson,disabledGracePeriod));
            existingDSOwners.addAll(ldap.search(ldap.createSearchFilter(reqPerson)));
        }catch(Exception e){
            e.printStackTrace();
        }

        String CONN_FILE_DIR = "/etc/v_vd/conn/";
        boolean verbose=false;
        String disabledGracePeriod = null;
        String ssnJson="";
        if (alreadyFound(reqPerson, existingOwners, existingDSOwners)) return "";
        if (conflictsExist) ssnJson+=",";
        ssnJson+= "\n  \"personPairedWith\": [";

        Vector<String> existingUserNames= new Vector<String>();
        if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()){
            boolean firstElem = true;
            if (!existingOwners.isEmpty()){
                for (AcademicPerson person: existingOwners){
                    if (!existingUserNames.contains(person.getLoginName())){
                        if (firstElem) firstElem=false;
                        else ssnJson += ",";
                        ssnJson+="\n    ";
                        ssnJson+="\"" + person.getLoginName() + "\"";
                        existingUserNames.add(person.getLoginName());
                    }
                }
            }
            else if (!existingDSOwners.isEmpty()){
                for (LdapEntry person: existingDSOwners){
                    String uid= person.getAttribute("uid").getStringValue();
                    if (!existingUserNames.contains(uid)){
                        if (firstElem) firstElem=false;
                        else ssnJson += ",";
                        ssnJson+="\n    ";
                        ssnJson+="\"" + uid + "\"";
                        existingUserNames.add(uid);
                    }
                }
            }
            ssnJson+="\n  ]";
            return ssnJson;
        }
        return "";
    }

    public Boolean alreadyFound(RequestPerson reqPerson, Collection<AcademicPerson> existingOwners, Collection<LdapEntry> existingDSOwners){
        if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()){
          if (!existingOwners.isEmpty()){
            if (loginNameFound(reqPerson.getLoginName(), existingOwners)) return true;
          }
          if (!existingDSOwners.isEmpty()){
            if (loginNameFoundDS(reqPerson.getLoginName(), existingDSOwners)) return true;
          }
        }
        return false;
    }

    public Boolean loginNameFound(String loginName, Collection<AcademicPerson> existingOwners){
      for (AcademicPerson person: existingOwners){
        if (person.getLoginName()!=null && person.getLoginName().equals(loginName)) return true;
      }
      return false;
    }

    public Boolean loginNameFoundDS(String loginName, Collection<LdapEntry> existingDSOwners){
      for (LdapEntry person: existingDSOwners){
        if (person.getAttribute("uid")==null) continue;
        String uid= person.getAttribute("uid").getStringValue();
        if (uid.equals(loginName)) return true;
      }
      return false;
    }

    public String proposeUserNames(LoginNameValidityChecker loginChecker, AcademicPerson reqPerson) throws LdapException, Exception{
        Collection<AcademicPerson> existingOwners = new Vector<AcademicPerson>();
        Collection<LdapEntry> existingDSOwners = new Vector<LdapEntry>();
        String CONN_FILE_DIR = "/etc/v_vd/conn/";

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        try{
            sis=Views.getSISConn();
            hrms=Views.getHRMSConn();
            hrms2=Views.getHRMS2Conn();
            ldap=ldapDS.getConn();

            existingOwners.addAll(sis.fetchAll("SSN",reqPerson.getSSN(),null));
            if (hrms!=null) existingOwners.addAll(hrms.fetchAll("SSN",reqPerson.getSSN(),null));
            if (hrms2!=null) existingOwners.addAll(hrms2.fetchAll("SSN",reqPerson.getSSN(),null));
            existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN="+reqPerson.getSsn())));
        }catch(Exception e){
            e.printStackTrace();
        }

        Vector<String> proposedNames =new Vector<String>();
        String proposeJson = ",\n  \"suggestions\":  [";
        if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()){
            UserNameGen loginGen = null;
            if (!existingOwners.isEmpty()) loginGen= new UserNameGen(existingOwners.iterator().next());
            else loginGen= new UserNameGen(existingDSOwners.iterator().next());
            boolean firstElem = true;

            proposedNames= loginGen.proposeNames();
            if (proposedNames!=null && !proposedNames.isEmpty()){
                for(String login : proposedNames){
                    if (loginChecker.checkIfUserNameExists(login)) continue;
                    if(firstElem){
                        firstElem = false;
                    }else{
                        proposeJson += ",";
                    }
                    proposeJson+="\n    ";
                    proposeJson += "\""+login+"\"";
                }
            }
        }
        if (proposedNames!=null && !proposedNames.isEmpty()){
            proposeJson += "\n  ]";
            return proposeJson;
        }
        return "";
    }
}
