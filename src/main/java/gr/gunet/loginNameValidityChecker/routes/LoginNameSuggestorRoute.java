package gr.gunet.loginNameValidityChecker.routes;
import com.google.gson.Gson;
import gr.gunet.loginNameValidityChecker.RequestPerson;
import gr.gunet.loginNameValidityChecker.generator.UserNameGen;
import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import gr.gunet.loginNameValidityChecker.db.HRMSDBView;
import gr.gunet.loginNameValidityChecker.db.SISDBView;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import gr.gunet.loginNameValidityChecker.ldap.LdapManager;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;
import org.ldaptive.LdapEntry;

import java.util.Collection;
import java.util.Vector;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginNameSuggestorRoute implements Route {
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    String SSN;
    String SSNCountry;
    String FN;
    String LN;
    String institution;
    String response_code;
    String message;
    String responseJson;
    String personPairedWith;
    String suggestedNames;
    public LoginNameSuggestorRoute() {
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        res.type("application/json");
        response_code="";
        message="";
        responseJson="";
        personPairedWith="";
        suggestedNames="";
        String CONN_FILE_DIR = "/etc/v_vd/conn";
        String reqBody= req.body();
        CustomJsonReader reader;
        try {
            reader= new CustomJsonReader(reqBody);
        }catch (Exception e){
            return e.getMessage();
        }

        SSN = reader.readPropertyAsString("ssn");
        SSNCountry = reader.readPropertyAsString("ssnCountry");
        FN= reader.readPropertyAsString("firstName");
        LN= reader.readPropertyAsString("lastName");
        institution = reader.readPropertyAsString("institution");
        
        if (institution==null){
          String errorJson="{\n  \"Response code\" : 40,\n" +"  \"message\" : \"No institution provided\"\n}\n";
          return errorJson;
        }

        Views= new DBConnectionPool(institution);
        ldapDS= new LdapConnectionPool(institution);

        Collection<AcademicPerson> existingOwners = new Vector<AcademicPerson>();
        Collection<LdapEntry> existingDSOwners = new Vector<LdapEntry>();

        String foundJson= "{";
        message="";

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        try {
            if (SSN==null || SSNCountry==null){
              response_code+="3";
              message= "\n  \"message\":\"";
              if (SSN==null) message+= "SSN not given, ";
              if (SSNCountry==null) message+="SSNCountry not given, ";
              int exit_code= generateNames();
              String returnJson="{";
              returnJson+= "\n  \"Response code\" : " + response_code+ ",";
              returnJson+=message;
              responseJson+=suggestedNames;
              returnJson+=responseJson;
              returnJson+="\n}";
              res.body(new Gson().toJson(returnJson));
              return returnJson;
            }

            sis = Views.getSISConn();
            hrms = Views.getHRMSConn();
            hrms2 = Views.getHRMS2Conn();
            ldap = ldapDS.getConn();

            existingOwners.addAll(sis.fetchAll(SSN, SSNCountry));
            if (hrms != null) existingOwners.addAll(hrms.fetchAll(SSN, SSNCountry));
            if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(SSN, SSNCountry));
            if (SSNCountry.equals("GR")) existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + SSN)));

            Vector<String> existingUserNames= new Vector<String>();
            if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) {
                response_code+="1";

                personPairedWith= "\n  \"personPairedWith\": [";

                boolean firstElem = true;
                if (!existingOwners.isEmpty()) {
                    for (AcademicPerson person : existingOwners) {
                        if (!existingUserNames.contains(person.getLoginName())) {
                            if (firstElem) firstElem = false;
                            else personPairedWith += ",";
                            personPairedWith += "\n    ";
                            personPairedWith += "\"" + person.getLoginName() + "\"";
                            existingUserNames.add(person.getLoginName());
                        }
                    }
                }
                else if (!existingDSOwners.isEmpty()) {
                    for (LdapEntry person : existingDSOwners) {
                        String uid = person.getAttribute("uid").getStringValue();
                        if (!existingUserNames.contains(uid)) {
                            if (firstElem) firstElem = false;
                            else personPairedWith += ",";
                            personPairedWith += "\n    ";
                            personPairedWith += "\"" + uid + "\"";
                            existingUserNames.add(uid);
                        }
                    }
                }
                personPairedWith += "\n  ]";
                message= "\n  \"message\": \"" + SSN + "-" + SSNCountry + " is already paired with at least 1 loginName, ";
            }else{
                response_code+="2";
                message= "\n  \"message\": \"" + SSN + "-" + SSNCountry + " combination not found in any Database, ";
            }
            int exit_code= generateNames();
            responseJson+=personPairedWith;
            responseJson+=suggestedNames;
            String returnJson="{";
            returnJson+= "\n  \"Response code\" : " + response_code+ ",";
            returnJson+=message;
            returnJson+=responseJson;
            returnJson+="\n}";
            res.body(new Gson().toJson(returnJson));
            return returnJson;
            //res.status(200);

        }catch (Exception e){
            e.printStackTrace(System.err);
            try{
              closeViews();
            }
            catch(Exception e1){
              e1.printStackTrace(System.err);
              String errorJson="{\n  \"Response code\" : 51,\n" +"  \"message\" : \"Could not connect to \'"+ institution+"\' DB View, incorrect connection details\"\n}\n";
              res.status(51);
              res.body(new Gson().toJson(errorJson));
              return errorJson;
            }
            String errorJson="{\n  \"Response code\" : 50,\n" +"  \"message\" : \"Could not connect to \'"+ institution+"\' DB View\"\n}\n";
            res.status(50);
            res.body(new Gson().toJson(errorJson));
            return errorJson;
        }
    }

    public int generateNames(){
        Collection<AcademicPerson> existingOwners = new Vector<AcademicPerson>();
        Collection<LdapEntry> existingDSOwners = new Vector<LdapEntry>();
        int exit_code=0;
        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        Vector<String> proposedNames =new Vector<String>();
        UserNameGen loginGen = null;
        boolean firstElem = true;
        try {
            sis = Views.getSISConn();
            hrms = Views.getHRMSConn();
            hrms2 = Views.getHRMS2Conn();
            ldap = ldapDS.getConn();

            if (FN==null || LN==null){
                existingOwners.addAll(sis.fetchAll(SSN, SSNCountry));
                if (hrms != null) existingOwners.addAll(hrms.fetchAll(SSN, SSNCountry));
                if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(SSN, SSNCountry));
                if (SSNCountry!=null && SSNCountry.equals("GR"))
                    existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + SSN)));
                if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()){
                    if (!existingOwners.isEmpty()) loginGen= new UserNameGen(existingOwners.iterator().next());
                    else loginGen= new UserNameGen(existingDSOwners.iterator().next());
                }
            }
            else{
                loginGen=new UserNameGen(FN, LN);
            }

            if (loginGen!=null) proposedNames= loginGen.proposeNames();
            if (proposedNames!=null && !proposedNames.isEmpty()){
                response_code+="0";
                message+= "Generator managed to create suggested names\",";
                if (!personPairedWith.equals("")) personPairedWith+=",";
                suggestedNames = "\n  \"suggestions\":  [";
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
            }
            else{
              response_code+="1";
              message+= "Generator did not manage to create suggested names\"";
            }
        }catch (Exception e){
            e.printStackTrace(System.err);
            closeViews();
        }
        return exit_code;
    }

    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }

}
