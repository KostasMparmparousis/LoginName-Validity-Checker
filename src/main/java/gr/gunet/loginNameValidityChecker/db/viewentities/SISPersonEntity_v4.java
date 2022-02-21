package gr.gunet.loginNameValidityChecker.db.viewentities;

import gr.gunet.loginNameValidityChecker.AcademicPerson;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name="v_vd_sis2")
public class SISPersonEntity_v4 implements Serializable,AcademicPerson{
    public SISPersonEntity_v4(){
        
    }
    
    @Id
    @Column(name = "systemID")
    private String systemID;
    
    @Id
    @Column(name = "registrationID")
    private String registrationID;
    
    @Column(name = "SSN")
    private String SSN;
    
    @Column(name = "SSNCOUNTRY")
    private String ssnCountry;
    
    @Column(name = "TIN")
    private String tin;
    
    @Column(name = "TINCOUNTRY")
    private String tinCountry;
    
    @Column(name = "firstNameEl")
    private String firstNameEl;
    
    @Column(name = "firstNameEn")
    private String firstNameEn;
    
    @Column(name = "lastNameEl")
    private String lastNameEl;
    
    @Column(name = "lastNameEn")
    private String lastNameEn;
    
    @Column(name = "fatherFirstNameEl")
    private String fatherNameEl;
    
    @Column(name = "fatherFirstNameEn")
    private String fatherNameEn;
    
    @Column(name = "motherFirstNameEl")
    private String motherFirstNameEl;
    
    @Column(name = "motherFirstNameEn")
    private String motherFirstNameEn;
    
    @Column(name = "birthDate")
    private String birthDate;
    
    @Column(name = "birthYear")
    private String birthYear;

    @Column(name = "gender")
    private String gender;
    
    @Column(name = "citizenship")
    private String citizenship;
    
    @Column(name = "departmentID")
    private String departmentID;
    
    @Column(name = "departmentEl")
    private String departmentEl;
    
    @Column(name = "departmentEn")
    private String departmentEn;
    
    @Column(name = "enrollmentStatus")
    private String enrollmentStatus;
    
    @Column(name = "enrollmentStatusDate")
    private String enrollmentStatusDate;
    
    @Column(name = "enrollmentType")
    private String enrollmentType;
    
    @Column(name = "enrollmentTerm")
    private String enrollmentTerm;
    
    @Column(name = "extEmail")
    private String extEmail;
    
    @Column(name = "mobilePhone")
    private String mobilePhone;
    
    @Column(name = "accountStatus")
    private String accountStatus;
    
    @Column(name = "attendanceType")
    private String attendanceType;
    
    @Column(name = "loginName")
    private String loginName;
    
    @Column(name = "inscriptionAcYear")
    private String inscriptionAcYear;
    
    @Override
    public String getSystemID() {
        return systemID;
    }

    public void setSystemID(String systemID) {
        this.systemID = systemID;
    }

    @Override
    public Collection<String> getSsn() {
      Collection<String> ssn= new HashSet();
      if (SSN!=null) ssn.add(SSN);
      return ssn;
    }

    @Override
    public String getSSN(){
      return SSN;
    }

    public void setSsn(String SSN) {
        this.SSN = SSN;
    }

    @Override
    public Collection<String> getSsnCountry() {
      Collection<String> ssncountry= new HashSet();
      if(ssnCountry!=null) ssncountry.add(ssnCountry.toUpperCase());
      return ssncountry;
    }

    @Override
    public String getSSNCountry(){
      return ssnCountry;
    }
    
    public void setSsnCountry(String ssnCountry) {
        this.ssnCountry = ssnCountry;
    }

    @Override
    public Collection<String> getTin() {
      Collection<String> Tin= new HashSet();
      if (tin!=null) Tin.add(tin);
      return Tin;
    }
    
