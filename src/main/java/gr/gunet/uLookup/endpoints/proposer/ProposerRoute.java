package gr.gunet.uLookup.endpoints.proposer;
import gr.gunet.uLookup.tools.generator.UserNameGen;
import gr.gunet.uLookup.tools.parsers.CustomJsonParser;
import gr.gunet.uLookup.db.personInstances.AcademicPerson;
import gr.gunet.uLookup.tools.ResponseMessages;
import gr.gunet.uLookup.db.HRMSDBView;
import gr.gunet.uLookup.db.SISDBView;
import gr.gunet.uLookup.db.DBConnectionPool;
import gr.gunet.uLookup.db.ldap.LdapManager;
import gr.gunet.uLookup.db.ldap.LdapConnectionPool;
import org.ldaptive.LdapEntry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import spark.Request;
import spark.Response;
import spark.Route;

public class ProposerRoute implements Route {
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    boolean fromWeb;
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
    ResponseMessages responses;
    String title;
    public ProposerRoute(String institution) {
      this.institution= institution;
    }

    @Override
    public Object handle(Request req, Response res) {
        responses= new ResponseMessages(req.session().attribute("web"));
        title="Suggested LoginNames";
        response_code="";
        message="";
        responseJson="";
        personPairedWith="";
        suggestedNames="";
        if (!req.session().attribute("authorized").equals("true")){
          String errorMessage= "You were not authorized";
          closeViews();
          return responses.getResponse("401", errorMessage, title);
        }
        if (!req.session().attribute("web").equals("true")){
          fromWeb=false;
          res.type("application/json");
          CustomJsonParser jsonReader = new CustomJsonParser(req.body());
          SSN = jsonReader.readPropertyAsString("ssn");
          SSNCountry = jsonReader.readPropertyAsString("ssnCountry");
          FN= jsonReader.readPropertyAsString("firstName");
          LN= jsonReader.readPropertyAsString("lastName");
        }
        else{
          fromWeb=true;
          res.type("text/html");
          SSN = req.queryParams("ssn");
          SSNCountry = req.queryParams("ssnCountry");
          FN= req.queryParams("firstName");
          LN= req.queryParams("lastName");
        }

        Views= new DBConnectionPool(institution);
        ldapDS= new LdapConnectionPool(institution);

        Collection<AcademicPerson> existingOwners;
        Collection<LdapEntry> existingDSOwners = new Vector<>();

        message="";
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("SSN", SSN);
        attributes.put("ssnCountry", SSNCountry);

        if (!findErrors().equals("")){
          return responses.getResponse("400", findErrors(), title);
        }
        if (SSN.trim().equals("") || SSNCountry.trim().equals("")){
          response_code+="3";
          generateNames();
          response_code+="0";
          return responses.getResponse(response_code, suggestedNames, title);
        }

        try{
            SISDBView sis = Views.getSISConn();
            existingOwners = new Vector<>(sis.fetchAll(attributes));
        }
        catch (Exception e){
            return errorMessage(e,"SIS");
        }

        try{
            HRMSDBView hrms = Views.getHRMSConn();
            if (hrms != null) existingOwners.addAll(hrms.fetchAll(attributes));
        }
        catch (Exception e){
            return errorMessage(e,"HRMS");
        }

        try{
            HRMSDBView hrms2 = Views.getHRMS2Conn();
            if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(attributes));
        }
        catch (Exception e){
            return errorMessage(e,"ELKE");
        }

