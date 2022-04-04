package gr.gunet.loginNameValidityChecker.routes;

import com.google.gson.Gson;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import gr.gunet.loginNameValidityChecker.ResponseMessages;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import gr.gunet.loginNameValidityChecker.db.HRMSDBView;
import gr.gunet.loginNameValidityChecker.db.SISDBView;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;
import gr.gunet.loginNameValidityChecker.ldap.LdapManager;
import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import gr.gunet.loginNameValidityChecker.tools.PropertyReader;
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
    public RoleFinderRoute() {
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        ResponseMessages responses= new ResponseMessages();
        String roleJson= "{";
        String message="";
        String roles="";
        String response_code="";

//        PropertyReader propReader= new PropertyReader(CONN_FILE_DIR+"/institution.properties");
//        institution= propReader.getProperty("institution");
        institution= req.session().attribute("institution");
        String htmlResponse= "<html><head><meta charset=\"ISO-8859-1\"><title>Response</title><link rel=\"stylesheet\" href=\"../css/style.css\"></head><body>";
        htmlResponse+="<header><h1 style=\"color: #ed7b42;\">Response</h1></header><hr class=\"new1\"><div class=\"sidenav\"><a href=\"../index.html\">Main Hub</a><a href=\"../validator.html\">Validator</a><a href=\"../proposer.html\">Proposer</a><a href=\"../roleFinder.html\">Finder</a></div><div class=\"main\">";

        loginName = req.queryParams("loginName");
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
          message= "<br>&emsp;\"message\": \"" + errorDescription + "\"";
          roleJson+= "<br>&emsp;\"Response code\" : " + response_code + ",";
          roleJson+=message;
          htmlResponse+=roleJson;
          htmlResponse+="<br>}</div></body></html>";
          return htmlResponse;
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
            errorJson="{<br>&emsp;\"Response code\" : 500,<br>" +"  \"message\" : \"Could not connect to the SIS\"<br>}<br>";
        }

        try{
          hrms = Views.getHRMSConn();
          if (hrms!=null) existingHRMSOwners= hrms.fetchAll("loginName", loginName, onlyActive);
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            errorJson="{<br>&emsp;\"Response code\" : 500,<br>" +"  \"message\" : \"Could not connect to the HRMS\"<br>}<br>";
        }

        try{
          hrms2 = Views.getHRMS2Conn();
          if (hrms2!=null) existingHRMS2Owners= hrms2.fetchAll("loginName", loginName, onlyActive);
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            errorJson="{<br>&emsp;\"Response code\" : 500,<br>" +"  \"message\" : \"Could not connect to the HRMS2\"<br>}<br>";
        }

        if (!errorJson.equals("")){
          htmlResponse+=errorJson;
          htmlResponse+="</div></body></html>";
          return htmlResponse;
        }

        if (existingSISOwners.isEmpty() && existingHRMSOwners.isEmpty() && existingHRMS2Owners.isEmpty()){
            response_code="000";
            message= "<br>&emsp;\"message\": \"" + loginName + " not found in any Database\"";
        }
        else {
            response_code="";
            boolean firstElem = true;
            message= "<br>&emsp;\"message\": \"" + loginName + " found\",";
            roles= "<br>&emsp;\"Roles\" : [";
            if (!existingSISOwners.isEmpty()) {
                if(firstElem){
                    firstElem = false;
                }else{
                    roles += ",";
                }
                roles += "<br>&emsp;&emsp;";
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
                roles += "<br>&emsp;&emsp;";
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
                roles += "<br>&emsp;&emsp;";
                roles += "\"Associate\"";
                response_code += "1";
            }
            else response_code += "0";

            roles+="<br>&emsp;]";
        }
        roleJson+= "<br>&emsp;\"Response code\" : " + response_code + ",";
        roleJson+=message;
        roleJson+=roles;
        roleJson+="<br>}<br>";
        htmlResponse+=roleJson;
        htmlResponse+="</div></body></html>";
        return htmlResponse;
    }

    public void closeViews(){
        Views.clean();
        ldapDS.clean();
    }
}