package gr.gunet.uLookup.db.personInstances;
import java.util.Collection;

public interface AcademicPerson {
    Collection<String> getSsn();
    Collection<String> getSsnCountry();
    Collection<String> getTin();
    Collection<String> getTinCountry();
    String getAcademicID();
    String getRegistrationID();
    String getSystemID();
    String getFirstNameEl();
    String getFirstNameEn();
    String getLastNameEl();
    String getLastNameEn();
    String getBirthDate();
    String getBirthYear();
    String getGender();
    String getCitizenship();
    String getLoginName();
    String getSSN();
    String getSSNCountry();
    String getTIN();
    String getTINCountry();
}