        try{
            LdapManager ldap = ldapDS.getConn();
            if (SSNCountry.equals("GR")) existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + SSN)));
        }
        catch (Exception e){
            return errorMessage(e,"DS");
        }

        Vector<String> existingUserNames= new Vector<>();
        if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) {
            response_code+="1";
            if (!fromWeb) personPairedWith= ",\n\t\"personPairedWith\": [";
            else personPairedWith= ",<br>&emsp;\"personPairedWith\": [";
            boolean firstElem = true;
            if (!existingOwners.isEmpty()) {
                for (AcademicPerson person : existingOwners) {
                    if (!existingUserNames.contains(person.getLoginName())) {
                        if (firstElem) firstElem = false;
                        else personPairedWith = personPairedWith.concat(",");
                        if (!fromWeb) personPairedWith = personPairedWith.concat("\n\t\t");
                        else personPairedWith = personPairedWith.concat("<br>&emsp;&emsp;");
                        personPairedWith = personPairedWith.concat("\"" + person.getLoginName() + "\"");
                        existingUserNames.add(person.getLoginName());
                    }
                }
            }
            if (!existingDSOwners.isEmpty()) {
                for (LdapEntry person : existingDSOwners) {
                    String uid = person.getAttribute("uid").getStringValue();
                    if (!existingUserNames.contains(uid)) {
                        if (firstElem) firstElem = false;
                        else personPairedWith = personPairedWith.concat( ",");
                        if (!fromWeb) personPairedWith = personPairedWith.concat("\n\t\t");
                        else personPairedWith = personPairedWith.concat("<br>&emsp;&emsp;");
                        personPairedWith = personPairedWith.concat("\"" + uid + "\"");
                        existingUserNames.add(uid);
                    }
                }
            }
            if (!fromWeb) personPairedWith = personPairedWith.concat("\n\t]");
            else personPairedWith = personPairedWith.concat("<br>&emsp;]");
        }else{
            response_code+="2";
        }
        generateNames();
        responseJson+=personPairedWith;
        responseJson+=suggestedNames;
        response_code+="0";
        return responses.getResponse(response_code, responseJson, title);
    }
    
    public String findErrors(){
      if (!findErrors(FN).equals("")) return findErrors(FN);
      if (!findErrors(LN).equals("")) return findErrors(LN);
      return "";
    }
    
    public String findErrors(String Name){
      if(Name == null || Name.trim().equals("")){
         return "";
      }
      else if (Name.length()>1 && !Name.equals(Name.trim())){
        return "Whitespace character found.";
      }
      else if (Name.length() < 2 || Name.length() > 20){
        return "Name length outside character limits.";
      }
      else if (!Name.matches("[a-z\\d]+")){
        for(int i=0;i<Name.length();i++){
            char ch = Name.charAt(i);
            if(Character.isUpperCase(ch)){
                return "Capital character found.";
            }
        }
        return "Invalid Name format.";
      }
      return "";
    }

    public void generateNames(){
        Collection<LdapEntry> existingDSOwners = new Vector<>();
        Vector<String> proposedNames = new Vector<>();
        UserNameGen loginGen = null;
        boolean firstElem = true;
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("SSN", SSN);
        attributes.put("ssnCountry", SSNCountry);
        try {
            SISDBView sis = Views.getSISConn();
            HRMSDBView hrms = Views.getHRMSConn();
            HRMSDBView hrms2 = Views.getHRMS2Conn();
            LdapManager ldap = ldapDS.getConn();


            if (FN.trim().equals("") || LN.trim().equals("")){
                if (!SSN.trim().equals("") && !SSNCountry.trim().equals("")){
                    Collection<AcademicPerson> existingOwners = new Vector<>(sis.fetchAll(attributes));
                    if (hrms != null) existingOwners.addAll(hrms.fetchAll(attributes));
                    if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(attributes));
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
                personPairedWith+=",";
                if (!fromWeb) suggestedNames = "\n\t\"suggestions\":\t[";
                else suggestedNames = "<br>&emsp;\"suggestions\":&emsp;[";
                for(String login : proposedNames){
                    if (loginGen.checkIfUserNameExists(login, Views, ldapDS, disabledGracePeriod)) continue;
                    if(firstElem){
                        firstElem = false;
                    }else{
                        suggestedNames= suggestedNames.concat(",");
                    }
                    if (!fromWeb) suggestedNames= suggestedNames.concat("\n\t\t");
                    else suggestedNames= suggestedNames.concat("<br>&emsp;&emsp;");
                    suggestedNames= suggestedNames.concat("\""+login+"\"");
                }
                if (!fromWeb) suggestedNames= suggestedNames.concat("\n\t]");
                else suggestedNames= suggestedNames.concat("<br>&emsp;]");
            }
            else{
              response_code+="1";
              message+= "Generator did not manage to create suggested names\"";
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
