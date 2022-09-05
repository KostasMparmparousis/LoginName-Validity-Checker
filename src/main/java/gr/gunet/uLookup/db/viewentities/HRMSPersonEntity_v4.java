package gr.gunet.uLookup.db.viewentities;

import gr.gunet.uLookup.AcademicPerson;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="V_VD_HRMS")
public class HRMSPersonEntity_v4 implements Serializable,AcademicPerson{
    public HRMSPersonEntity_v4(){
    }
    
    @Id
    @Column(name = "SYSTEMID")
    private String systemID;

    @Id
    @Column(name = "REGISTRATIONID")
    private String registrationID;

    @Column(name = "SSN")
    private String SSN;
    
    @Column(name = "SSNCOUNTRY")
    private String ssnCountry;
    
    @Column(name = "TIN")
    private String tin;
    
    @Column(name = "TINCOUNTRY")
    private String tinCountry;
    
    @Column(name = "ACADEMICID")
    private String academicID;
    
    @Column(name = "DEPARTMENTID")
    private String departmentID;
    
    @Column(name = "DEPARTMENTEL")
    private String departmentEl;
    
    @Column(name = "DEPARTMENTEN")
    private String departmentEn;
    
    @Column(name = "FIRSTNAMEEL")
    private String firstNameEl;
    
    @Column(name = "FIRSTNAMEEN")
    private String firstNameEn;
    
    @Column(name = "LASTNAMEEL")
    private String lastNameEl;
    
    @Column(name = "LASTNAMEEN")
    private String lastNameEn;
    
    @Column(name = "MIDDLENAMEEL")
    private String middleNameEl;
    
    @Column(name = "MIDDLENAMEEN")
    private String middleNameEn;
    
    @Column(name = "FATHERFIRSTNAMEEL")
    private String fatherFirstNameEl;
    
    @Column(name = "FATHERFIRSTNAMEEN")
    private String fatherFirstNameEn;
    
    @Column(name = "MOTHERFIRSTNAMEEL")
    private String motherFirstNameEl;
    
    @Column(name = "MOTHERFIRSTNAMEEN")
    private String motherFirstNameEn;
    
    @Column(name = "BIRTHDATE")
    private String birthDate;
    
    @Column(name = "BIRTHYEAR")
    private String birthYear;
    
    @Column(name = "GENDER")
    private String gender;
    
    @Column(name = "CITIZENSHIP")
    private String citizenship;
    
    @Column(name = "EMPLOYEETYPE")
    private String employeeType;
    
    @Column(name = "FACULTYTYPE")
    private String facultyType;
    
    @Column(name = "STAFFTYPE")
    private String staffType;
    
    @Column(name = "ATTENDANCETYPE")
    private String attendanceType;
    
    @Column(name = "CONTRACTTYPE")
    private String contractType;
    
    @Column(name  = "EMPLOYEESTATUS")
    private String employeeStatus;
    
    @Column(name = "EMPLOYEESTATUSDATE")
    private String employeeStatusDate;
    
    @Column(name = "MOBILEPHONE")
    private String mobilePhone;
    
    @Column(name = "EXTEMAIL")
    private String extEmail;
    
    @Column(name = "ACCOUNTSTATUS")
    private String accountStatus;
    
    @Column(name = "LOGINNAME")
    private String loginName;

    @Override
    public String getSystemID() {
        return systemID;
    }

    public void setSystemID(String systemID) {
        this.systemID = systemID;
    }

    @Override
    public String getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    @Override
    public Collection<String> getSsn() {
      Collection<String> ssn= new HashSet<>();
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
      Collection<String> ssncountry= new HashSet<>();
      if(ssnCountry!=null) ssncountry.add(ssnCountry.toUpperCase());
      return ssncountry;
    }

    public void setSsnCountry(String ssnCountry) {
        this.ssnCountry = ssnCountry;
    }
    
    @Override
    public String getSSNCountry(){
      return ssnCountry;
    }

    @Override
    public Collection<String> getTin() {
      Collection<String> Tin= new HashSet<>();
      if (tin!=null) Tin.add(tin);
      return Tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }
    
    @Override
    public String getTIN(){
      return tin;
    }

    @Override
    public Collection<String> getTinCountry() {
      Collection<String> tincountry= new HashSet<>();
      if(tinCountry!=null) tincountry.add(tinCountry.toUpperCase());
      return tincountry;
    }

    public void setTinCountry(String tinCountry) {
        this.tinCountry = tinCountry;
    }
    
    @Override
    public String getTINCountry(){
      return tinCountry;
    }

    public String getAcademicID() {
        return academicID;
    }

    public void setAcademicID(String academicID) {
        this.academicID = academicID;
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

    public String getMiddleNameEl() {
        return middleNameEl;
    }

    public void setMiddleNameEl(String middleNameEl) {
        this.middleNameEl = middleNameEl;
    }

    public String getMiddleNameEn() {
        return middleNameEn;
    }

    public void setMiddleNameEn(String middleNameEn) {
        this.middleNameEn = middleNameEn;
    }

    public String getFatherFirstNameEl() {
        return fatherFirstNameEl;
    }

    public void setFatherFirstNameEl(String fatherFirstNameEl) {
        this.fatherFirstNameEl = fatherFirstNameEl;
    }

    public String getFatherFirstNameEn() {
        return fatherFirstNameEn;
    }

    public void setFatherFirstNameEn(String fatherFirstNameEn) {
        this.fatherFirstNameEn = fatherFirstNameEn;
    }

    public String getMotherFirstNameEl() {
        return motherFirstNameEl;
    }

    public void setMotherFirstNameEl(String motherFirstNameEl) {
        this.motherFirstNameEl = motherFirstNameEl;
    }

    public String getMotherFirstNameEn() {
        return motherFirstNameEn;
    }

    public void setMotherFirstNameEn(String motherFirstNameEn) {
        this.motherFirstNameEn = motherFirstNameEn;
    }

    public String getBirthdate() {
        return birthDate;
    }
    
    @Override
    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthdate(String birthdate) {
        this.birthDate = birthdate;
    }

    @Override
    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthyear(String birthYear) {
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

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }

    public String getFacultyType() {
        return facultyType;
    }

    public void setFacultyType(String facultyType) {
        this.facultyType = facultyType;
    }

    public String getStaffType() {
        return staffType;
    }

    public void setStaffType(String staffType) {
        this.staffType = staffType;
    }

    public String getAttendanceType() {
        return attendanceType;
    }

    public void setAttendanceType(String attendanceType) {
        this.attendanceType = attendanceType;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getEmployeeStatus() {
        return employeeStatus;
    }

    public void setEmployeeStatus(String employeeStatus) {
        this.employeeStatus = employeeStatus;
    }

    public String getEmployeeStatusDate() {
        return employeeStatusDate;
    }

    public void setEmployeeStatusDate(String employeeStatusDate) {
        this.employeeStatusDate = employeeStatusDate;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getExtEmail() {
        return extEmail;
    }

    public void setExtEmail(String extEmail) {
        this.extEmail = extEmail;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    @Override
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
