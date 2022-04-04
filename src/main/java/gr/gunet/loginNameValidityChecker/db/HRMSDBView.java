package gr.gunet.loginNameValidityChecker.db;

import gr.gunet.loginNameValidityChecker.AcademicPerson;
import gr.gunet.loginNameValidityChecker.db.viewentities.*;
import gr.gunet.loginNameValidityChecker.tools.PropertyReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class HRMSDBView extends DBManager{
    private static final HashMap<String,String> ATTRIBUTE_DATA_TYPES;
    static{
        ATTRIBUTE_DATA_TYPES = new HashMap();
        ATTRIBUTE_DATA_TYPES.put("registrationid", "varchar");
        ATTRIBUTE_DATA_TYPES.put("systemid", "varchar");
        ATTRIBUTE_DATA_TYPES.put("ssn", "varchar");
        ATTRIBUTE_DATA_TYPES.put("tin", "varchar");
        ATTRIBUTE_DATA_TYPES.put("loginname", "varchar");
        ATTRIBUTE_DATA_TYPES.put("extemail", "varchar");
        ATTRIBUTE_DATA_TYPES.put("mobilephone", "varchar");
    }

    private String entityVersion;

    public HRMSDBView(String propertyFile) throws Exception{
        this(new PropertyReader(CONN_FILE_DIR+"/"+propertyFile));
    }

    public HRMSDBView(PropertyReader propReader) throws Exception{
        this(propReader.getProperty("databaseType"),propReader);
    }

    public HRMSDBView(String connector,String propertyFile) throws Exception{
        this(connector,new PropertyReader(CONN_FILE_DIR+"/"+propertyFile));
    }

    public HRMSDBView(String connector,PropertyReader propReader) throws Exception{
        super(connector,propReader);
        entityVersion = propReader.getProperty("entityVersion");
    }

    public Collection<AcademicPerson> fetchAll(String attributeName, String attributeValue, String disabledGracePeriod) throws Exception{
        String attributeType = ATTRIBUTE_DATA_TYPES.get(attributeName.toLowerCase());
        if(attributeType == null || attributeType.equals("")){
            throw new Exception("Field with name '"+attributeName+"' does not have a type registered");
        }
        if(attributeType.equals("number") && !attributeValue.matches("\\d+")){
            return new HashSet();
        }

        List<AcademicPerson> retVals = new LinkedList();

        String sql = "SELECT hp FROM HRMSPersonEntity_v"+entityVersion+" hp WHERE hp."+attributeName;
        if(attributeType.equals("varchar")){
            sql += "='"+attributeValue+"'";
        }else if(attributeType.equals("number")){
            sql += "="+attributeValue;
        }else{
            throw new Exception("Unknown data type '"+attributeType+"' encountered on attribute '"+attributeName+"' on CrossChecker:fetch");
        }
        if(disabledGracePeriod == null){
            sql += " AND hp.employeeStatus IN ('active','interim')";
        }else{
            sql += " AND (hp.employeeStatus IN ('active','interim') OR hp.employeeStatusDate > '"+disabledGracePeriod+"')";
        }

        if(entityVersion.equals("1")){
            List<HRMSPersonEntity_v1> results = select(sql,HRMSPersonEntity_v1.class);
            retVals.addAll(results);
        }
        else if(entityVersion.equals("2")){
            List<HRMSPersonEntity_v2> results = select(sql,HRMSPersonEntity_v2.class);
            retVals.addAll(results);
        }
        else if(entityVersion.equals("3")){
            List<HRMSPersonEntity_v3> results = select(sql,HRMSPersonEntity_v3.class);
            retVals.addAll(results);
        }
        else if(entityVersion.equals("4")){
            List<HRMSPersonEntity_v4> results = select(sql,HRMSPersonEntity_v4.class);
            retVals.addAll(results);
        }
        else if(entityVersion.equals("5")){
            List<HRMSPersonEntity_v5> results = select(sql,HRMSPersonEntity_v5.class);
            retVals.addAll(results);
        }
        else{
            throw new Exception("Unsupported entity version '"+entityVersion+"' on HRMS DB View.");
        }
        return retVals;
    }
    
    public Collection<AcademicPerson> fetchAll(String attributeName, String attributeValue, boolean onlyActive) throws Exception{
        String attributeType = ATTRIBUTE_DATA_TYPES.get(attributeName.toLowerCase());
        if(attributeType == null || attributeType.equals("")){
            throw new Exception("Field with name '"+attributeName+"' does not have a type registered");
        }
        if(attributeType.equals("number") && !attributeValue.matches("\\d+")){
            return new HashSet();
        }

        List<AcademicPerson> retVals = new LinkedList();

        String sql = "SELECT hp FROM HRMSPersonEntity_v"+entityVersion+" hp WHERE hp."+attributeName;
        if(attributeType.equals("varchar")){
            sql += "='"+attributeValue+"'";
        }else if(attributeType.equals("number")){
            sql += "="+attributeValue;
        }else{
            throw new Exception("Unknown data type '"+attributeType+"' encountered on attribute '"+attributeName+"' on CrossChecker:fetch");
        }
        if(onlyActive == true){
            sql += " AND hp.employeeStatus IN ('active','interim')";
        }

        if(entityVersion.equals("1")){
            List<HRMSPersonEntity_v1> results = select(sql,HRMSPersonEntity_v1.class);
            retVals.addAll(results);
        }
        else if(entityVersion.equals("2")){
            List<HRMSPersonEntity_v2> results = select(sql,HRMSPersonEntity_v2.class);
            retVals.addAll(results);
        }
        else if(entityVersion.equals("3")){
            List<HRMSPersonEntity_v3> results = select(sql,HRMSPersonEntity_v3.class);
            retVals.addAll(results);
        }
        else if(entityVersion.equals("4")){
            List<HRMSPersonEntity_v4> results = select(sql,HRMSPersonEntity_v4.class);
            retVals.addAll(results);
        }
        else if(entityVersion.equals("5")){
            List<HRMSPersonEntity_v5> results = select(sql,HRMSPersonEntity_v5.class);
            retVals.addAll(results);
        }
        else{
            throw new Exception("Unsupported entity version '"+entityVersion+"' on HRMS DB View.");
        }
        return retVals;
    }

    public Collection<AcademicPerson> fetchAll(AcademicPerson person, String disabledGracePeriod) throws Exception{
        String sql = "SELECT hp FROM HRMSPersonEntity_v"+entityVersion+" hp WHERE hp.SSN='" + person.getSSN();
        sql += "' AND hp.ssnCountry = '"+person.getSSNCountry()+"'";
        if (person.getTIN()!=null && person.getTINCountry()!=null){
            sql += " AND hp.tin = '"+person.getTIN()+"'";
            sql += " AND hp.tinCountry = '"+person.getTINCountry()+"'";
        }
        if (person.getBirthDate()!=null){
            if (entityVersion.equals("1"))  sql += " AND hp.birthdate = '"+person.getBirthDate()+"'";
            if (entityVersion.equals("4"))  sql += " AND hp.birthDate = '"+person.getBirthDate()+"'";
            sql += " AND hp.birthYear = '"+person.getBirthYear()+"'";
        }
        if(disabledGracePeriod == null){
            sql += " AND hp.employeeStatus IN ('active','interim')";
        }else{
            sql += " AND (hp.employeeStatus IN ('active','interim') OR hp.employeeStatusDate > '"+disabledGracePeriod+"')";
        }

        List<AcademicPerson> retVals = new LinkedList();
        if(entityVersion.equals("1")){
            List<HRMSPersonEntity_v1> results = select(sql,HRMSPersonEntity_v1.class);
            retVals.addAll(results);
        }else if(entityVersion.equals("2")){
            List<HRMSPersonEntity_v2> results = select(sql,HRMSPersonEntity_v2.class);
            retVals.addAll(results);
        }else if(entityVersion.equals("3")){
            List<HRMSPersonEntity_v3> results = select(sql,HRMSPersonEntity_v3.class);
            retVals.addAll(results);
        }else if(entityVersion.equals("4")){
            List<HRMSPersonEntity_v4> results = select(sql,HRMSPersonEntity_v4.class);
            retVals.addAll(results);
        }else if(entityVersion.equals("5")){
            List<HRMSPersonEntity_v5> results = select(sql,HRMSPersonEntity_v5.class);
            retVals.addAll(results);
        }else{
            throw new Exception("Unsupported entity version '"+entityVersion+"' on HRMS DB View.");
        }
        return retVals;
    }

    public Collection<AcademicPerson> fetchAll(String ssn, String ssnCountry) throws Exception{
        String sql = "SELECT hp FROM HRMSPersonEntity_v"+entityVersion+" hp WHERE hp.SSN='" + ssn;
        sql += "' AND hp.ssnCountry = '"+ssnCountry+"'";

        List<AcademicPerson> retVals = new LinkedList();
        if(entityVersion.equals("1")){
            List<HRMSPersonEntity_v1> results = select(sql,HRMSPersonEntity_v1.class);
            retVals.addAll(results);
        }else if(entityVersion.equals("2")){
            List<HRMSPersonEntity_v2> results = select(sql,HRMSPersonEntity_v2.class);
            retVals.addAll(results);
        }else if(entityVersion.equals("3")){
            List<HRMSPersonEntity_v3> results = select(sql,HRMSPersonEntity_v3.class);
            retVals.addAll(results);
        }else if(entityVersion.equals("4")){
            List<HRMSPersonEntity_v4> results = select(sql,HRMSPersonEntity_v4.class);
            retVals.addAll(results);
        }else if(entityVersion.equals("5")){
            List<HRMSPersonEntity_v5> results = select(sql,HRMSPersonEntity_v5.class);
            retVals.addAll(results);
        }
        else{
            throw new Exception("Unsupported entity version '"+entityVersion+"' on HRMS DB View.");
        }
        return retVals;
    }
}