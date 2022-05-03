package gr.gunet.uLookup.routes;
import com.google.gson.Gson;
import gr.gunet.uLookup.AcademicPerson;
import gr.gunet.uLookup.Conflict;
import gr.gunet.uLookup.LoginNameValidator;
import gr.gunet.uLookup.db.HRMSDBView;
import gr.gunet.uLookup.db.SISDBView;
import gr.gunet.uLookup.generator.UserNameGen;
import gr.gunet.uLookup.ldap.LdapManager;
import gr.gunet.uLookup.tools.CustomJsonReader;
import gr.gunet.uLookup.RequestPerson;
import gr.gunet.uLookup.ResponseMessages;
import gr.gunet.uLookup.db.DBConnectionPool;
import gr.gunet.uLookup.ldap.LdapConnectionPool;
import gr.gunet.uLookup.tools.PropertyReader;
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
    boolean fromWeb;
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    boolean verbose= false;
    String disabledGracePeriod=null;
    String responseCode;
    String responseContent;
    String institution;
    ResponseMessages responses;
    public LoginNameValidatorRoute(String institution) {
      this.institution= institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception{
        String title="";
        responses= new ResponseMessages(req.session().attribute("web"));
        RequestPerson reqPerson;
        if (!req.session().attribute("authorized").equals("true")){
          String errorMessage= "You were not authorized";
          closeViews();
          return responses.getResponse("401", errorMessage, title);
        }
        String htmlContent="";
        responseCode="";

        if (!req.session().attribute("web").equals("true")){
          fromWeb=false;
          res.type("application/json");
          CustomJsonReader jsonReader = new CustomJsonReader(req.body());
          String loginName= jsonReader.readPropertyAsString("loginName");
          
          disabledGracePeriod= jsonReader.readPropertyAsString("disabledGracePeriod");
          if(disabledGracePeriod == null || disabledGracePeriod.trim().equals("")){
            disabledGracePeriod = null;
          }
          else if (disabledGracePeriod.length()<3){
              LocalDate ld = java.time.LocalDate.now().minusMonths(Integer.parseInt(req.queryParams("disabledGracePeriod")));
              disabledGracePeriod= ld.toString();
              disabledGracePeriod= disabledGracePeriod.replace("-", "");
          }
          try{
              reqPerson = new RequestPerson(jsonReader);
          }catch(Exception e){
              e.printStackTrace(System.err);
              String errorMessage=e.getMessage();
              closeViews();
              return responses.getResponse("400", errorMessage, title);
          }
        }
        else{
          fromWeb=true;
          res.type("text/html");
          String loginName= req.queryParams("loginName");
          disabledGracePeriod= req.queryParams("disabledGracePeriod");

          if(disabledGracePeriod == null || disabledGracePeriod.trim().equals("")){
            disabledGracePeriod = null;
          }
          else if (disabledGracePeriod.length()<3){
              LocalDate ld = java.time.LocalDate.now().minusMonths(Integer.parseInt(req.queryParams("disabledGracePeriod")));
              disabledGracePeriod= ld.toString();
              disabledGracePeriod= disabledGracePeriod.replace("-", "");
          }

          try{
            reqPerson = new RequestPerson(req);
          }catch(Exception e){
              e.printStackTrace(System.err);
              String errorMessage=e.getMessage();
              closeViews();
              return responses.getResponse("400", errorMessage, title);
          }
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
                System.out.println("-Response code: 300");
                System.out.println("-message: \"" + uid + "already exists while not following the typical DS Account generation procedure\"");
                System.out.println("-----------------------------------------------------------");
                System.out.println();
                return responses.getResponse("300", "", title);
            }
        }
        catch(Exception e){
            e.printStackTrace(System.err);
            closeViews();
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the DS\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("500", "DS", title);
        }

        Collection<Conflict> conflicts;
        responseContent = "";
        try{
            conflicts= loginChecker.checkForValidityConflicts(reqPerson,disabledGracePeriod);
            getConflicts(conflicts,reqPerson, loginChecker);

            System.out.println("-Response code: " + responseCode);
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse(responseCode, responseContent, title);
        }catch(Exception e){
            e.printStackTrace(System.err);
            String errorSource= e.getMessage();
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the " + errorSource + "\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("500", errorSource, title);
        }
    }

    public void getConflicts(Collection<Conflict> conflicts, RequestPerson reqPerson, LoginNameValidator loginChecker) throws Exception{
        verbose=reqPerson.getVerbose();
        if (!conflicts.isEmpty()){
            responseCode+="2";
            if (verbose){
                responseContent+= ("," + responses.formattedString("\"conflicts\": ", 1));
                boolean firstElem = true;
                for(Conflict conflict : conflicts){
                    if(firstElem){
                        firstElem = false;
                        responseContent+="[\n";
                    }else{
                        responseContent += ",\n";
                    }
                    responseContent += conflict.toJson(fromWeb);
                }
                responseContent+= responses.formattedString("]", 1);
            }
        }
        else{
            Collection<String> nullAttributes;
            nullAttributes=loginChecker.getNullAttributes(reqPerson, disabledGracePeriod);
            if (nullAttributes!=null && !nullAttributes.isEmpty()){
                if (nullAttributes.contains("ssn") || nullAttributes.contains("ssnCountry")){
                    responseCode="310";
                    responseContent="";
                    return;
                }
            }
            responseCode+="1";
        }
        getExistingLoginNames(reqPerson, loginChecker, conflicts);
        if (responseCode.length()<3) responseCode+="0";
    }

    public void getExistingLoginNames(RequestPerson reqPerson, LoginNameValidator loginChecker, Collection<Conflict> conflicts){
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
                foundNames+= ( "," + responses.formattedString("\"personPairedWith\": [", 1));
                boolean firstElem = true;
                if (!existingOwners.isEmpty()) {
                    for (AcademicPerson person : existingOwners) {
                        if (!existingUserNames.contains(person.getLoginName())) {
                            if (firstElem) firstElem=false;
                            else foundNames += ",";
                            foundNames+= responses.formattedString("\"" + person.getLoginName() + "\"", 2);
                            existingUserNames.add(person.getLoginName());
                        }
                    }
                }
                if (!existingDSOwners.isEmpty()) {
                    for (LdapEntry person : existingDSOwners) {
                        String uid = person.getAttribute("uid").getStringValue();
                        if (!existingUserNames.contains(uid)) {
                            if (firstElem) firstElem=false;
                            else foundNames += ",";
                            foundNames+= responses.formattedString("\"" + uid + "\"", 2);
                            existingUserNames.add(uid);
                        }
                    }
                }
                if (existingUserNames.contains(reqPerson.getLoginName())) { //
                    responseCode+="0";
                    if (conflicts.isEmpty()){
                        Collection<String> loginNameSources;
                        loginNameSources = loginChecker.getLoginNameSources(reqPerson, disabledGracePeriod);
                        if (!loginNameSources.contains("DS")) responseCode+="1"; //101
                        else responseCode+="2"; //102
                    }
                    else responseCode+="0"; //200
                }
                else{
                    responseCode += "1"; //X10
                    foundNames+= responses.formattedString("]", 1);
                    responseContent += foundNames;
                }
            }
            else {
                if (conflicts.isEmpty()) responseCode += "00"; //100
                else responseCode+="20"; //220
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
