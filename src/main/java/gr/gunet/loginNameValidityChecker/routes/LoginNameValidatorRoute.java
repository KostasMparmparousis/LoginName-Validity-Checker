package gr.gunet.loginNameValidityChecker.routes;
import com.google.gson.Gson;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import gr.gunet.loginNameValidityChecker.Conflict;
import gr.gunet.loginNameValidityChecker.LoginNameValidator;
import gr.gunet.loginNameValidityChecker.db.HRMSDBView;
import gr.gunet.loginNameValidityChecker.db.SISDBView;
import gr.gunet.loginNameValidityChecker.generator.UserNameGen;
import gr.gunet.loginNameValidityChecker.ldap.LdapManager;
import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import gr.gunet.loginNameValidityChecker.RequestPerson;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;

import org.ldaptive.LdapEntry;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginNameValidatorRoute implements Route{
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    boolean verbose= false;
    String disabledGracePeriod=null;
    String response_code;
    String message;
    String responseJson;
    private String CONN_FILE_DIR = "/etc/v_vd/conn/";

    public LoginNameValidatorRoute() {
    }
    @Override
    public Object handle(Request req, Response res) throws Exception{
        response_code="";
        res.type("application/json");
        CustomJsonReader jsonReader = new CustomJsonReader(req.body());
        boolean verbose=false;
        String disabledGracePeriod = null;

        RequestPerson reqPerson;
        try{
            reqPerson = new RequestPerson(jsonReader);
        }catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            String errorJson="{\n  \"Response code\" : 1400,\n" +
                    "  \"message\" : \""+e.getMessage()+"\"\n}\n";
            res.status(1400);
            res.body(new Gson().toJson(errorJson));
            return errorJson;
        }

        if (!institutionExists(reqPerson.getInstitution())){
          closeViews();
          String errorJson="{\n  \"Response code\" : 1401,\n" +"  \"message\" : \"Could not connect to \'"+ reqPerson.getInstitution()+"\'\"\n}\n";
          res.status(1401);
          res.body(new Gson().toJson(errorJson));
          return errorJson;
        }

        Views= new DBConnectionPool(reqPerson.getInstitution());
        ldapDS= new LdapConnectionPool(reqPerson.getInstitution());
        LoginNameValidator loginChecker = new LoginNameValidator(Views, ldapDS);

        Collection<String> UIDPersons;
        try{
            UIDPersons=loginChecker.getUIDPersons(reqPerson, disabledGracePeriod);
            if (!UIDPersons.isEmpty()){
                String uid= UIDPersons.iterator().next();
                String warningJson="{\n  \"Response code\" : 1300, \n"+
                        "  \"message\" : \"" + uid + " already exists while not following the typical DS Account generation procedure\"\n}";
                res.status(1300);
                res.body(new Gson().toJson(warningJson));
                return warningJson;
            }
        }
        catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            String errorJson="{\n  \"Response code\" : 1500,\n" +"  \"message\" : \"Could not connect to the DS\"\n}\n";
            res.status(1500);
            res.body(new Gson().toJson(errorJson));
            return errorJson;
        }
        
        Collection<Conflict> conflicts;
        responseJson = "";
        try{
            response_code="1";
            conflicts= loginChecker.checkForValidityConflicts(reqPerson,disabledGracePeriod);
            responseJson+=getConflicts(conflicts,reqPerson);
            res.body(new Gson().toJson(responseJson));
            return responseJson;
        }catch(Exception e){
            e.printStackTrace(System.err);
            String errorSource= e.getMessage();
            String errorJson="{\n  \"Response code\" : 1500,\n" +"  \"message\" : \"Could not connect to the " + errorSource + "\"\n}\n";
            res.status(1500);
            res.body(new Gson().toJson(errorJson));
            return errorJson;
        }
        
    }

    public boolean institutionExists(String institution){
      Path path= Paths.get(CONN_FILE_DIR+ institution + ".properties");
      if (!Files.exists(path)) {
        return false;
      }
      return true;
    }
    
    public String getConflicts(Collection<Conflict> conflicts, RequestPerson reqPerson){
        message=",\n  \"message\" : \"";
        if (conflicts.isEmpty()) {
            message += "No ";
        }
        message += "Conflicts Found";
        String conflictsJson="{";
        verbose=reqPerson.getVerbose();

        if (!conflicts.isEmpty()) {
            response_code+="2";
            if (verbose){
                responseJson += ",\n  \"conflicts\": " ;
                boolean firstElem = true;
                for(Conflict conflict : conflicts){
                    if(firstElem){
                        firstElem = false;
                        responseJson+="[\n";
                    }else{
                        responseJson += ",\n";
                    }
                    responseJson += conflict.toJson();
                }
                responseJson += "\n  ]";
            }
        }
        else response_code+="1";
        getExistingLoginNames(reqPerson);
        response_code+="0";

        conflictsJson+="\n  \"Response code\" : " + response_code;
        conflictsJson+=message;
        conflictsJson+=responseJson;
        if (responseJson.equals("")) conflictsJson+="\"";
        conflictsJson+="\n}\n";
        return conflictsJson;
    }

    public void getExistingLoginNames(RequestPerson reqPerson){
        if (!reqPerson.findExisting()) {
            response_code+="2";
            return ;
        }
        Collection<AcademicPerson> existingOwners = new Vector<AcademicPerson>();
        Collection<LdapEntry> existingDSOwners = new Vector<LdapEntry>();

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        try {
            sis = Views.getSISConn();
            hrms = Views.getHRMSConn();
            hrms2 = Views.getHRMS2Conn();
            ldap = ldapDS.getConn();

            existingOwners.addAll(sis.fetchAll(reqPerson, null));
            if (hrms != null) existingOwners.addAll(hrms.fetchAll(reqPerson, null));
            if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(reqPerson, null));
            if (reqPerson.getSSNCountry().equals("GR"))
                existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + reqPerson.getSSN())));

            Vector<String> existingUserNames= new Vector<String>();
            String foundNames="";
            if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) {
                response_code+="0";

                foundNames= ",\n  \"personPairedWith\": [";
                boolean firstElem = true;
                if (!existingOwners.isEmpty()) {

                    for (AcademicPerson person : existingOwners) {
                        if (!existingUserNames.contains(person.getLoginName())) {
                            if (firstElem) firstElem = false;
                            else foundNames += ",";
                            foundNames += "\n    ";
                            foundNames += "\"" + person.getLoginName() + "\"";
                            existingUserNames.add(person.getLoginName());
                        }
                    }
                }
                else if (!existingDSOwners.isEmpty()) {
                    foundNames= ",\n  \"personPairedWith\": [";
                    for (LdapEntry person : existingDSOwners) {
                        String uid = person.getAttribute("uid").getStringValue();
                        if (!existingUserNames.contains(uid)) {
                            if (firstElem) firstElem = false;
                            else foundNames += ",";
                            foundNames += "\n    ";
                            foundNames += "\"" + uid + "\"";
                            existingUserNames.add(uid);
                        }
                    }
                }
                if (existingUserNames.contains(reqPerson.getLoginName()) && existingUserNames.size()==1) {
                    responseJson="";
                    return;
                }
                message+= ", " + reqPerson.getSSN() + "-" + reqPerson.getSSNCountry() + " is already paired with at least 1 loginName\"";
                foundNames += "\n  ]";
                responseJson+=foundNames;
            }else{
                response_code+="1";
                message+= ", " + reqPerson.getSSN() + "-" + reqPerson.getSSNCountry() + " combination not found in any Database";
                if (!responseJson.equals("")) message+="\"";
            }
        }catch (Exception e){
            e.printStackTrace(System.err);
            
            closeViews();
        }
    }

    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }

}
