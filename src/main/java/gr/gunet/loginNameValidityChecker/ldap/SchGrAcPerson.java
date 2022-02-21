package gr.gunet.loginNameValidityChecker.ldap;

import gr.gunet.loginNameValidityChecker.AcademicPerson;
import java.util.Collection;
import java.util.HashSet;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;

public class SchGrAcPerson implements AcademicPerson{
    private Collection<String> uids;
    private String academicID;
    private Collection<String> ssn;
    private String SSN;
    private String SSNCountry;
    private Collection<String> ssnCountry;
    private Collection<String> tin;
    private Collection<String> tinCountry;
    private String lastNameEl;
    private String lastNameEn;
    private String birthDate;
    private String birthYear;
    private String gender;
    private String citizenship;
    
    private String loginNameOfConcern;
    private String SsnOfConcern;
    
    public SchGrAcPerson(LdapEntry entity, String conflictingLoginName) throws Exception{
        this.ssn=new HashSet();
        this.ssnCountry=new HashSet();
        this.tin=new HashSet();
        this.tinCountry=new HashSet();

        LdapAttribute uidAttribute = entity.getAttribute("uid");
        LdapAttribute academicIDAttribute = entity.getAttribute("schGrAcPersonID");
        LdapAttribute uniqueIDAttribute = entity.getAttribute("schacPersonalUniqueID");
        LdapAttribute lastNameElAttribute = entity.getAttribute("sn;lang-el");
        LdapAttribute lastNameEnAttribute = entity.getAttribute("sn;lang-en");
        LdapAttribute dateOfBirthAttribute = entity.getAttribute("schacDateOfBirth");
        LdapAttribute yearOfBirthAttribute = entity.getAttribute("schacYearOfBirth");
        LdapAttribute genderAttribute = entity.getAttribute("schacGender");
        LdapAttribute citizenshipAttribute = entity.getAttribute("schacCountryOfCitizenship");
        LdapAttribute ssnAttribute = entity.getAttribute("schGrAcPersonSSN");
        
        if(uidAttribute == null){
            this.uids = null;
        }else{
            this.uids = uidAttribute.getStringValues();
        }
        
        this.academicID = academicIDAttribute.getStringValue();
        
        this.ssn = new HashSet();
        this.ssnCountry = new HashSet();
        this.tin = new HashSet();
        this.tinCountry = new HashSet();
        
        if(uniqueIDAttribute != null){
            Collection<String> uniqueIDs = uniqueIDAttribute.getStringValues();
            for(String uniqueID : uniqueIDs){
                String[] uniqueIDSplit = uniqueID.split(":");
                if(uniqueIDSplit.length != 8){
                    throw new Exception("Could not parse a value in attribute schacPersonalUniqueID");
                }
                String type = uniqueIDSplit[6];
                if(type.toUpperCase().equals("SSN")){
                    ssn.add(uniqueIDSplit[7]);
                    ssnCountry.add(uniqueIDSplit[5].toUpperCase());
                    SSNCountry=uniqueIDSplit[5].toUpperCase();
                }else if(type.toUpperCase().equals("TIN")){
                    tin.add(uniqueIDSplit[7]);
                    tinCountry.add(uniqueIDSplit[5].toUpperCase());
                }
            }
        }
        if (ssnAttribute!=null) this.SSN=ssnAttribute.getStringValue();
        if (lastNameElAttribute!=null) this.lastNameEl = lastNameElAttribute.getStringValue();
        if (lastNameEnAttribute!=null) this.lastNameEn = lastNameEnAttribute.getStringValue();
        if (dateOfBirthAttribute!=null) this.birthDate = dateOfBirthAttribute.getStringValue();
        if (yearOfBirthAttribute!=null) this.birthYear = yearOfBirthAttribute.getStringValue();
        if (genderAttribute!=null) this.gender = genderAttribute.getStringValue();
        if (citizenshipAttribute!=null) this.citizenship = citizenshipAttribute.getStringValue();

        this.loginNameOfConcern = conflictingLoginName;
    }

