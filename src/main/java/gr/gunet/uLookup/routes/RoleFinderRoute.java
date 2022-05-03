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
        boolean fromWeb;
        
        String title= "Roles Found";
        responses= new ResponseMessages(req.session().attribute("web"));
        if (!req.session().attribute("authorized").equals("true")){
          String errorMessage= "You were not authorized";
          closeViews();
          return responses.getResponse("401", errorMessage, title);
        }
        String roleJson= "{";
        String message="";
        String roles="";
        String response_code="";
        String response="";

        if (!req.session().attribute("web").equals("true")){
          res.type("application/json");
          CustomJsonReader jsonReader = new CustomJsonReader(req.body());
          loginName = jsonReader.readPropertyAsString("loginName");
          fromWeb=false;
        }
        else{
          res.type("text/html");
          loginName = req.queryParams("loginName");
          fromWeb=true;
        }
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
          return responses.getResponse("400", errorDescription, title);
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
            closeViews();
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the SIS\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("500", "SIS", title);
        }

        try{
          hrms = Views.getHRMSConn();
          if (hrms!=null) existingHRMSOwners= hrms.fetchAll("loginName", loginName, onlyActive);
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            closeViews();
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the HRMS\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("500", "HRMS", title);
        }

        try{
          hrms2 = Views.getHRMS2Conn();
          if (hrms2!=null) existingHRMS2Owners= hrms2.fetchAll("loginName", loginName, onlyActive);
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            closeViews();
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the ELKE\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("500", "ELKE", title);
        }
        if (!errorJson.equals("")) return errorJson;
        if (existingSISOwners.isEmpty() && existingHRMSOwners.isEmpty() && existingHRMS2Owners.isEmpty()){
            response_code="000";
            return responses.getResponse(response_code, "", title);
        }
        else {
            response_code="";
            boolean firstElem = true;
            if (!fromWeb) roles= "\n\t\"Roles\" : [";
            else roles= "<br>&emsp;\"Roles\" : [";
            if (!existingSISOwners.isEmpty()) {
                if(firstElem){
                    firstElem = false;
                }else{
                    roles += ",";
                }
                if (!fromWeb) roles += "\n\t\t";
                else roles += "<br>&emsp;&emsp;";
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
                if (!fromWeb) roles += "\n\t\t";
                else roles += "<br>&emsp;&emsp;";
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
                if (!fromWeb) roles += "\n\t\t";
                else roles += "<br>&emsp;&emsp;";
                roles += "\"Associate\"";
                response_code += "1";
            }
            else response_code += "0";

            if (!fromWeb) roles+="\n\t]";
            else roles+="<br>&emsp;]";
        }
        return responses.getResponse(response_code, roles, title);
    }

    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }
}