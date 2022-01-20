package gr.gunet.loginNameValidityChecker.routes;
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

import java.util.*;

import org.ldaptive.LdapEntry;


public class LoginNameValidatorRoute /*implements Route*/{
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    boolean verbose= false;
    String disabledGracePeriod=null;
    String response_code;
    String message;
    String responseJson;

    public LoginNameValidatorRoute() {
    }
    //@Override
    public Object handle(String req /*Request req, Response res*/) throws Exception{
        response_code="";
        //res.type("application/json");
        CustomJsonReader jsonReader = new CustomJsonReader(req /*req.body()*/);
        boolean verbose=false;
        String disabledGracePeriod = null;

        RequestPerson reqPerson;
        try{
            reqPerson = new RequestPerson(jsonReader);
        }catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\n  \"Response code\" : 09,\n" +
                    "  \"message\" : \""+e.getMessage()+"\"\n}\n";
        }

        Views= new DBConnectionPool(reqPerson.getInstitution());
        ldapDS= new LdapConnectionPool(reqPerson.getInstitution());
        LoginNameValidator loginChecker = new LoginNameValidator(Views, ldapDS);

        Collection<String> UIDPersons;
        try{
            UIDPersons=loginChecker.getUIDPersons(reqPerson, disabledGracePeriod);
            if (!UIDPersons.isEmpty()){
                String uid= UIDPersons.iterator().next();
                String warningJson="{\n  \"Response code\" : 300, \n"+
                        "  \"message\" : \"" + uid + " already exists while not following the typical DS Account generation procedure\"\n}";
                return warningJson;
            }
        }
        catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\n  \"Response code\" : 09,\n" +
                    "  \"message\" : \""+e.getMessage()+"\"\n}\n";
        }

        Collection<Conflict> conflicts;
        responseJson = "";
        try{
            conflicts= loginChecker.checkForValidityConflicts(reqPerson,disabledGracePeriod);
            responseJson+=getConflicts(conflicts,reqPerson);
            return responseJson;
        }catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\n  \"Response code\" : 09,\n" +
                    "  \"message\" : \""+e.getMessage()+"\"\n}\n";
        }
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
        int exit_code= getExistingLoginNames(reqPerson);
        if (conflicts.isEmpty() || exit_code!=2) response_code+="0";
        else generateNames(reqPerson);

        conflictsJson+="\n  \"Response code\" : " + response_code;
        conflictsJson+=message;
        conflictsJson+=responseJson;
        if (responseJson.equals("")) conflictsJson+="\"";
        conflictsJson+="\n}\n";
        return conflictsJson;
    }

    public int getExistingLoginNames(RequestPerson reqPerson){
        int exit_code=0;
        if (!reqPerson.findExisting()) {
            response_code+="2";
            return 2;
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
                message+= ", " + reqPerson.getSSN() + "-" + reqPerson.getSSNCountry() + " is already paired with at least 1 loginName\"";
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
                foundNames += "\n  ]";
                responseJson+=foundNames;
                exit_code=0;
            }else{
                response_code+="1";
                exit_code=1;
                message+= ", " + reqPerson.getSSN() + "-" + reqPerson.getSSNCountry() + " combination not found in any Database";
                if (!responseJson.equals("")) message+="\"";
            }
        }catch (Exception e){
            e.printStackTrace(System.err);
            closeViews();
            exit_code=-1;
        }
        return exit_code;
    }
    public void generateNames(RequestPerson reqPerson){
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

            String suggestedNames="";
            Vector<String> proposedNames =new Vector<String>();
            if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()){
                UserNameGen loginGen = null;
                if (!existingOwners.isEmpty()) loginGen= new UserNameGen(existingOwners.iterator().next());
                else loginGen= new UserNameGen(existingDSOwners.iterator().next());
                boolean firstElem = true;

                proposedNames= loginGen.proposeNames();
                if (proposedNames!=null && !proposedNames.isEmpty()){
                    response_code+="0";
                    message+= ", Generator managed to create suggested names\"";
                    suggestedNames = ",\n  \"suggestions\":  [";
                    for(String login : proposedNames){
                        if (loginGen.checkIfUserNameExists(login, Views, ldapDS)) continue;
                        if(firstElem){
                            firstElem = false;
                        }else{
                            suggestedNames += ",";
                        }
                        suggestedNames +="\n    ";
                        suggestedNames += "\""+login+"\"";
                    }
                    suggestedNames+="\n  ]";
                    responseJson+=suggestedNames;
                }
                else{
                    response_code+="2";
                    message+= ", Generator could not suggest names, firstNameEn and lastNameEn probably not available";
                    if (!responseJson.equals("")) message+="\"";
                }
            }
            else {
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
