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
    public LoginNameProposerRoute(String institution) {
      this.institution= institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        responses= new ResponseMessages(req.session().attribute("web"));
        String title="Suggested LoginNames";
        String response="";
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
          CustomJsonReader jsonReader = new CustomJsonReader(req.body());
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
          return responses.getResponse("400", findErrors(), title);
        }
        if (SSN.trim().equals("") || SSNCountry.trim().equals("")){
          response_code+="3";
          int exit_code= generateNames();
          response_code+="0";
          return responses.getResponse(response_code, suggestedNames, title);
        }

        try{
          sis = Views.getSISConn();
          existingOwners.addAll(sis.fetchAll(SSN, SSNCountry));
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
          if (hrms != null) existingOwners.addAll(hrms.fetchAll(SSN, SSNCountry));
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
          if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(SSN, SSNCountry));
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            closeViews();
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the HRMS\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("500", "ELKE", title);
        }

        try{
          ldap = ldapDS.getConn();
          if (SSNCountry.equals("GR")) existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + SSN)));
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            closeViews();
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the DS\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("500", "DS", title);
        }

        Vector<String> existingUserNames= new Vector<String>();
        if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) {
            response_code+="1";
            if (!fromWeb) personPairedWith= "\n\t\"personPairedWith\": [";
            else personPairedWith= "<br>&emsp;\"personPairedWith\": [";
            boolean firstElem = true;
            if (!existingOwners.isEmpty()) {
                for (AcademicPerson person : existingOwners) {
                    if (!existingUserNames.contains(person.getLoginName())) {
                        if (firstElem) firstElem = false;
                        else personPairedWith += ",";
                        if (!fromWeb) personPairedWith += "\n\t\t";
                        else personPairedWith += "<br>&emsp;&emsp;";
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
                        if (!fromWeb) personPairedWith += "\n\t\t";
                        else personPairedWith += "<br>&emsp;&emsp;";
                        personPairedWith += "\"" + uid + "\"";
                        existingUserNames.add(uid);
                    }
                }
            }
            if (!fromWeb) personPairedWith += "\n\t]";
            else personPairedWith += "<br>&emsp;]";
        }else{
            response_code+="2";
        }
        int exit_code= generateNames();
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
                if (!fromWeb) suggestedNames = "\n\t\"suggestions\":\t[";
                else suggestedNames = "<br>&emsp;\"suggestions\":&emsp;[";
                for(String login : proposedNames){
                    if (loginGen.checkIfUserNameExists(login, Views, ldapDS, disabledGracePeriod)) continue;
                    if(firstElem){
                        firstElem = false;
                    }else{
                        suggestedNames += ",";
                    }
                    if (!fromWeb) suggestedNames +="\n\t\t";
                    else suggestedNames +="<br>&emsp;&emsp;";
                    suggestedNames += "\""+login+"\"";
                }
                if (!fromWeb) suggestedNames+="\n\t]";
                else suggestedNames+="<br>&emsp;]";
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
