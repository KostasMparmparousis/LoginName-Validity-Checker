package gr.gunet.loginNameValidityChecker.routes;

import com.google.gson.Gson;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import gr.gunet.loginNameValidityChecker.db.HRMSDBView;
import gr.gunet.loginNameValidityChecker.db.SISDBView;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;
import gr.gunet.loginNameValidityChecker.ldap.LdapManager;
import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Collection;

public class RoleFinderRoute /*implements Route*/ {
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    String loginName;
    String institution;
    public RoleFinderRoute() {
    }

    //@Override
    public Object handle(String req /*Request req, Response res*/) throws Exception {
        //res.type("application/json");
        String CONN_FILE_DIR = "./etc/v_vd/conn";
        String reqBody = req; /*req.body();*/
        CustomJsonReader reader;
        try {
            reader = new CustomJsonReader(reqBody);
        } catch (Exception e) {
            return e.getMessage();
        }

        String roleJson= "{";
        String message="";
        String roles="";
        String response_code="";

        loginName = reader.readPropertyAsString("loginName");
        institution = reader.readPropertyAsString("institution");

        Views = new DBConnectionPool(institution);
        ldapDS = new LdapConnectionPool(institution);

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        Collection<AcademicPerson> existingSISOwners=null, existingHRMSOwners=null, existingHRMS2Owners= null;
        try {
            sis = Views.getSISConn();
            hrms = Views.getHRMSConn();
            hrms2 = Views.getHRMS2Conn();
            ldap = ldapDS.getConn();
            existingSISOwners= sis.fetchAll("loginName", loginName, null);
            if (hrms!=null) existingHRMSOwners= hrms.fetchAll("loginName", loginName, null);
            if (hrms2!=null) existingHRMS2Owners= hrms2.fetchAll("loginName", loginName, null);

            if (existingSISOwners.isEmpty() && existingHRMSOwners.isEmpty() && existingHRMS2Owners.isEmpty()){
                response_code="000";
                message= "\n  \"message\": \"" + loginName + " not found in any Database\"";
            }
            else {
                message= "\n  \"message\": \"" + loginName + " found\",";
                roles= "\n  \"Roles\" : {";
                roles += "\n    \"Student\" : ";
                if (!existingSISOwners.isEmpty()) {
                    roles += "\"Yes\",";
                    response_code += "1";
                } else {
                    roles += "\"No\",";
                    response_code += "0";
                }

                roles += "\n    \"Member Of The Teaching Staff\" : ";
                if (existingHRMSOwners != null && !existingHRMSOwners.isEmpty()) {
                    roles += "\"Yes\",";
                    response_code += "1";
                } else {
                    roles += "\"No\",";
                    response_code += "0";
                }

                roles += "\n    \"Associate\" : ";
                if (existingHRMS2Owners != null && !existingHRMS2Owners.isEmpty()) {
                    roles += "\"Yes\"";
                    response_code += "1";
                } else {
                    roles += "\"No\"";
                    response_code += "0";
                }
                roles+="\n  }";
            }
            roleJson+= "\n  \"Response code\" : " + response_code + ",";
            roleJson+=message;
            roleJson+=roles;
            roleJson+="\n}\n";
            //res.status(200);
            //res.body(new Gson().toJson(roleJson));

            return roleJson;
        }catch (Exception e){
            e.printStackTrace(System.err);
            closeViews();
            return "{\n  \"Response code\" : 09,\n" +
                    "  \"message\" : \""+e.getMessage()+"\"\n}\n";
        }
    }
    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }
}