package gr.gunet.loginNameValidityChecker;
import java.util.Collection;

public interface AcademicPerson {
    public Collection<String> getSsn();
    public Collection<String> getSsnCountry();
    public Collection<String> getTin();
    public Collection<String> getTinCountry();
    public String getAcademicID();
    public String getRegistrationID();
    public String getSystemID();
    public String getFirstNameEl();
    public String getFirstNameEn();
    public String getLastNameEl();
    public String getLastNameEn();
    public String getBirthDate();
    public String getBirthYear();
    public String getGender();
    public String getCitizenship();
    public String getLoginName();
    public String getSSN();
    public String getSSNCountry();
    public String getTIN();
    public String getTINCountry();
}