    @Override
    public String getTIN(){
      return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    @Override
    public Collection<String> getTinCountry() {
      Collection<String> tincountry= new HashSet();
      if(tinCountry!=null) tincountry.add(tinCountry.toUpperCase());
      return tincountry;
    }
    
    @Override
    public String getTINCountry(){
      return tinCountry;
    }
    
    public void setTinCountry(String tinCountry) {
        this.tinCountry = tinCountry;
    }

    public String getFirstNameEl() {
        return firstNameEl;
    }

    public void setFirstNameEl(String firstNameEl) {
        this.firstNameEl = firstNameEl;
    }

    public String getFirstNameEn() {
        return firstNameEn;
    }

    public void setFirstNameEn(String firstNameEn) {
        this.firstNameEn = firstNameEn;
    }

    @Override
    public String getLastNameEl() {
        return lastNameEl;
    }

    public void setLastNameEl(String lastNameEl) {
        this.lastNameEl = lastNameEl;
    }

    @Override
    public String getLastNameEn() {
        return lastNameEn;
    }

    public void setLastNameEn(String lastNameEn) {
        this.lastNameEn = lastNameEn;
    }

    public String getFatherNameEl() {
        return fatherNameEl;
    }
    
    public String getFatherFirstNameEl() {
        return fatherNameEl;
    }

    public void setFatherNameEl(String fatherNameEl) {
        this.fatherNameEl = fatherNameEl;
    }

    public String getFatherNameEn() {
        return fatherNameEn;
    }
    
    public String getFatherFirstNameEn() {
        return fatherNameEn;
    }

    public void setFatherNameEn(String fatherNameEn) {
        this.fatherNameEn = fatherNameEn;
    }

    public String getMotherFirstNameEl() {
        return motherFirstNameEl;
    }
    
    public String getMotherNameEl() {
        return motherFirstNameEl;
    }

    public void setMotherFirstNameEl(String motherFirstNameEl) {
        this.motherFirstNameEl = motherFirstNameEl;
    }

    public String getMotherFirstNameEn() {
        return motherFirstNameEn;
    }
    
    public String getMotherNameEn() {
        return motherFirstNameEn;
    }

    public void setMotherFirstNameEn(String motherFirstNameEn) {
        this.motherFirstNameEn = motherFirstNameEn;
    }

    @Override
    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    @Override
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    @Override
    public String getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    public String getDepartmentID() {
        return departmentID;
    }

    public void setDepartmentID(String departmentID) {
        this.departmentID = departmentID;
    }

    public String getDepartmentEl() {
        return departmentEl;
    }

    public void setDepartmentEl(String departmentEl) {
        this.departmentEl = departmentEl;
    }

    public String getDepartmentEn() {
        return departmentEn;
    }

    public void setDepartmentEn(String departmentEn) {
        this.departmentEn = departmentEn;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public String getEnrollmentStatusDate() {
        return enrollmentStatusDate;
    }

    public void setEnrollmentStatusDate(String enrollmentStatusDate) {
        this.enrollmentStatusDate = enrollmentStatusDate;
    }

    public String getEnrollmentType() {
        return enrollmentType;
    }

    public void setEnrollmentType(String enrollmentType) {
        this.enrollmentType = enrollmentType;
    }

    public String getExtEmail() {
        return extEmail;
    }

    public void setExtEmail(String extEmail) {
        this.extEmail = extEmail;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getAttendanceType() {
        return attendanceType;
    }

    public void setAttendanceType(String attendanceType) {
        this.attendanceType = attendanceType;
    }

    public String getEnrollmentTerm() {
        return enrollmentTerm;
    }

    public void setEnrollmentTerm(String enrollmentTerm) {
        this.enrollmentTerm = enrollmentTerm;
    }

    @Override
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getInscriptionAcYear() {
        return inscriptionAcYear;
    }

    public void setInscriptionAcYear(String inscriptionAcYear) {
        this.inscriptionAcYear = inscriptionAcYear;
    }

    @Override
    public String getAcademicID() {
        return null;
    }
}
