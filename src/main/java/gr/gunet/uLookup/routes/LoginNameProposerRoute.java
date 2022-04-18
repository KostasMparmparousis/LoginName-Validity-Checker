package gr.gunet.uLookup.routes;
import com.google.gson.Gson;
import gr.gunet.uLookup.RequestPerson;
import gr.gunet.uLookup.generator.UserNameGen;
import gr.gunet.uLookup.tools.CustomJsonReader;
import gr.gunet.uLookup.AcademicPerson;
import gr.gunet.uLookup.ResponseMessages;
import gr.gunet.uLookup.db.HRMSDBView;
import gr.gunet.uLookup.db.SISDBView;
import gr.gunet.uLookup.db.DBConnectionPool;
import gr.gunet.uLookup.ldap.LdapManager;
import gr.gunet.uLookup.ldap.LdapConnectionPool;
import gr.gunet.uLookup.tools.PropertyReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.ldaptive.LdapEntry;

import java.util.Collection;
import java.util.Vector;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoginNameProposerRoute implements Route {
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
    ResponseMessages responses;
    public LoginNameProposerRoute(String institution) {
      this.institution= institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        responses=new ResponseMessages();
        if (!req.session().attribute("authorized").equals("true")){
          String errorMessage= "You were not authorized";
          closeViews();
          return responses.getValidatorResponse("401", errorMessage);
        }
        ResponseMessages responses= new ResponseMessages();
        CustomJsonReader jsonReader = new CustomJsonReader(req.body());
        response_code="";
        message="";
        responseJson="";
        personPairedWith="";
        suggestedNames="";
        String response="";

        SSN = jsonReader.readPropertyAsString("ssn");
        SSNCountry = jsonReader.readPropertyAsString("ssnCountry");
        FN= jsonReader.readPropertyAsString("firstName");
        LN= jsonReader.readPropertyAsString("lastName");

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

        if (!findErrors().equals("")){
          String returnJson="{";
          response_code="400";
          message= "\n\t\"message\": \"" + findErrors() + "\"";
          returnJson+= "\n\t\"Response code\" : " + response_code+ ",";
          returnJson+=message;
          returnJson+="\n}";
          response=returnJson;
          return response;
        }
        if (SSN.trim().equals("") || SSNCountry.trim().equals("")){
          response_code+="3";
          message= "\n\t\"message\":\"";
          if (SSN.trim().equals("")) message+= "SSN not given, ";
          if (SSNCountry.trim().equals("")) message+="SSNCountry not given, ";
          int exit_code= generateNames();
          String returnJson="{";
          response_code+="0";
          returnJson+= "\n\t\"Response code\" : " + response_code+ ",";
          returnJson+=message;
          responseJson+=suggestedNames;
          returnJson+=responseJson;
          returnJson+="\n}";
          response+=returnJson;
          return response;
        }

        try{
          sis = Views.getSISConn();
          existingOwners.addAll(sis.fetchAll(SSN, SSNCountry));
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            errorJson="{\n\t\"Response code\" : 500," +"\n\t\"message\" : \"Could not connect to the SIS\"\n}\n";
        }

        try{
          hrms = Views.getHRMSConn();
          if (hrms != null) existingOwners.addAll(hrms.fetchAll(SSN, SSNCountry));
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            errorJson="{\n\t\"Response code\" : 500," +"\n\t\"message\" : \"Could not connect to the HRMS\"\n}\n";
        }

        try{
          hrms2 = Views.getHRMS2Conn();
          if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(SSN, SSNCountry));
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            errorJson="{\n\t\"Response code\" : 500," +"\n\t\"message\" : \"Could not connect to ELKE\"\n}\n";
        }

        try{
          ldap = ldapDS.getConn();
          if (SSNCountry.equals("GR")) existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + SSN)));
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            errorJson="{\n\t\"Response code\" : 500," +"\n\t\"message\" : \"Could not connect to the DS\"\n}\n";
        }

        if (!errorJson.equals("")){
          response+=errorJson;
          return response;
        }

        Vector<String> existingUserNames= new Vector<String>();
        if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) {
            response_code+="1";
            personPairedWith= "\n\t\"personPairedWith\": [";
            boolean firstElem = true;
            if (!existingOwners.isEmpty()) {
                for (AcademicPerson person : existingOwners) {
                    if (!existingUserNames.contains(person.getLoginName())) {
                        if (firstElem) firstElem = false;
                        else personPairedWith += ",";
                        personPairedWith += "\n\t\t";
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
                        personPairedWith += "\n\t\t";
                        personPairedWith += "\"" + uid + "\"";
                        existingUserNames.add(uid);
                    }
                }
            }
            personPairedWith += "\n\t]";
            message= "\n\t\"message\": \"" + SSN + "-" + SSNCountry + " is already paired with at least 1 loginName, ";
        }else{
            response_code+="2";
            message= "\n\t\"message\": \"" + SSN + "-" + SSNCountry + " combination not found in any Database, ";
        }
        int exit_code= generateNames();
        responseJson+=personPairedWith;
        responseJson+=suggestedNames;
        String returnJson="{";
        response_code+="0";
        returnJson+= "\n\t\"Response code\" : " + response_code+ ",";
        returnJson+=message;
        returnJson+=responseJson;
        returnJson+="\n}";
        response+=returnJson;
        return response;
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
      else if (!Name.matches("[a-z0-9]+")){
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
                suggestedNames = "\n\t\"suggestions\":\t[";
                for(String login : proposedNames){
                    if (loginGen.checkIfUserNameExists(login, Views, ldapDS, disabledGracePeriod)) continue;
                    if(firstElem){
                        firstElem = false;
                    }else{
                        suggestedNames += ",";
                    }
                    suggestedNames +="\n\t\t";
                    suggestedNames += "\""+login+"\"";
                }
                suggestedNames+="\n\t]";
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
