package gr.gunet.uLookup.endpoints.finder;

import gr.gunet.uLookup.db.DBConnectionPool;
import gr.gunet.uLookup.db.HRMSDBView;
import gr.gunet.uLookup.db.SISDBView;
import gr.gunet.uLookup.db.ldap.LdapConnectionPool;
import gr.gunet.uLookup.db.ldap.LdapManager;
import gr.gunet.uLookup.db.personInstances.AcademicPerson;
import gr.gunet.uLookup.tools.ResponseMessages;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;

import java.util.*;

public class Finder {
    private final DBConnectionPool Views;
    private final LdapConnectionPool ldapDS;
    ResponseMessages responses;
    String loginName;
    String primaryAffiliation;

    public Finder(String institution, ResponseMessages responses) {
        this.Views= new DBConnectionPool(institution);
        this.ldapDS= new LdapConnectionPool(institution);
        this.responses=responses;
    }

    public String findRoles(String loginName, boolean fromWeb) throws Exception {
        this.loginName= loginName;
        if (!findErrors(loginName).equals("")){
            return responses.getResponse("400", findErrors(loginName), "Roles Found");
        }

        Collection<String> currentRoles;
        Object DSRoles= getDSRoles();
        if (DSRoles instanceof String) return (String) DSRoles;
        else  currentRoles = new HashSet<>((Collection<String>) DSRoles);

        Object viewRoles= getViewRoles();
        if (viewRoles instanceof String) return (String) viewRoles;
        else  currentRoles.addAll((Collection<String>) viewRoles);

        if (currentRoles.isEmpty()){
            return responses.getResponse("200", "", "Roles Found");
        }
        else {
            HashMap<String, String> results= new FinderResults(currentRoles,primaryAffiliation, responses).getResults(fromWeb);
            return responses.getResponse(results.get("code"), results.get("content"), results.get("title"));
        }
    }

    public String findErrors(String Name){
        if(Name == null || Name.trim().equals("")){
            return "No loginName provided.";
        }
        else if (Name.length()>1 && !Name.equals(Name.trim())){
            return "Whitespace character found.";
        }
        else if (Name.length() < 4 || Name.length() > 20){
            return "LoginName length outside character limits.";
        }
        else if (!Name.matches("([a-z\\d]+[._-]?[a-z\\d]+)+")){
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
        LdapManager ds;
        Collection<LdapEntry> existingDSOwners;

        Object existingSISOwners=getPerson(Views.getSISConn());
        if (existingSISOwners instanceof String) return existingSISOwners;
        else if (existingSISOwners!=null) {
            Collection<AcademicPerson> SISOwners= (Collection<AcademicPerson>) existingSISOwners;
            if (!SISOwners.isEmpty()) viewRoles.add("student");
            else{
                ds= ldapDS.getConn();
                existingDSOwners= ds.search(ds.createSearchFilter("schGrAcPersonalLinkageID=*sis*","uid="+loginName));
                if (existingDSOwners!=null && !existingDSOwners.isEmpty()) viewRoles.add("student");
            }
        }

        Object existingHRMSOwners=getPerson(Views.getHRMSConn(), false);
        if (existingHRMSOwners instanceof String) return existingHRMSOwners;
        else if (existingHRMSOwners!=null) {
            Collection<AcademicPerson> HRMSOwners= (Collection<AcademicPerson>) existingHRMSOwners;
            if (!HRMSOwners.isEmpty()) viewRoles.add("faculty");
            else{
                ds= ldapDS.getConn();
                existingDSOwners= ds.search(ds.createSearchFilter("schGrAcPersonalLinkageID=*hrms*","uid="+loginName));
                if (existingDSOwners!=null && !existingDSOwners.isEmpty()) viewRoles.add("faculty");
            }
        }

        if (Views.getHRMS2Conn()==null) return viewRoles;
        Object existingHRMS2Owners=getPerson(Views.getHRMS2Conn(), true);
        if (existingHRMS2Owners instanceof String) return existingHRMS2Owners;
        else if (existingHRMS2Owners!=null){
            Collection<AcademicPerson> HRMS2Owners= (Collection<AcademicPerson>) existingHRMS2Owners;
            if (!HRMS2Owners.isEmpty()) viewRoles.add("staff");
            else{
                ds= ldapDS.getConn();
                existingDSOwners= ds.search(ds.createSearchFilter("schGrAcPersonalLinkageID=*hrms2*","uid="+loginName));
                if (existingDSOwners!=null && !existingDSOwners.isEmpty()) viewRoles.add("staff");
            }
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
            return responses.getResponse("500", source, "Roles Found");
        }
        else{
            System.out.println("-Response code: 501");
            System.out.println("-message: An error has occurred");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("501", "An error has occurred.", "Roles Found");
        }
    }

    public void closeViews(){
        DBConnectionPool.clean();
        LdapConnectionPool.clean();
    }
}
