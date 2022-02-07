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

//    @Override
    public Object handle(String req /*Request req, Response res*/) throws Exception {
        //res.type("application/json");
        String CONN_FILE_DIR = "/etc/v_vd/conn";
        String reqBody = req;
        //String reqBody = req.body();
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
        
        if (loginName==null || institution==null){
          response_code="400";
          message= "\n  \"message\": \"";
          if (loginName==null) {
            message += "loginName";
            if (institution==null) message+=",";
          }
          if (institution==null) message += "institution";
          message+=" not given\"";
          
          roleJson+= "\n  \"Response code\" : " + response_code + ",";
          roleJson+=message;
          roleJson+="\n}\n";
          return roleJson;
        }

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
                boolean firstElem = true;
                message= "\n  \"message\": \"" + loginName + " found\",";
                roles= "\n  \"Roles\" : [";
                if (!existingSISOwners.isEmpty()) {
                    if(firstElem){
                        firstElem = false;
                    }else{
                        roles += ",";
                    }
                    roles += "\n    ";
                    roles += "\"Student\"";
                    response_code += "1";
                }
                else response_code += "0";

                if (existingHRMSOwners != null && !existingHRMSOwners.isEmpty()) {
                    if(firstElem){
                        firstElem = false;
                    }else{
                        roles += ",";
                    }
                    roles += "\n    ";
                    roles += "\"Member of the Teaching Staff\"";
                    response_code += "1";
                }
                else response_code += "0";

                if (existingHRMS2Owners != null && !existingHRMS2Owners.isEmpty()) {
                    if(firstElem){
                        firstElem = false;
                    }else{
                        roles += ",";
                    }
                    roles += "\n    ";
                    roles += "\"Associate\"";
                    response_code += "1";
                }
                else response_code += "0";

                roles+="\n  ]";
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
            try{
              closeViews();
            }
            catch(Exception e1){
              e1.printStackTrace(System.err);
              String errorJson="{\n  \"Response code\" : 501,\n" +"  \"message\" : \"Could not connect to \'"+ institution+"\' DB View, incorrect connection details\"\n}\n";
              //res.status(501);
              //res.body(new Gson().toJson(errorJson));
              return errorJson;
            }
            
            String errorJson="{\n  \"Response code\" : 500,\n" +"  \"message\" : \"Could not connect to \'"+ institution+"\' DB View\"\n}\n";
            //res.status(500);
            //res.body(new Gson().toJson(errorJson));
            return errorJson;
        }
    }
    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }
}