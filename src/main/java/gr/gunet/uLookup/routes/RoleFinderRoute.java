package gr.gunet.uLookup.routes;

import com.google.gson.Gson;
import gr.gunet.uLookup.AcademicPerson;
import gr.gunet.uLookup.ResponseMessages;
import gr.gunet.uLookup.db.DBConnectionPool;
import gr.gunet.uLookup.db.HRMSDBView;
import gr.gunet.uLookup.db.SISDBView;
import gr.gunet.uLookup.ldap.LdapConnectionPool;
import gr.gunet.uLookup.ldap.LdapManager;
import gr.gunet.uLookup.tools.CustomJsonReader;
import gr.gunet.uLookup.tools.PropertyReader;
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
    ResponseMessages responses;
    public RoleFinderRoute(String institution) {
      this.institution= institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        responses= new ResponseMessages();
        if (!req.session().attribute("authorized").equals("true")){
          String errorMessage= "You were not authorized";
          closeViews();
          return responses.getValidatorResponse("401", errorMessage);
        }
        CustomJsonReader jsonReader = new CustomJsonReader(req.body());
        String roleJson= "{";
        String message="";
        String roles="";
        String response_code="";
        String response="";

        loginName = jsonReader.readPropertyAsString("loginName");
        String errorDescription="";

        if(loginName == null || loginName.trim().equals("")) {
          response_code = "400";
          errorDescription= "No loginName provided.";
        }
        else if (!loginName.equals(loginName.trim())){
          response_code = "400";
          errorDescription="Whitespace character found.";
        }
        else if (loginName.length() < 4 || loginName.length() > 20){
          response_code = "400";
          errorDescription="LoginName length outside character limits.";
        }else if (!loginName.matches("([a-z0-9]+[._-]?[a-z0-9]+)+")){
          response_code = "400";
          for(int i=0;i<loginName.length();i++){
              char ch = loginName.charAt(i);
              if(Character.isUpperCase(ch)){
                errorDescription="Capital character found.";
                break;
              }
          }
          if (errorDescription.equals("")) errorDescription= "Invalid loginName format.";
        }

        if (response_code.equals("400")){
          closeViews();
          message= "\n\t\"message\": \"" + errorDescription + "\"";
          roleJson+= "\n\t\"Response code\" : " + response_code + ",";
          roleJson+=message;
          response+=roleJson;
          response+="\n}";
          return response;
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
            e.printStackTrace(System.err);
            errorJson="{\n\t\"Response code\" : 500,\n" +"  \"message\" : \"Could not connect to the SIS\"\n}\n";
        }

        try{
          hrms = Views.getHRMSConn();
          if (hrms!=null) existingHRMSOwners= hrms.fetchAll("loginName", loginName, onlyActive);
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            errorJson="{\n\t\"Response code\" : 500,\n" +"  \"message\" : \"Could not connect to the HRMS\"\n}\n";
        }

        try{
          hrms2 = Views.getHRMS2Conn();
          if (hrms2!=null) existingHRMS2Owners= hrms2.fetchAll("loginName", loginName, onlyActive);
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            errorJson="{\n\t\"Response code\" : 500,\n" +"  \"message\" : \"Could not connect to the HRMS2\"\n}\n";
        }

        if (!errorJson.equals("")) return errorJson;

        if (existingSISOwners.isEmpty() && existingHRMSOwners.isEmpty() && existingHRMS2Owners.isEmpty()){
            response_code="000";
            message= "\n\t\"message\": \"" + loginName + " not found in any Database\"";
        }
        else {
            response_code="";
            boolean firstElem = true;
            message= "\n\t\"message\": \"" + loginName + " found\",";
            roles= "\n\t\"Roles\" : [";
            if (!existingSISOwners.isEmpty()) {
                if(firstElem){
                    firstElem = false;
                }else{
                    roles += ",";
                }
                roles += "\n\t\t";
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
                roles += "\n\t\t";
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
                roles += "\n\t\t";
                roles += "\"Associate\"";
                response_code += "1";
            }
            else response_code += "0";

            roles+="\n\t]";
        }
        roleJson+= "\n\t\"Response code\" : " + response_code + ",";
        roleJson+=message;
        roleJson+=roles;
        roleJson+="\n}\n";
        response+=roleJson;
        return response;
    }

    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }
}