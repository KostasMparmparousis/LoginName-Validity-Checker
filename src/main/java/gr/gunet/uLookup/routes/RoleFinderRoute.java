package gr.gunet.uLookup.routes;

import gr.gunet.uLookup.AcademicPerson;
import gr.gunet.uLookup.ResponseMessages;
import gr.gunet.uLookup.db.DBConnectionPool;
import gr.gunet.uLookup.db.HRMSDBView;
import gr.gunet.uLookup.db.SISDBView;
import gr.gunet.uLookup.ldap.LdapConnectionPool;
import gr.gunet.uLookup.ldap.LdapManager;
import gr.gunet.uLookup.tools.CustomJsonReader;

import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.*;

public class RoleFinderRoute implements Route {
    DBConnectionPool Views;
    LdapConnectionPool ldapDS;
    String loginName;
    String institution;
    boolean onlyActive;
    ResponseMessages responses;
    String primaryAffiliation;
    String title;
    public RoleFinderRoute(String institution) {
      this.institution= institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        primaryAffiliation=null;
        boolean fromWeb;

        title= "Roles Found";
        responses= new ResponseMessages(req.session().attribute("web"));
        if (!req.session().attribute("authorized").equals("true")){
          String errorMessage= "You were not authorized";
          closeViews();
          return responses.getResponse("401", errorMessage, title);
        }

        String roles;
        String response_code="";

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
        }else if (!loginName.matches("([a-z\\d]+[._-]?[a-z\\d]+)+")){
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

        Collection<String> currentRoles;
        Object DSRoles= getDSRoles();
        if (DSRoles instanceof String) return DSRoles;
        else  currentRoles = new HashSet<>((Collection<String>) DSRoles);

        Object viewRoles= getViewRoles();
        if (viewRoles instanceof String) return viewRoles;
        else  currentRoles.addAll((Collection<String>) viewRoles);

        if (currentRoles.isEmpty()){
            response_code="200";
            return responses.getResponse(response_code, "", title);
        }
        else {
            response_code = "100";
            boolean firstElem = true;
            if (!fromWeb) roles = ",\n\t\"Affiliations\" : [";
            else roles = ",<br>&emsp;\"Affiliations\" : [";
            for (String role : currentRoles) {
                if (firstElem) {
                    firstElem = false;
                } else {
                    roles= roles.concat(",");
                }
                if (!fromWeb) roles= roles.concat("\n\t\t");
                else roles= roles.concat("<br>&emsp;&emsp;");
                roles += "\"" + role + "\"";
            }
            if (!fromWeb) roles += "\n\t]";
            else roles += "<br>&emsp;]";

            if (primaryAffiliation!=null){
                if (!fromWeb) roles= roles.concat(",\n\t\"primaryAffiliation\" : \"" + primaryAffiliation + "\"");
                else roles= roles.concat(",<br>&emsp;\"primaryAffiliation\" : \"" + primaryAffiliation + "\"");
            }
        }
        return responses.getResponse(response_code, roles, title);
    }

    private Object getDSRoles(){
        LdapManager ds;
        Collection<LdapEntry> existingDSOwners;
        try {
            ds = ldapDS.getConn();
             existingDSOwners = ds.search(ds.createSearchFilter("uid=" + loginName));
        }
        catch (Exception e){
            return errorMessage(e,"DS");
        }

        LdapAttribute primaryAffiliationAttribute;
        LdapAttribute affiliation;
        Collection<String> affiliations= new LinkedList<>();

        for(LdapEntry existingDSOwner : existingDSOwners){
            affiliation=existingDSOwner.getAttribute("eduPersonAffiliation");
            primaryAffiliationAttribute=existingDSOwner.getAttribute("eduPersonPrimaryAffiliation");
            if (affiliation!=null) affiliations= affiliation.getStringValues();
            if (primaryAffiliationAttribute!=null) primaryAffiliation= primaryAffiliationAttribute.getStringValue();
        }
        return affiliations;
    }

    private Object getViewRoles() throws Exception{
        Collection<String> viewRoles= new ArrayList<>();

        Object existingSISOwners=getPerson(Views.getSISConn());
        if (existingSISOwners instanceof String) return existingSISOwners;
        else if (existingSISOwners!=null) {
            Collection<AcademicPerson> SISOwners= (Collection<AcademicPerson>) existingSISOwners;
            if (!SISOwners.isEmpty()) viewRoles.add("student");
        }

        Object existingHRMSOwners=getPerson(Views.getHRMSConn(), false);
        if (existingHRMSOwners instanceof String) return existingHRMSOwners;
        else if (existingHRMSOwners!=null) {
            Collection<AcademicPerson> HRMSOwners= (Collection<AcademicPerson>) existingHRMSOwners;
            if (!HRMSOwners.isEmpty()) viewRoles.add("faculty");
        }

        if (Views.getHRMS2Conn()==null) return viewRoles;
        Object existingHRMS2Owners=getPerson(Views.getHRMS2Conn(), true);
        if (existingHRMS2Owners instanceof String) return existingHRMS2Owners;
        else if (existingHRMS2Owners!=null){
            Collection<AcademicPerson> HRMS2Owners= (Collection<AcademicPerson>) existingHRMS2Owners;
            if (!HRMS2Owners.isEmpty()) viewRoles.add("staff");
        }

        return viewRoles;
    }

    private Object getPerson(SISDBView view){
        Collection<AcademicPerson> existingSISOwners;
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("loginName", loginName);
        try{
            existingSISOwners= view.fetchAll(attributes);
        }
        catch (Exception e){
            return errorMessage(e,"SIS");
        }
        return existingSISOwners;
    }

    private Object getPerson(HRMSDBView view, boolean ELKE){
        Collection<AcademicPerson> existingHRMSOwners;
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("loginName", loginName);
        try{
            existingHRMSOwners= view.fetchAll(attributes);
        }
        catch (Exception e){
            if (!ELKE) return errorMessage(e,"HRMS");
            else return errorMessage(e,"ELKE");
        }
        return existingHRMSOwners;
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