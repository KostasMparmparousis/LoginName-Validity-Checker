package gr.gunet.loginNameValidityChecker;

import gr.gunet.loginNameValidityChecker.db.HRMSDBView;
import gr.gunet.loginNameValidityChecker.db.SISDBView;
import gr.gunet.loginNameValidityChecker.ldap.LdapManager;
import gr.gunet.loginNameValidityChecker.ldap.SchGrAcPerson;
import gr.gunet.loginNameValidityChecker.ldap.LdapConnectionPool;
import gr.gunet.loginNameValidityChecker.db.DBConnectionPool;
import java.util.Collection;
import java.util.HashSet;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;

public class LoginNameValidator {
    private DBConnectionPool Views;
    private LdapConnectionPool ldapDS;

    public LoginNameValidator(DBConnectionPool Views, LdapConnectionPool ldapDS) {
        this.Views=Views;
        this.ldapDS=ldapDS;
    }

    public Collection<Conflict> checkForValidityConflicts(AcademicPerson loginNameOwner,String loginName,String disabledGracePeriod) throws Exception{
        return checkForUniquenessConflicts(loginNameOwner,loginName,disabledGracePeriod);
    }

    public  Collection<Conflict> checkForValidityConflicts(AcademicPerson loginNameOwner,String disabledGracePeriod) throws Exception{
        return checkForUniquenessConflicts(loginNameOwner,disabledGracePeriod);
    }

    private Collection<Conflict> checkForUniquenessConflicts(AcademicPerson loginNameOwner,String disabledGracePeriod) throws Exception{
        return checkForUniquenessConflicts(loginNameOwner, loginNameOwner.getLoginName(), disabledGracePeriod);
    }

    private  Collection<Conflict> checkForUniquenessConflicts(AcademicPerson loginNameOwner,String loginName,String disabledGracePeriod) throws LdapException, Exception{
        Collection<Conflict> conflicts = new HashSet();
        Collection<LdapEntry> existingDSOwners;
        Collection<AcademicPerson> existingOwners;

        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;

        try{
          sis=Views.getSISConn();
          existingOwners= sis.fetchAll("loginName",loginName,disabledGracePeriod);
          for(AcademicPerson existingOwner : existingOwners){
            conflicts.addAll(samePersonChecks(loginNameOwner,existingOwner,"SIS DB View"));
          }
        }
        catch(Exception e){
          throw new Exception("SIS");
        }

        try{
          hrms=Views.getHRMSConn();
          if (hrms!=null){
            existingOwners= hrms.fetchAll("loginName",loginName,disabledGracePeriod);
            for(AcademicPerson existingOwner : existingOwners){
              conflicts.addAll(samePersonChecks(loginNameOwner,existingOwner,"HRMS DB View"));
            }
          }
        }
        catch(Exception e){
          throw new Exception("HRMS");
        }

        try{
          hrms2=Views.getHRMS2Conn();
          if (hrms2!=null){
            existingOwners= hrms2.fetchAll("loginName",loginName,disabledGracePeriod);
            for(AcademicPerson existingOwner : existingOwners){
                conflicts.addAll(samePersonChecks(loginNameOwner,existingOwner,"Associates DB View"));
            }
          }
        }
        catch(Exception e){
          throw new Exception("HRMS2");
        }

        try{
          ldap=ldapDS.getConn();
          existingDSOwners = ldap.search(ldap.createSearchFilter("(schGrAcPersonID=*)","uid="+loginName));
          for(LdapEntry existingDSOwner : existingDSOwners){
            conflicts.addAll(samePersonChecks(loginNameOwner,new SchGrAcPerson(existingDSOwner, loginName),"DS"));
          }
        }
        catch(Exception e){
          throw new Exception("DS");
        }

        return conflicts;
    }

    public Collection<String> getUIDPersons(AcademicPerson loginNameOwner,String disabledGracePeriod) throws Exception{
        return getUIDPersons(loginNameOwner,loginNameOwner.getLoginName(), disabledGracePeriod);
    }

