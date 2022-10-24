package gr.gunet.uLookup.db.personInstances;

import gr.gunet.uLookup.tools.parsers.CustomJsonParser;
import java.util.Collection;
import java.util.HashSet;
import spark.Request;

public class RequestPerson implements AcademicPerson {
    private final Collection<String> ssn;
    private final Collection<String> ssnCountry;
    private final Collection<String> tin;
    private final Collection<String> tinCountry;
    private final String SSN;
    private final String SSNCountry;
    private String TIN;
    private String TINCountry;
    private final String birthDate;
    private final String birthYear;
    private final String loginName;
    private final boolean verbose;

    public RequestPerson(CustomJsonParser jsonReader) throws Exception{
      this.ssn=new HashSet<>();
      this.ssnCountry=new HashSet<>();
      this.tin=new HashSet<>();
      this.tinCountry=new HashSet<>();

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
      if(TIN == null || TIN.trim().equals("")){
        TIN=null;
      }
      else{
        tin.add(TIN);
      }

      TINCountry= jsonReader.readPropertyAsString("tinCountry");
      if(TINCountry == null || TINCountry.trim().equals("")){
        TINCountry=null;
      }
      else{
        tinCountry.add(TINCountry);
      }

      this.birthDate = jsonReader.readPropertyAsString("birthDate");
      if(this.birthDate == null || this.birthDate.trim().equals("")){
        throw new Exception("No birthDate provided");
      }
      else{
        this.birthYear= this.birthDate.substring(0, 4);
      }

      this.loginName = jsonReader.readPropertyAsString("loginName");
      if(this.loginName == null || this.loginName.trim().equals("")){
        throw new Exception("No loginName provided.");
      }
      else if (!loginName.equals(loginName.trim())){
        throw new Exception("Whitespace character found.");
      }
      else if (loginName.length() < 4 || loginName.length() > 20){
        throw new Exception("LoginName length outside character limits.");
      }else if (!loginName.matches("([a-z0-9]+[._-]?[a-z0-9]+)+")){
        for(int i=0;i<loginName.length();i++){
            char ch = loginName.charAt(i);
            if(Character.isUpperCase(ch)){
                throw new Exception("Capital character found.");
            }
        }
        throw new Exception("Invalid loginName format.");
      }
      String Verbose=jsonReader.readPropertyAsString("verbose");
      verbose= Verbose != null && !Verbose.trim().equals("") && !Verbose.equals("false");
    }

    public RequestPerson(Request req) throws Exception{
      this.ssn=new HashSet<>();
      this.ssnCountry=new HashSet<>();
      this.tin=new HashSet<>();
      this.tinCountry=new HashSet<>();

      SSN= req.queryParams("ssn");
      if(SSN == null || SSN.trim().equals("")){
        throw new Exception("No ssn provided");
      }
      else{
        ssn.add(SSN);
      }

      SSNCountry= req.queryParams("ssnCountry");
      if(SSNCountry == null || SSNCountry.trim().equals("")){
        throw new Exception("No ssnCountry provided");
      }
      else{
        ssnCountry.add(SSNCountry.toUpperCase());
      }

      TIN= req.queryParams("tin");
      if(TIN == null || TIN.trim().equals("")){
        TIN=null;
      }
      else{
        tin.add(TIN);
      }

      TINCountry= req.queryParams("tinCountry");
      if(TINCountry == null || TINCountry.trim().equals("")){
        TINCountry=null;
      }
      else{
        tinCountry.add(TINCountry);
      }

      this.birthDate = req.queryParams("birthDate");
      if(this.birthDate == null || this.birthDate.trim().equals("")){
        throw new Exception("No birthDate provided");
      }
      else{
        this.birthYear= this.birthDate.substring(0, 4);
      }

      this.loginName = req.queryParams("loginName");
      if(this.loginName == null || this.loginName.trim().equals("")){
        throw new Exception("No loginName provided.");
      }
      else if (!loginName.equals(loginName.trim())){
        throw new Exception("Whitespace character found.");
      }
      else if (loginName.length() < 4 || loginName.length() > 20){
        throw new Exception("LoginName length outside character limits.");
      }else if (!loginName.matches("([a-z0-9]+[._-]?[a-z0-9]+)+")){
        for(int i=0;i<loginName.length();i++){
            char ch = loginName.charAt(i);
            if(Character.isUpperCase(ch)){
                throw new Exception("Capital character found.");
            }
        }
        throw new Exception("Invalid loginName format.");
      }
      
      String Verbose=req.queryParams("verbose");
      verbose= Verbose != null && !Verbose.trim().equals("") && !Verbose.equals("No");

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

    @Override
    public String getFirstNameEl() {
        return null;
    }

    @Override
    public String getFirstNameEn() {
        return null;
    }

    @Override
    public String getLastNameEl() {
        return null;
    }

    @Override
    public String getLastNameEn() {
        return null;
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
        return null;
    }

    @Override
    public String getCitizenship() {
        return null;
    }

    @Override
    public String getLoginName() {
        return this.loginName;
    }

    @Override
    public String getAcademicID() {
        return null;
    }

    public Boolean getVerbose(){
        return verbose;
    }

}
