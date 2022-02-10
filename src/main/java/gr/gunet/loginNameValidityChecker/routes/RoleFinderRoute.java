package gr.gunet.loginNameValidityChecker.routes;

import com.google.gson.Gson;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import gr.gunet.loginNameValidityChecker.db.HRMSDBView;
import gr.gunet.loginNameValidityChecker.db.SISDBView;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;
import gr.gunet.loginNameValidityChecker.ldap.LdapManager;
import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Collection;

public class RoleFinderRoute implements Route {
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    String loginName;
    String institution;
    boolean onlyActive;
    private String CONN_FILE_DIR = "/etc/v_vd/conn/";
    public RoleFinderRoute() {
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        res.type("application/json");
        String reqBody = req.body();
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
        String Active=reader.readPropertyAsString("onlyActive");
        if (Active== null || Active.trim().equals("") || Active.equals("false")){
          onlyActive=false;
        }
        else onlyActive=true;
        
        if (loginName==null || institution==null){
          closeViews();
          response_code="3400";
          String missingAttribute;
          if (loginName==null) missingAttribute="loginName";
          else missingAttribute="institution";
          message= "\n  \"message\": \"No " + missingAttribute + " provided\"";
          
          roleJson+= "\n  \"Response code\" : " + response_code + ",";
          roleJson+=message;
          roleJson+="\n}\n";
          res.body(new Gson().toJson(roleJson));
          return roleJson;
        }
        else if (institution!= null && !institutionExists(institution)){
          closeViews();
          String errorJson="{\n  \"Response code\" : 3401,\n" +"  \"message\" : \"Could not connect to \'"+ institution+"\'\"\n}\n";
          res.body(new Gson().toJson(errorJson));
          return errorJson;
        }

        Views = new DBConnectionPool(institution);
        ldapDS = new LdapConnectionPool(institution);

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;
        String errorJson="";
        Collection<AcademicPerson> existingSISOwners=null, existingHRMSOwners=null, existingHRMS2Owners= null;
        
        try{
          sis = Views.getSISConn();
          existingSISOwners= sis.fetchAll("loginName", loginName, onlyActive);
        }
        catch (Exception e){
          errorJson="{\n  \"Response code\" : 3500,\n" +"  \"message\" : \"Could not connect to the SIS\"\n}\n";
        }

        try{
          hrms = Views.getHRMSConn();
          if (hrms!=null) existingHRMSOwners= hrms.fetchAll("loginName", loginName, onlyActive);
        }
        catch (Exception e){
          errorJson="{\n  \"Response code\" : 3500,\n" +"  \"message\" : \"Could not connect to the HRMS\"\n}\n";
        }

        try{
          hrms2 = Views.getHRMS2Conn();
          if (hrms2!=null) existingHRMS2Owners= hrms2.fetchAll("loginName", loginName, onlyActive);
        }
        catch (Exception e){
          errorJson="{\n  \"Response code\" : 3500,\n" +"  \"message\" : \"Could not connect to the HRMS2\"\n}\n";
        }

        if (!errorJson.equals("")){
          res.body(new Gson().toJson(errorJson));
          return errorJson;
        }

        if (existingSISOwners.isEmpty() && existingHRMSOwners.isEmpty() && existingHRMS2Owners.isEmpty()){
            response_code="3000";
            message= "\n  \"message\": \"" + loginName + " not found in any Database\"";
        }
        else {
            response_code="3";
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
        res.body(new Gson().toJson(roleJson));
        return roleJson;
    }
    public boolean institutionExists(String institution){
      Path path= Paths.get(CONN_FILE_DIR+ institution + ".properties");
      if (!Files.exists(path)) {
        return false;
      }
      return true;
    }
    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }
}