    private Collection<String> getUIDPersons(AcademicPerson loginNameOwner,String loginName,String disabledGracePeriod) throws LdapException,Exception{
        Collection<String> UIDPersons = new HashSet();
        LdapManager ds=null;
        ds= ldapDS.getConn();
        Collection<LdapEntry> existingDSOwners= ds.search(ds.createSearchFilter("(!(schGrAcPersonID=*))","uid="+loginName));
        if (!existingDSOwners.isEmpty()){
          for (LdapEntry uidPerson: existingDSOwners){
              if (uidPerson.getAttribute("uid")!=null && (uidPerson.getAttribute("uid").getStringValue()).equals(loginName)){
                UIDPersons.add(loginName);
              }
          }
        }
        return UIDPersons;
    }

    public Collection<String> getNullAttributes(AcademicPerson loginNameOwner,String disabledGracePeriod) throws Exception{
        return getNullAttributes(loginNameOwner,loginNameOwner.getLoginName(), disabledGracePeriod);
    }

    private Collection<String> getNullAttributes(AcademicPerson loginNameOwner,String loginName,String disabledGracePeriod) throws LdapException,Exception{
        Collection<String> nullAttributes = new HashSet();
        Collection<LdapEntry> DSOwners;
        Collection<AcademicPerson> existingOwners= null;
        SISDBView sis=null;
        HRMSDBView hrms=null;
        HRMSDBView hrms2=null;
        LdapManager ldap=null;

        try{
            sis=Views.getSISConn();
            existingOwners= sis.fetchAll("loginName", loginName, disabledGracePeriod);
        }
        catch(Exception e){
            throw new Exception("SIS");
        }

        try{
            hrms=Views.getHRMSConn();
            if (hrms!=null){
                existingOwners.addAll(hrms.fetchAll("loginName",loginName,disabledGracePeriod));
            }
        }
        catch(Exception e){
            throw new Exception("HRMS");
        }

        try{
            hrms2=Views.getHRMS2Conn();
            if (hrms2!=null){
                existingOwners.addAll(hrms2.fetchAll("loginName",loginName,disabledGracePeriod));
            }
        }
        catch(Exception e){
            throw new Exception("HRMS2");
        }

        try{
            ldap=ldapDS.getConn();
            DSOwners = ldap.search(ldap.createSearchFilter("(schGrAcPersonID=*)","uid="+loginName));
            for (LdapEntry DSOwner: DSOwners){
                existingOwners.add(new SchGrAcPerson(DSOwner, loginName));
            }
        }
        catch(Exception e){
            throw new Exception("DS");
        }

        if (!existingOwners.isEmpty()) {
            nullAttributes = findNullAttributes(existingOwners, disabledGracePeriod);
        }

        return nullAttributes;
    }

    private Collection<String> findNullAttributes(Collection<AcademicPerson> existingOwners, String disabledGracePeriod){
        Collection<String> nullAttributes = new HashSet();
        String ssn=null;
        String ssnCountry=null;
        String birthDate=null;
        String birthYear=null;

        for (AcademicPerson existingOwner: existingOwners){
            if (existingOwner.getSSN()!=null) ssn=existingOwner.getSSN();
            if (existingOwner.getSSNCountry()!=null) ssnCountry=existingOwner.getSSNCountry();
            if (existingOwner.getBirthDate()!=null) birthDate=existingOwner.getBirthDate();
            if (existingOwner.getBirthYear()!=null) birthYear=existingOwner.getBirthYear();
        }

        if (ssn==null) nullAttributes.add("\"ssn\"");
        if (ssnCountry==null) nullAttributes.add("\"ssnCountry\"");
        if (birthDate==null) nullAttributes.add("\"birthDate\"");
        if (birthYear==null) nullAttributes.add("\"birthYear\"");

        return  nullAttributes;
    }

