package gr.gunet.uLookup.endpoints.proposer;

import gr.gunet.uLookup.db.DBConnectionPool;
import gr.gunet.uLookup.db.HRMSDBView;
import gr.gunet.uLookup.db.SISDBView;
import gr.gunet.uLookup.db.ldap.LdapConnectionPool;
import gr.gunet.uLookup.db.ldap.LdapManager;
import gr.gunet.uLookup.db.personInstances.AcademicPerson;
import gr.gunet.uLookup.tools.ResponseMessages;
import gr.gunet.uLookup.tools.generator.UserNameGen;
import org.ldaptive.LdapEntry;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

public class Proposer {
    private final DBConnectionPool Views;
    private final LdapConnectionPool ldapDS;
    ResponseMessages responses;
    HashMap<String,String> attributes;
    Collection<AcademicPerson> existingOwners;
    Collection<LdapEntry> existingDSOwners;

    public Proposer(String institution, ResponseMessages responses) {
        this.Views= new DBConnectionPool(institution);
        this.ldapDS= new LdapConnectionPool(institution);
        this.responses=responses;
    }
    public String proposeNames(HashMap<String,String> attributes, boolean fromWeb) throws Exception {
        this.attributes=attributes;

        if (!findErrors().equals("")){
          return responses.getResponse("400", findErrors(), "Suggested LoginNames");
        }
        Object existingNames=getExisting();
        if (existingNames instanceof String) return (String) existingNames;
        Object newNames= generateNew();
        if (newNames instanceof String) return (String) newNames;
        HashMap<String, String> results= new ProposerResults((Collection<String>) existingNames,(Collection<String>) newNames,attributes, responses).getResults(fromWeb);

        System.out.println("-Response code: " + results.get("code"));
        System.out.println("-----------------------------------------------------------");
        System.out.println();
        return responses.getResponse(results.get("code"), results.get("content"), results.get("title"));
    }
    public Object getExisting(){
        existingDSOwners= new Vector<>();
        existingOwners=new Vector<>();
        String SSN= attributes.get("SSN");
        String ssnCountry= attributes.get("ssnCountry");
        if (SSN.trim().equals("") || ssnCountry.trim().equals("")) return null;
        HashMap<String, String> searchAttributes = new HashMap<>();
        searchAttributes.put("SSN", SSN);
        searchAttributes.put("ssnCountry", ssnCountry);

        try{
            SISDBView sis = Views.getSISConn();
            existingOwners.addAll(sis.fetchAll(searchAttributes));
        }
        catch (Exception e){
            return errorMessage(e,"SIS");
        }

        try{
            HRMSDBView hrms = Views.getHRMSConn();
            if (hrms != null) existingOwners.addAll(hrms.fetchAll(searchAttributes));
        }
        catch (Exception e){
            return errorMessage(e,"HRMS");
        }

        try{
            HRMSDBView hrms2 = Views.getHRMS2Conn();
            if (hrms2 != null) existingOwners.addAll(hrms2.fetchAll(searchAttributes));
        }
        catch (Exception e){
            return errorMessage(e,"ELKE");
        }

        try{
            String SSNCountry= attributes.get("ssnCountry");
            LdapManager ldap = ldapDS.getConn();
            if (SSNCountry.equals("GR")) existingDSOwners.addAll(ldap.search(ldap.createSearchFilter("schGrAcPersonSSN=" + attributes.get("SSN"))));
        }
        catch (Exception e){
            return errorMessage(e,"DS");
        }

        Collection<String> existingUserNames= new LinkedList<>();
        if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()) {
            if (!existingOwners.isEmpty()) {
                for (AcademicPerson person : existingOwners) {
                    if (!existingUserNames.contains(person.getLoginName())) {
                        existingUserNames.add(person.getLoginName());
                    }
                }
            }
            if (!existingDSOwners.isEmpty()) {
                for (LdapEntry person : existingDSOwners) {
                    String uid = person.getAttribute("uid").getStringValue();
                    if (!existingUserNames.contains(uid)) {
                        existingUserNames.add(uid);
                    }
                }
            }
        }
        return existingUserNames;
    }

    public Object generateNew() throws Exception {
        String FN= attributes.get("fn");
        String LN= attributes.get("ln");
        if (FN==null) FN="";
        if (LN==null) LN="";

        UserNameGen loginGen = null;
        if (FN.trim().equals("") || LN.trim().equals("")){
            if (!existingOwners.isEmpty() || !existingDSOwners.isEmpty()){
                if (!existingOwners.isEmpty()) loginGen= new UserNameGen(existingOwners.iterator().next());
                else loginGen= new UserNameGen(existingDSOwners.iterator().next());
            }
        }
        else loginGen=new UserNameGen(FN, LN);
        if (loginGen!=null){
            Collection<String> proposedNames= loginGen.proposeNames();
            return keepNew(proposedNames);
        }
        return null;
    }

    public Object keepNew(Collection<String> proposedNames) throws Exception {
        SISDBView sis;
        HRMSDBView hrms, hrms2;
        LdapManager ldap;
        Collection<String> keptNames= new LinkedList<>();
        try {
            sis=Views.getSISConn();
            hrms=Views.getHRMSConn();
            hrms2=Views.getHRMS2Conn();
            ldap=ldapDS.getConn();
        }
        catch (Exception e){
            return errorMessage(e, "unknown");
        }

        for (String proposedName: proposedNames){
            HashMap<String,String> searchAttributes= new HashMap<>();
            searchAttributes.put("loginName", proposedName);
            Collection<AcademicPerson> existingOwners= sis.fetchAll(searchAttributes);
            if (hrms!=null) existingOwners.addAll(hrms.fetchAll(searchAttributes));
            if (hrms2!=null) existingOwners.addAll(hrms2.fetchAll(searchAttributes));
            Collection<LdapEntry> existingDSOwners= ldap.search(ldap.createSearchFilter("(schGrAcPersonID=*)","uid="+proposedName));
            if (existingOwners.isEmpty() && existingDSOwners.isEmpty()) keptNames.add(proposedName);
        }
        return keptNames;
    }

    public String findErrors(){
        String FN =attributes.get("fn");
        String LN =attributes.get("ln");
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

    public String errorMessage(Exception e, String source){
        e.printStackTrace(System.err);
        closeViews();
        if (source!=null){
            System.out.println("-Response code: 500");
            System.out.println("-message: " + "\"Could not connect to the " + source + ".\"");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("500", source, "Suggested LoginNames");
        }
        else{
            System.out.println("-Response code: 501");
            System.out.println("-message: An error has occurred");
            System.out.println("-----------------------------------------------------------");
            System.out.println();
            return responses.getResponse("501", "An error has occurred.", "Suggested LoginNames");
        }
    }

    public void closeViews(){
        DBConnectionPool.clean();
        LdapConnectionPool.clean();
    }
}