    public SchGrAcPerson(LdapEntry entity, String conflictingSSN, int SSN) throws Exception{
        LdapAttribute uidAttribute = entity.getAttribute("uid");
        LdapAttribute academicIDAttribute = entity.getAttribute("schGrAcPersonID");
        LdapAttribute uniqueIDAttribute = entity.getAttribute("schacPersonalUniqueID");
        LdapAttribute lastNameElAttribute = entity.getAttribute("sn;lang-el");
        LdapAttribute lastNameEnAttribute = entity.getAttribute("sn;lang-en");
        LdapAttribute dateOfBirthAttribute = entity.getAttribute("schacDateOfBirth");
        LdapAttribute yearOfBirthAttribute = entity.getAttribute("schacYearOfBirth");
        LdapAttribute genderAttribute = entity.getAttribute("schacGender");
        LdapAttribute citizenshipAttribute = entity.getAttribute("schacCountryOfCitizenship");
        
        if(uidAttribute == null){
            this.uids = null;
        }else{
            this.uids = uidAttribute.getStringValues();
        }
        
        this.academicID = academicIDAttribute.getStringValue();
        
        this.ssn = new HashSet();
        this.ssnCountry = new HashSet();
        this.tin = new HashSet();
        this.tinCountry = new HashSet();
        
        if(uniqueIDAttribute != null){
            Collection<String> uniqueIDs = uniqueIDAttribute.getStringValues();
            for(String uniqueID : uniqueIDs){
                String[] uniqueIDSplit = uniqueID.split(":");
                if(uniqueIDSplit.length != 8){
                    throw new Exception("Could not parse a value in attribute schacPersonalUniqueID");
                }
                String type = uniqueIDSplit[6];
                if(type.toUpperCase().equals("SSN")){
                    ssn.add(uniqueIDSplit[7]);
                    ssnCountry.add(uniqueIDSplit[5].toUpperCase());
                }else if(type.toUpperCase().equals("TIN")){
                    tin.add(uniqueIDSplit[7]);
                    tinCountry.add(uniqueIDSplit[5].toUpperCase());
                }
            }
        }
        if (lastNameElAttribute!=null) this.lastNameEl = lastNameElAttribute.getStringValue();
        if (lastNameEnAttribute!=null) this.lastNameEn = lastNameEnAttribute.getStringValue();
        if (dateOfBirthAttribute!=null) this.birthDate = dateOfBirthAttribute.getStringValue();
        if (yearOfBirthAttribute!=null) this.birthYear = yearOfBirthAttribute.getStringValue();
        else if (dateOfBirthAttribute!=null) this.birthYear = this.birthDate.substring(0, 4);
        this.gender = genderAttribute.getStringValue();
        this.citizenship = citizenshipAttribute.getStringValue();
        
        this.SsnOfConcern = conflictingSSN;
        this.loginNameOfConcern= uidAttribute.getStringValue();
    }

    public Collection<String> getUids() {
        return uids;
    }
    
    @Override
    public String getAcademicID() {
        return academicID;
    }

    @Override
    public Collection<String> getSsn() {
        return ssn;
    }
    
    @Override
    public String getSSN() {
        return SSN;
    }

    @Override
    public Collection<String> getSsnCountry() {
        return ssnCountry;
    }
    
    @Override
    public Collection<String> getTin() {
        return tin;
    }
    
    @Override
    public Collection<String> getTinCountry() {
        return ssnCountry;
    }
    
    @Override
    public String getRegistrationID() {
        return null;
    }

    @Override
    public String getSystemID() {
        return null;
    }

    @Override
    public String getLastNameEl() {
        return lastNameEl;
    }

    @Override
    public String getLastNameEn() {
        return lastNameEn;
    }

    @Override
    public String getFirstNameEl() {
        return lastNameEl;
    }

    @Override
    public String getFirstNameEn() {
        return lastNameEn;
    }

    @Override
    public String getBirthDate() {
        return birthDate;
    }

    @Override
    public String getBirthYear() {
      return this.birthYear;
    }

    @Override
    public String getGender() {
        return this.gender;
    }

    @Override
    public String getCitizenship() {
        return this.citizenship;
    }

    @Override
    public String getLoginName() {
        return this.loginNameOfConcern;
    }
    
    @Override
    public String getSSNCountry() {
        return SSNCountry;
    }

    @Override
    public String getTIN() {
        return null;
    }

    @Override
    public String getTINCountry() {
        return null;
    }
}