    private Collection<Conflict> samePersonChecks(AcademicPerson loginNameOwner, AcademicPerson existingOwner, String existingOwnerSource){
        Collection<Conflict> conflicts = new HashSet();

        String conflType = "existing-login";
        String conflDesc = "A record exists in '"+existingOwnerSource+"' with the same loginName, but a different";
        String existingOwnerKeys;
        if(existingOwnerSource.equals("DS")){
            existingOwnerKeys = "academicID="+existingOwner.getAcademicID();
        }else{
            existingOwnerKeys = "registrationID="+existingOwner.getRegistrationID()+",systemID="+existingOwner.getSystemID();
        }
        String conflSource = existingOwnerSource.split(" ")[0].toLowerCase();

        if(!loginNameOwner.getTin().isEmpty() && !existingOwner.getTin().isEmpty()){
          Collection<String> requestTins= loginNameOwner.getTin();
          Collection<String> existingTins= existingOwner.getTin();
          for (String requestTin: requestTins){
            if (!existingTins.contains(requestTin)){
              for(String existingTin: existingTins){
                conflicts.add(new Conflict(conflType, conflDesc+" TIN", "TIN", existingOwnerKeys,conflSource,requestTin,existingTin));
              }
            }
          }
        }

        if(!loginNameOwner.getTinCountry().isEmpty() && !existingOwner.getTinCountry().isEmpty()){
          Collection<String> requestTinCountries= loginNameOwner.getTinCountry();
          Collection<String> existingTinCountries= existingOwner.getTinCountry();
          for (String requestTinCountry: requestTinCountries){
            if (!existingTinCountries.contains(requestTinCountry)){
              for(String existingTinCountry: existingTinCountries){
                conflicts.add(new Conflict(conflType, conflDesc+" TIN Country", "TIN Country", existingOwnerKeys,conflSource,requestTinCountry,existingTinCountry));
              }
            }
          }
        }

        if(!loginNameOwner.getSsn().isEmpty() && !existingOwner.getSsn().isEmpty()){
          Collection<String> requestSsns= loginNameOwner.getSsn();
          Collection<String> existingSsns= existingOwner.getSsn();
          for (String requestSsn: requestSsns){
            if (!existingSsns.contains(requestSsn)){
              for(String existingSsn: existingSsns){
                conflicts.add(new Conflict(conflType, conflDesc+" SSN", "SSN", existingOwnerKeys,conflSource,requestSsn,existingSsn));
              }
            }
          }
        }

        if(!loginNameOwner.getSsnCountry().isEmpty() && !existingOwner.getSsnCountry().isEmpty()){
          Collection<String> requestSsnCountries= loginNameOwner.getSsnCountry();
          Collection<String> existingSsnCountries= existingOwner.getSsnCountry();
          for (String requestSsnCountry: requestSsnCountries){
            if (!existingSsnCountries.contains(requestSsnCountry)){
              for(String existingSsnCountry: existingSsnCountries){
                conflicts.add(new Conflict(conflType, conflDesc+" SSN Country", "SSN Country", existingOwnerKeys,conflSource,requestSsnCountry,existingSsnCountry));
              }
            }
          }
        }

        String loginNameOwnerBirthDate=loginNameOwner.getBirthDate();
        String existingOwnerBirthDate= existingOwner.getBirthDate();
        if (loginNameOwnerBirthDate!=null && existingOwnerBirthDate!=null){
            loginNameOwnerBirthDate= removeSpecialChars(loginNameOwnerBirthDate);
            existingOwnerBirthDate= removeSpecialChars(existingOwnerBirthDate);
            if (!loginNameOwnerBirthDate.equals(existingOwnerBirthDate)){
                conflicts.add(new Conflict(conflType, conflDesc+" date of birth", "birthDate", existingOwnerKeys,conflSource, loginNameOwnerBirthDate, existingOwnerBirthDate));
            }
        }

        String loginNameOwnerBirthYear=loginNameOwner.getBirthYear();
        String existingOwnerBirthYear=null;
        if (existingOwnerBirthDate!=null) existingOwnerBirthYear= existingOwner.getBirthYear();
        if (loginNameOwnerBirthYear!=null && existingOwnerBirthYear!=null){
            loginNameOwnerBirthYear= removeSpecialChars(loginNameOwnerBirthYear);
            existingOwnerBirthYear= removeSpecialChars(existingOwnerBirthYear);
            if (!loginNameOwnerBirthYear.equals(existingOwnerBirthYear)){
                conflicts.add(new Conflict(conflType, conflDesc+" year of birth", "birthYear", existingOwnerKeys,conflSource, loginNameOwnerBirthYear, existingOwnerBirthYear));
            }
        }

        return conflicts;
    }

    private String removeSpecialChars (String str){
        return str.replaceAll("[^0-9]", "");
    }

}
