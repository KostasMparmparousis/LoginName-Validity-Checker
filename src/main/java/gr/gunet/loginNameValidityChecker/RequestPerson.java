package gr.gunet.loginNameValidityChecker;

import gr.gunet.loginNameValidityChecker.tools.CustomJsonReader;
import java.util.Collection;
import java.util.HashSet;

public class RequestPerson implements AcademicPerson{
    private Collection<String> ssn;
    private Collection<String> ssnCountry;
    private Collection<String> tin;
    private Collection<String> tinCountry;
    private String SSN;
    private String SSNCountry;
    private String TIN;
    private String TINCountry;
    private String firstNameEl;
    private String firstNameEn;
    private String lastNameEl;
    private String lastNameEn;
    private String birthDate;
    private String birthYear;
    private String gender;
    private String citizenship;
    private String loginName;
    private String institution;
    private boolean verbose;
    public RequestPerson(CustomJsonReader jsonReader) throws Exception{
      this.ssn=new HashSet();
      this.ssnCountry=new HashSet();
      this.tin=new HashSet();
      this.tinCountry=new HashSet();
      
      SSN= jsonReader.readPropertyAsString("ssn");
      if(SSN == null || SSN.trim().equals("")){
        throw new Exception("No ssn provided");
      }
      else{
        ssn.add(SSN);
      }

      SSNCountry= jsonReader.readPropertyAsString("ssnCountry");
      if(SSNCountry == null || SSNCountry.trim().equals("")){
        throw new Exception("No ssnCountry provided");
      }
      else{
        ssnCountry.add(SSNCountry.toUpperCase());
      }

      TIN= jsonReader.readPropertyAsString("tin");
      if (TIN!=null && !TIN.trim().equals("")) tin.add(TIN);
      
      TINCountry= jsonReader.readPropertyAsString("tinCountry");
      if (TINCountry!=null && !TINCountry.trim().equals("")) tinCountry.add(TINCountry);

      this.birthYear = jsonReader.readPropertyAsString("birthYear");
      if(this.birthYear == null || this.birthYear.trim().equals("")){
        throw new Exception("No birthYear provided");
      }

      this.birthDate = jsonReader.readPropertyAsString("birthDate");
      if(this.birthDate == null || this.birthDate.trim().equals("")){
        throw new Exception("No birthDate provided");
      }

      this.loginName = jsonReader.readPropertyAsString("loginName");
      if(this.loginName == null || this.loginName.trim().equals("")){
        throw new Exception("No loginName provided");
      }

      this.institution = jsonReader.readPropertyAsString("institution");
      if(this.institution == null || this.institution.trim().equals("")){
        throw new Exception("No institution provided");
      }

      verbose=jsonReader.readPropertyAsBoolean("verbose");
    }

    @Override
    public Collection<String> getSsn() {
        return this.ssn;
    }

    @Override
    public Collection<String> getSsnCountry() {
        return this.ssnCountry;
    }

    @Override
    public Collection<String> getTin() {
        return this.tin;
    }

    @Override
    public Collection<String> getTinCountry() {
        return this.tinCountry;
    }
    
    public String getSSN(){
      return this.SSN;
    }

    public String getSSNCountry(){
      return this.SSNCountry;
    }
    
    public String getTIN(){
      return this.TIN;
    }
    
    public String getTINCountry(){
      return this.TINCountry;
    }

    @Override
    public String getRegistrationID() {
        return null;
    }

    @Override
    public String getSystemID() {
        return null;
    }

    public String getFirstNameEl(){
        return this.firstNameEl;
    }

    public String getFirstNameEn(){
        return this.firstNameEn;
    }

    @Override
    public String getLastNameEl() {
        return this.lastNameEl;
    }

    @Override
    public String getLastNameEn() {
        return this.lastNameEn;
    }

    @Override
    public String getBirthDate() {
        return this.birthDate;
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
        return this.loginName;
    }

    @Override
    public String getAcademicID() {
        return null;
    }

    public String getInstitution() {
        return institution;
    }

    public Boolean getVerbose(){
        return verbose;
    }

}
