package gr.gunet.uLookup.routes;
import gr.gunet.uLookup.AcademicPerson;
import gr.gunet.uLookup.Conflict;
import gr.gunet.uLookup.LoginNameValidator;
import gr.gunet.uLookup.db.HRMSDBView;
import gr.gunet.uLookup.db.SISDBView;
import gr.gunet.uLookup.ldap.LdapManager;
import gr.gunet.uLookup.tools.CustomJsonReader;
import gr.gunet.uLookup.RequestPerson;
import gr.gunet.uLookup.ResponseMessages;
import gr.gunet.uLookup.db.DBConnectionPool;
import gr.gunet.uLookup.ldap.LdapConnectionPool;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import org.ldaptive.LdapEntry;
import spark.Request;
import spark.Response;
import spark.Route;

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
    String title;
    public LoginNameValidatorRoute(String institution) {
      this.institution= institution;
    }

    @Override
    public Object handle(Request req, Response res) {
        title="";
        responses= new ResponseMessages(req.session().attribute("web"));
        RequestPerson reqPerson;
        if (!req.session().attribute("authorized").equals("true")){
          String errorMessage= "You were not authorized";
          closeViews();
          return responses.getResponse("401", errorMessage, title);
        }
        responseCode="";

        if (!req.session().attribute("web").equals("true")){
          fromWeb=false;
          res.type("application/json");
          CustomJsonReader jsonReader = new CustomJsonReader(req.body());
          disabledGracePeriod= jsonReader.readPropertyAsString("disabledGracePeriod");
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
          disabledGracePeriod= req.queryParams("disabledGracePeriod");
          try{
            reqPerson = new RequestPerson(req);
          }catch(Exception e){
              e.printStackTrace(System.err);
              String errorMessage=e.getMessage();
              closeViews();
              return responses.getResponse("400", errorMessage, title);
          }
        }
        if(disabledGracePeriod == null || disabledGracePeriod.trim().equals("")){
            disabledGracePeriod = null;
        }
        else if (disabledGracePeriod.length()<3){
            LocalDate ld = java.time.LocalDate.now().minusMonths(Integer.parseInt(disabledGracePeriod));
            disabledGracePeriod= ld.toString();
            disabledGracePeriod= disabledGracePeriod.replace("-", "");
        }

        verbose=reqPerson.getVerbose();
        
        System.out.println("-----------------------------------------------------------");
        System.out.println();
        System.out.println("Request Date and Time: " + java.time.LocalDateTime.now());
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
        LoginNameValidator loginChecker = new LoginNameValidator(Views, ldapDS, disabledGracePeriod);

        Collection<String> UIDPersons;
        try{
            UIDPersons=loginChecker.getUIDPersons(reqPerson);
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
            return errorMessage(e,"DS");
        }

        Collection<Conflict> conflicts;
        responseContent = "";
        try{
            conflicts= loginChecker.checkForValidityConflicts(reqPerson);
            getConflicts(conflicts,reqPerson, loginChecker);

            System.out.println("-Response code: " + responseCode);
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse(responseCode, responseContent, title);
        }catch(Exception e){
            String errorSource= e.getMessage();
            return errorMessage(e,errorSource);
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
                        responseContent= responseContent.concat("[\n");
                    }else{
                        responseContent= responseContent.concat(",\n");
                    }
                    responseContent= responseContent.concat(conflict.toJson(fromWeb));
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
        Collection<LdapEntry> existingDSOwners = new LinkedList<>();
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("loginName", reqPerson.getLoginName());
        attributes.put("SSN", reqPerson.getSSN());
        attributes.put("ssnCountry", reqPerson.getSSNCountry());
        if (reqPerson.getTIN()!=null && reqPerson.getTINCountry()!=null){
            attributes.put("tin", reqPerson.getTIN());
            attributes.put("tinCountry", reqPerson.getTINCountry());
        }
        if (reqPerson.getBirthDate()!=null){
            attributes.put("birthDate", reqPerson.getBirthDate());
        }
        if (disabledGracePeriod!=null) attributes.put("disabledGracePeriod", disabledGracePeriod);

        SISDBView sis;
        HRMSDBView hrms,hrms2;
        LdapManager ldap;
        try {
            sis = Views.getSISConn();
            hrms = Views.getHRMSConn();
            hrms2 = Views.getHRMS2Conn();
            ldap = ldapDS.getConn();

            Collection<AcademicPerson> existingOwners;
            existingOwners = sis.fetchAll(attributes);
            if (hrms != null) existingOwners.addAll(hrms.fetchAll(attributes));
            if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(attributes));
            if (reqPerson.getSSNCountry().equals("GR"))
                existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + reqPerson.getSSN())));

            LinkedList<String> existingUserNames= new LinkedList<>();
            String foundNames="";
            if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) {
                foundNames+= ( "," + responses.formattedString("\"personPairedWith\": [", 1));
                boolean firstElem = true;
                if (!existingOwners.isEmpty()) {
                    for (AcademicPerson person : existingOwners) {
                        if (!existingUserNames.contains(person.getLoginName())) {
                            if (firstElem) firstElem=false;
                            else foundNames = foundNames.concat(",");
                            foundNames = foundNames.concat(responses.formattedString("\"" + person.getLoginName() + "\"", 2));
                            existingUserNames.add(person.getLoginName());
                        }
                    }
                }
                if (!existingDSOwners.isEmpty()) {
                    for (LdapEntry person : existingDSOwners) {
                        String uid = person.getAttribute("uid").getStringValue();
                        if (!existingUserNames.contains(uid)) {
                            if (firstElem) firstElem=false;
                            else foundNames = foundNames.concat(",");
                            foundNames = foundNames.concat(responses.formattedString("\"" + uid + "\"", 2));
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

    public String errorMessage(Exception e, String source){
        e.printStackTrace(System.err);
        closeViews();
        if (source!=null){
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the " + source + ".\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("500", source, title);
        }
        else{
            System.out.println("-Response code: 501");
            System.out.println("-message: An error has occurred");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("501", "An error has occurred.", title);
        }
    }

    public void closeViews(){
        DBConnectionPool.clean();
        LdapConnectionPool.clean();
    }
}
