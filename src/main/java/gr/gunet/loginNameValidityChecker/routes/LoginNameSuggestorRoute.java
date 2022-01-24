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

    public LoginNameSuggestorRoute() {
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        res.type("application/json");
        response_code="";
        message="";
        responseJson="";
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

        Views= new DBConnectionPool(institution);
        ldapDS= new LdapConnectionPool(institution);

        Collection<AcademicPerson> existingOwners = new Vector<AcademicPerson>();
        Collection<LdapEntry> existingDSOwners = new Vector<LdapEntry>();

        String foundJson= "{";
        message="";
        String foundNames="";

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        try {
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

                foundNames= "\n  \"personPairedWith\": [";

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
                message= "\n  \"message\": \"" + SSN + "-" + SSNCountry + " is already paired with at least 1 loginName";
                responseJson+=foundNames;
                int exit_code= generateNames();

            }else{
                response_code+="20";
                message= "\n  \"message\": \"" + SSN + "-" + SSNCountry + " combination not found in any Database\"";
            }
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
            closeViews();
            return "{\n  \"Response code\" : 09,\n" +
                    "  \"message\" : \""+e.getMessage()+"\"\n}\n";
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
        String suggestedNames="";
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
                if (SSNCountry.equals("GR"))
                    existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + SSN)));
                if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()){
                    if (!existingOwners.isEmpty()) loginGen= new UserNameGen(existingOwners.iterator().next());
                    else loginGen= new UserNameGen(existingDSOwners.iterator().next());
                }
            }
            else{
                loginGen=new UserNameGen(FN, LN);
            }

            proposedNames= loginGen.proposeNames();
            if (proposedNames!=null && !proposedNames.isEmpty()){
                response_code+="0";
                message+= ", Generator managed to create suggested names\",";
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
