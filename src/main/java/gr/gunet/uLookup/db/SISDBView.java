package gr.gunet.uLookup.db;

import gr.gunet.uLookup.AcademicPerson;
import gr.gunet.uLookup.db.viewentities.SISPersonEntity_v1;
import gr.gunet.uLookup.db.viewentities.SISPersonEntity_v2;
import gr.gunet.uLookup.db.viewentities.SISPersonEntity_v3;
import gr.gunet.uLookup.tools.PropertyReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class SISDBView extends DBManager{
    private static final HashMap<String,String> ATTRIBUTE_DATA_TYPES;
    static{
        ATTRIBUTE_DATA_TYPES = new HashMap<>();
        ATTRIBUTE_DATA_TYPES.put("registrationid", "varchar");
        ATTRIBUTE_DATA_TYPES.put("systemid", "varchar");
        ATTRIBUTE_DATA_TYPES.put("ssn", "varchar");
        ATTRIBUTE_DATA_TYPES.put("tin", "varchar");
        ATTRIBUTE_DATA_TYPES.put("loginname", "varchar");
        ATTRIBUTE_DATA_TYPES.put("extemail", "varchar");
        ATTRIBUTE_DATA_TYPES.put("mobilephone", "varchar");
    }
    
    private final String entityVersion;
    
    public SISDBView(String propertyFile) throws Exception{
        this(new PropertyReader(CONN_FILE_DIR+"/"+propertyFile));
    }
    
    public SISDBView(PropertyReader propReader) throws Exception{
        this(propReader.getProperty("databaseType"),propReader);
    }

    public SISDBView(String connector,PropertyReader propReader) throws Exception{
        super(connector,propReader);
        this.entityVersion = propReader.getProperty("entityVersion");
    }
    
    public Collection<AcademicPerson> fetchAll(String attributeName, String attributeValue, String disabledGracePeriod) throws Exception{
        String attributeType = ATTRIBUTE_DATA_TYPES.get(attributeName.toLowerCase());
        if(attributeType == null || attributeType.equals("")){
            throw new Exception("Field with name '"+attributeName+"' does not have a type registered");
        }
        if(attributeType.equals("number") && !attributeValue.matches("\\d+")){
            return new HashSet<>();
        }
        
        String sql = "SELECT sp FROM SISPersonEntity_v"+entityVersion+" sp WHERE sp."+attributeName;
        if(attributeType.equals("varchar")){
            sql += "='"+attributeValue+"'";
        }else if(attributeType.equals("number")){
            sql += "="+attributeValue;
        }else{
            throw new Exception("Unknown data type '"+attributeType+"' encountered on attribute '"+attributeName+"' on CrossChecker:fetch");
        }
        if(disabledGracePeriod == null){
            sql += " AND sp.enrollmentStatus IN ('active','interim')";
        }else{
            sql += " AND (sp.enrollmentStatus IN ('active','interim') OR sp.enrollmentStatusDate > '"+disabledGracePeriod+"')";
        }
        
        List<AcademicPerson> retVals = new LinkedList<>();
        switch (entityVersion) {
            case "1": {
                List<SISPersonEntity_v1> results = select(sql, SISPersonEntity_v1.class);
                retVals.addAll(results);
                break;
            }
            case "2": {
                List<SISPersonEntity_v2> results = select(sql, SISPersonEntity_v2.class);
                retVals.addAll(results);
                break;
            }
            case "3": {
                List<SISPersonEntity_v3> results = select(sql, SISPersonEntity_v3.class);
                retVals.addAll(results);
                break;
            }
            default:
                throw new Exception("Unsupported entity version '" + entityVersion + "' on SIS DB View.");
        }
        return retVals;
    }

    public Collection<AcademicPerson> fetchAll(String attributeName, String attributeValue, boolean onlyActive) throws Exception{
        String attributeType = ATTRIBUTE_DATA_TYPES.get(attributeName.toLowerCase());
        if(attributeType == null || attributeType.equals("")){
            throw new Exception("Field with name '"+attributeName+"' does not have a type registered");
        }
        if(attributeType.equals("number") && !attributeValue.matches("\\d+")){
            return new HashSet<>();
        }
        
        String sql = "SELECT sp FROM SISPersonEntity_v"+entityVersion+" sp WHERE sp."+attributeName;
        if(attributeType.equals("varchar")){
            sql += "='"+attributeValue+"'";
        }else if(attributeType.equals("number")){
            sql += "="+attributeValue;
        }else{
            throw new Exception("Unknown data type '"+attributeType+"' encountered on attribute '"+attributeName+"' on CrossChecker:fetch");
        }
        if(onlyActive){
            sql += " AND sp.enrollmentStatus IN ('active','interim')";
        }

        List<AcademicPerson> retVals = new LinkedList<>();
        switch (entityVersion) {
            case "1": {
                List<SISPersonEntity_v1> results = select(sql, SISPersonEntity_v1.class);
                retVals.addAll(results);
                break;
            }
            case "2": {
                List<SISPersonEntity_v2> results = select(sql, SISPersonEntity_v2.class);
                retVals.addAll(results);
                break;
            }
            case "3": {
                List<SISPersonEntity_v3> results = select(sql, SISPersonEntity_v3.class);
                retVals.addAll(results);
                break;
            }
            default:
                throw new Exception("Unsupported entity version '" + entityVersion + "' on HRMS DB View.");
        }
        return retVals;
    }

    public Collection<AcademicPerson> fetchAll(String ssn, String ssnCountry) throws Exception{
        String sql = "SELECT sp FROM SISPersonEntity_v"+entityVersion+" sp WHERE sp.SSN='" + ssn;
        sql += "' AND sp.ssnCountry = '"+ssnCountry+"'";

        List<AcademicPerson> retVals = new LinkedList<>();
        switch (entityVersion) {
            case "1": {
                List<SISPersonEntity_v1> results = select(sql, SISPersonEntity_v1.class);
                retVals.addAll(results);
                break;
            }
            case "2": {
                List<SISPersonEntity_v2> results = select(sql, SISPersonEntity_v2.class);
                retVals.addAll(results);
                break;
            }
            case "3": {
                List<SISPersonEntity_v3> results = select(sql, SISPersonEntity_v3.class);
                retVals.addAll(results);
                break;
            }
            default:
                throw new Exception("Unsupported entity version '" + entityVersion + "' on HRMS DB View.");
        }
        return retVals;
    }

    public Collection<AcademicPerson> fetchAll(AcademicPerson person, String disabledGracePeriod) throws Exception{
        String sql = "SELECT sp FROM SISPersonEntity_v"+entityVersion+" sp WHERE sp.SSN='" + person.getSSN();
        sql += "' AND sp.ssnCountry = '"+person.getSSNCountry()+"'";
        if (person.getTIN()!=null && person.getTINCountry()!=null){
            sql += " AND sp.tin = '"+person.getTIN()+"'";
            sql += " AND sp.tinCountry = '"+person.getTINCountry()+"'";
        }
        if (person.getBirthDate()!=null){
            sql += " AND sp.birthDate = '"+person.getBirthDate()+"'";
            sql += " AND sp.birthYear = '"+person.getBirthYear()+"'";
        }

        if(disabledGracePeriod == null){
            sql += " AND sp.enrollmentStatus IN ('active','interim')";
        }else{
            sql += " AND (sp.enrollmentStatus IN ('active','interim') OR sp.enrollmentStatusDate > '"+disabledGracePeriod+"')";
        }

        List<AcademicPerson> retVals = new LinkedList<>();
        switch (entityVersion) {
            case "1": {
                List<SISPersonEntity_v1> results = select(sql, SISPersonEntity_v1.class);
                retVals.addAll(results);
                break;
            }
            case "2": {
                List<SISPersonEntity_v2> results = select(sql, SISPersonEntity_v2.class);
                retVals.addAll(results);
                break;
            }
            case "3": {
                List<SISPersonEntity_v3> results = select(sql, SISPersonEntity_v3.class);
                retVals.addAll(results);
                break;
            }
            default:
                throw new Exception("Unsupported entity version '" + entityVersion + "' on HRMS DB View.");
        }
        return retVals;
    }
}
