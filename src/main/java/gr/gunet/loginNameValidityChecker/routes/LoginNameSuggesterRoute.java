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
import gr.gunet.loginNameValidityChecker.tools.PropertyReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.ldaptive.LdapEntry;

import java.util.Collection;
import java.util.Vector;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginNameSuggesterRoute implements Route {
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    String SSN;
    String SSNCountry;
    String FN;
    String LN;
    String institution;
    String disabledGracePeriod=null;
    String response_code;
    String message;
    String responseJson;
    String personPairedWith;
    String suggestedNames;
    private String CONN_FILE_DIR = "/etc/v_vd/conn/";
    public LoginNameSuggesterRoute() {
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        response_code="";
        message="";
        responseJson="";
        personPairedWith="";
        suggestedNames="";
        String htmlResponse= "<html><head><meta charset=\"ISO-8859-1\"><title>Response</title><link rel=\"stylesheet\" href=\"../css/style.css\"></head><body>";
        htmlResponse+="<header><h1 style=\"color: #ed7b42;\">Response</h1></header><hr class=\"new1\"><div class=\"sidenav\"><a href=\"../index.html\">Main Hub</a><a href=\"../validator.html\">Validator</a><a href=\"../suggester.html\">Suggester</a><a href=\"../roleFinder.html\">Finder</a></div><div class=\"main\">";
        
        institution= req.session().attribute("institution");
        SSN = req.queryParams("ssn");
        SSNCountry = req.queryParams("ssnCountry");
        FN= req.queryParams("firstName");
        LN= req.queryParams("lastName");

        Views= new DBConnectionPool(institution);
        ldapDS= new LdapConnectionPool(institution);

        Collection<AcademicPerson> existingOwners = new Vector<AcademicPerson>();
        Collection<LdapEntry> existingDSOwners = new Vector<LdapEntry>();

        String foundJson= "{";
        message="";
        String errorJson="";

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;

        if (SSN.trim().equals("") || SSNCountry.trim().equals("")){
          response_code+="3";
          message= "<br>&emsp;\"message\":\"";
          if (SSN.trim().equals("")) message+= "SSN not given, ";
          if (SSNCountry.trim().equals("")) message+="SSNCountry not given, ";
          int exit_code= generateNames();
          String returnJson="{";
          response_code+="0";
          returnJson+= "<br>&emsp;\"Response code\" : " + response_code+ ",";
          returnJson+=message;
          responseJson+=suggestedNames;
          returnJson+=responseJson;
          returnJson+="<br>}";
          res.body(new Gson().toJson(returnJson));
          htmlResponse+=returnJson;
          htmlResponse+="</div></body></html>";
          return htmlResponse;
        }

        try{
          sis = Views.getSISConn();
          existingOwners.addAll(sis.fetchAll(SSN, SSNCountry));
        }
        catch (Exception e){
          errorJson="{<br>&emsp;\"Response code\" : 500,<br>" +"&emsp;\"message\" : \"Could not connect to the SIS\"<br>}<br>";
        }

        try{
          hrms = Views.getHRMSConn();
          if (hrms != null) existingOwners.addAll(hrms.fetchAll(SSN, SSNCountry));
        }
        catch (Exception e){
          errorJson="{<br>&emsp;\"Response code\" : 500,<br>" +"&emsp;\"message\" : \"Could not connect to the HRMS\"<br>}<br>";
        }

        try{
          hrms2 = Views.getHRMS2Conn();
          if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(SSN, SSNCountry));
        }
        catch (Exception e){
          errorJson="{<br>&emsp;\"Response code\" : 500,<br>" +"&emsp;\"message\" : \"Could not connect to the HRMS2\"<br>}<br>";
        }

        try{
          ldap = ldapDS.getConn();
          if (SSNCountry.equals("GR")) existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + SSN)));
        }
        catch (Exception e){
          errorJson="{<br>&emsp;\"Response code\" : 500,<br>" +"&emsp;\"message\" : \"Could not connect to the DS\"<br>}<br>";
        }

        if (!errorJson.equals("")){
          htmlResponse+=errorJson;
          htmlResponse+="</div></body></html>";
          return htmlResponse;
        }

        Vector<String> existingUserNames= new Vector<String>();
        if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) {
            response_code+="1";

            personPairedWith= "<br>&emsp;\"personPairedWith\": [";

            boolean firstElem = true;
            if (!existingOwners.isEmpty()) {
                for (AcademicPerson person : existingOwners) {
                    if (!existingUserNames.contains(person.getLoginName())) {
                        if (firstElem) firstElem = false;
                        else personPairedWith += ",";
                        personPairedWith += "<br>&emsp;&emsp;";
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
                        personPairedWith += "<br>&emsp;&emsp;";
                        personPairedWith += "\"" + uid + "\"";
                        existingUserNames.add(uid);
                    }
                }
            }
            personPairedWith += "<br>&emsp;]";
            message= "<br>&emsp;\"message\": \"" + SSN + "-" + SSNCountry + " is already paired with at least 1 loginName, ";
        }else{
            response_code+="2";
            message= "<br>&emsp;\"message\": \"" + SSN + "-" + SSNCountry + " combination not found in any Database, ";
        }
        int exit_code= generateNames();
        responseJson+=personPairedWith;
        responseJson+=suggestedNames;
        String returnJson="{";
        response_code+="0";
        returnJson+= "<br>&emsp;\"Response code\" : " + response_code+ ",";
        returnJson+=message;
        returnJson+=responseJson;
        returnJson+="<br>}";
        htmlResponse+=returnJson;
        htmlResponse+="</div></body></html>";
        return htmlResponse;
    }

    public boolean institutionExists(String institution){
      Path path= Paths.get(CONN_FILE_DIR+ institution + ".properties");
      if (!Files.exists(path)) {
        return false;
      }
      return true;
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

            if (FN.trim().equals("") || LN.trim().equals("")){
                if (!SSN.trim().equals("") && !SSNCountry.trim().equals("")){
                    existingOwners.addAll(sis.fetchAll(SSN, SSNCountry));
                    if (hrms != null) existingOwners.addAll(hrms.fetchAll(SSN, SSNCountry));
                    if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(SSN, SSNCountry));
                    if ( SSNCountry.equals("GR") ){
                        existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + SSN)));
                    }
                    if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()){
                        if (!existingOwners.isEmpty()) loginGen= new UserNameGen(existingOwners.iterator().next());
                        else loginGen= new UserNameGen(existingDSOwners.iterator().next());
                    }
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
                suggestedNames = "<br>&emsp;\"suggestions\":&emsp;[";
                for(String login : proposedNames){
                    if (loginGen.checkIfUserNameExists(login, Views, ldapDS, disabledGracePeriod)) continue;
                    if(firstElem){
                        firstElem = false;
                    }else{
                        suggestedNames += ",";
                    }
                    suggestedNames +="<br>&emsp;&emsp;";
                    suggestedNames += "\""+login+"\"";
                }
                suggestedNames+="<br>&emsp;]";
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
