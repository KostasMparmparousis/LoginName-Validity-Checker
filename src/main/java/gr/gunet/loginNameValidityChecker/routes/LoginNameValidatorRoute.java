package gr.gunet.loginNameValidityChecker.routes;
import com.google.gson.Gson;
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
import gr.gunet.loginNameValidityChecker.tools.PropertyReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.*;

import org.ldaptive.LdapEntry;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class LoginNameValidatorRoute implements Route{
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    boolean verbose= false;
    String disabledGracePeriod=null;
    String response_code;
    String message;
    String responseJson;
    private String CONN_FILE_DIR = "/etc/v_vd/conn/";
    String institution;

    public LoginNameValidatorRoute() {
    }
    @Override
    public Object handle(Request req, Response res) throws Exception{
        response_code="";
        String loginName= req.queryParams("loginName");
        String htmlResponse= "<html><head><meta charset=\"ISO-8859-1\"><title>Response</title><link rel=\"stylesheet\" href=\"../css/style.css\"></head><body>";
        htmlResponse+="<header><h1 style=\"color: #ed7b42;\">Response</h1></header><hr class=\"new1\"><div class=\"sidenav\"><a href=\"../index.html\">Main Hub</a><a href=\"../validator.html\">Validator</a><a href=\"../suggester.html\">Suggester</a><a href=\"../roleFinder.html\">Finder</a></div><div class=\"main\">";
        disabledGracePeriod= req.queryParams("disabledGracePeriod");
        if(disabledGracePeriod == null || disabledGracePeriod.trim().equals("")){
          disabledGracePeriod = null;
        }
        else if (disabledGracePeriod.length()<3){
            LocalDate ld = java.time.LocalDate.now().minusMonths(Integer.parseInt(req.queryParams("disabledGracePeriod")));
            disabledGracePeriod= ld.toString();
            disabledGracePeriod= disabledGracePeriod.replace("-", "");
        }
        institution= req.session().attribute("institution");

        RequestPerson reqPerson;
        try{
            reqPerson = new RequestPerson(req);
        }catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            String errorJson="{<br>&emsp;\"Response code\" : 400,<br>" +
                    "&emsp;\"message\" : \""+e.getMessage()+"\"<br>}<br>";
            htmlResponse+=errorJson;
            htmlResponse+="</div></body></html>";
            return htmlResponse;
        }

        verbose=reqPerson.getVerbose();
        System.out.println("-----------------------------------------------------------");
        System.out.println();
        System.out.println("Rquest Date and Time: " + java.time.LocalDateTime.now());
        System.out.println();
        System.out.println("Request Attributes: ");
        System.out.println("-SSN: "+ reqPerson.getSSN());
        System.out.println("-SSNCountry: "+ reqPerson.getSSNCountry());
        System.out.println("-TIN: "+ reqPerson.getTIN());
        System.out.println("-TINCountry: "+ reqPerson.getTINCountry());
        System.out.println("-birthDate: " + reqPerson.getBirthDate());
        System.out.println("-disabled Grace Period: " + disabledGracePeriod);
        System.out.println("-loginName: " + reqPerson.getLoginName());
        System.out.println("-institution: " + institution);
        if (verbose) System.out.println("-verbose: YES");
        else System.out.println("-verbose: NO");
        System.out.println();
        System.out.println("Response: ");

        Views= new DBConnectionPool(institution);
        ldapDS= new LdapConnectionPool(institution);
        LoginNameValidator loginChecker = new LoginNameValidator(Views, ldapDS);

        Collection<String> UIDPersons;
        try{
            UIDPersons=loginChecker.getUIDPersons(reqPerson, disabledGracePeriod);
            if (!UIDPersons.isEmpty()){
                String uid= UIDPersons.iterator().next();
                String warningJson="{<br>&emsp;\"Response code\" : 300, <br>"+
                        "&emsp;\"message\" : \"" + uid + " already exists while not following the typical DS Account generation procedure\"<br>}";
                htmlResponse+=warningJson;
                htmlResponse+="</div></body></html>";
                System.out.println("-Response code: 300");
                System.out.println("-message: \"" + uid + "already exists while not following the typical DS Account generation procedure\"");
                System.out.println("-----------------------------------------------------------");
                System.out.println();
                return htmlResponse;
            }
        }
        catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            String errorJson="{<br>&emsp;\"Response code\" : 500,<br>" +"&emsp;\"message\" : \"Could not connect to the DS\"<br>}<br>";
            htmlResponse+=errorJson;
            htmlResponse+="</div></body></html>";
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the DS\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return htmlResponse;
        }

        Collection<Conflict> conflicts;
        responseJson = "";
        try{
            conflicts= loginChecker.checkForValidityConflicts(reqPerson,disabledGracePeriod);
            responseJson+=getConflicts(conflicts,reqPerson, loginChecker);
            htmlResponse+=responseJson;
            htmlResponse+="</div></body></html>";
            return htmlResponse;
        }catch(Exception e){
            e.printStackTrace(System.err);
            String errorSource= e.getMessage();
            String errorJson="{<br>&emsp;\"Response code\" : 500,<br>" +"&emsp;\"message\" : \"Could not connect to the " + errorSource + "\"<br>}<br>";
            htmlResponse+=errorJson;
            htmlResponse+="</div></body></html>";
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the " + errorSource + "\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return htmlResponse;
        }
    }

    public boolean institutionExists(String institution){
      Path path= Paths.get(CONN_FILE_DIR+ institution + ".properties");
      if (!Files.exists(path)) {
        return false;
      }
      return true;
    }
    
    public String getConflicts(Collection<Conflict> conflicts, RequestPerson reqPerson, LoginNameValidator loginChecker) throws Exception{
        message=",<br>&emsp;\"message\" : \"";
        if (conflicts.isEmpty()) {
            message += "No ";
        }
        message += "Conflicts Found";
        String conflictsJson="{";
        verbose=reqPerson.getVerbose();

        if (!conflicts.isEmpty()) {
            response_code+="2";
            if (verbose){
                responseJson += ",<br>&emsp;\"conflicts\": " ;
                boolean firstElem = true;
                for(Conflict conflict : conflicts){
                    if(firstElem){
                        firstElem = false;
                        responseJson+="[<br>";
                    }else{
                        responseJson += ",<br>";
                    }
                    responseJson += conflict.toJson();
                }
                responseJson += "<br>&emsp;]";
            }
        }
        else response_code+="1";
        getExistingLoginNames(reqPerson);
        
        Collection<String> nullAttributes;
        Collection<String> loginNameSources;
        if (conflicts.isEmpty()){
            nullAttributes=loginChecker.getNullAttributes(reqPerson, disabledGracePeriod);
            if (nullAttributes!=null && !nullAttributes.isEmpty()){
                if (nullAttributes.contains("ssn") || nullAttributes.contains("ssnCountry")){
                    String warningJson="{<br>&emsp;\"Response code\" : 310,<br>" + "&emsp;\"message\" : \"A primary identifier given in the Request (ssn or ssnCountry) was never matched by any Record that is paired with " + reqPerson.getLoginName()  +". We can not safely assume they are the same person.\" <br>}";
                    return warningJson;
                }
                else{
                    message+=", some Request attributes were not matched to their Database counterparts by any Record that is paired with " + reqPerson.getLoginName();
                    String nullAttrs= ",<br>&emsp;\"unmatchedAttrs\": ";
                    boolean firstElem=true;
                    for (String nullAttr: nullAttributes){
                        if (firstElem==true){
                            firstElem=false;
                            nullAttrs+="[<br>";
                        }
                        else{
                            nullAttrs+=",<br>";
                        }
                        nullAttrs+="&emsp;&emsp;\"" + nullAttr + "\"";
                    }
                    nullAttrs += "<br>&emsp;]<br>";
                    responseJson+=nullAttrs;
                }
            }
            loginNameSources = loginChecker.getLoginNameSources(reqPerson, disabledGracePeriod);
            String status= ",<br>&emsp;\"status\": ";
            if (loginNameSources==null || loginNameSources.isEmpty()){
              response_code+="0";
              status+="\"Selection of a new loginName.\"";
            }
            else{
              if (!loginNameSources.contains("DS")){
                response_code+="1";
                status+="\"loginName already reserved for that person.\"";
              }
              else{
                response_code+="2";
                status+="\"loginName already assigned to that person.\"";
              }
            }
            responseJson+=status;
        }
        else{
            response_code+="0";
        }

        conflictsJson+="<br>&emsp;\"Response code\" : " + response_code;
        message+="\"";
        conflictsJson+=message;
        conflictsJson+=responseJson;
        if (responseJson.equals("")) conflictsJson+="\"";
        conflictsJson+="<br>}<br>";

        System.out.println("-Response code: " + response_code);
        System.out.println("-----------------------------------------------------------");
        System.out.println();

        return conflictsJson;
    }

    public void getExistingLoginNames(RequestPerson reqPerson){
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
                foundNames= ",<br>&emsp;\"personPairedWith\": [";
                boolean firstElem = true;
                if (!existingOwners.isEmpty()) {
                    for (AcademicPerson person : existingOwners) {
                        if (!existingUserNames.contains(person.getLoginName())) {
                            if (firstElem) firstElem = false;
                            else foundNames += ",";
                            foundNames += "<br>&emsp;&emsp;";
                            foundNames += "\"" + person.getLoginName() + "\"";
                            existingUserNames.add(person.getLoginName());
                        }
                    }
                }
                if (!existingDSOwners.isEmpty()) {
                    for (LdapEntry person : existingDSOwners) {
                        String uid = person.getAttribute("uid").getStringValue();
                        if (!existingUserNames.contains(uid)) {
                            if (firstElem) firstElem = false;
                            else foundNames += ",";
                            foundNames += "<br>&emsp;&emsp;";
                            foundNames += "\"" + uid + "\"";
                            existingUserNames.add(uid);
                        }
                    }
                }
                if (existingUserNames.contains(reqPerson.getLoginName())) {
                    response_code+="0";
                    responseJson+="";
                    return;
                }
                else {
                    response_code += "1";
                    message += ", " + reqPerson.getSSN() + "-" + reqPerson.getSSNCountry() + " is already paired with at least 1 loginName";
                    foundNames += "<br>&emsp;]";
                    responseJson += foundNames;
                }
            }else{
                response_code+="2";
                message+= ", " + reqPerson.getSSN() + "-" + reqPerson.getSSNCountry() + " combination not found in any Database";
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
