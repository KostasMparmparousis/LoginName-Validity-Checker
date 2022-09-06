package gr.gunet.uLookup.db;

import gr.gunet.uLookup.db.viewentities.HRMSPersonEntity_v5;
import gr.gunet.uLookup.db.viewentities.HRMSPersonEntity_v3;
import gr.gunet.uLookup.db.viewentities.HRMSPersonEntity_v2;
import gr.gunet.uLookup.db.viewentities.HRMSPersonEntity_v4;
import gr.gunet.uLookup.db.viewentities.HRMSPersonEntity_v1;
import gr.gunet.uLookup.AcademicPerson;
import gr.gunet.uLookup.tools.PropertyReader;

import java.util.*;

public class HRMSDBView extends DBManager{
    private static final HashMap<String,String> ATTRIBUTE_DATA_TYPES;
    static{
        ATTRIBUTE_DATA_TYPES = new HashMap<>();
        ATTRIBUTE_DATA_TYPES.put("registrationid", "varchar");
        ATTRIBUTE_DATA_TYPES.put("systemid", "varchar");
        ATTRIBUTE_DATA_TYPES.put("ssn", "varchar");
        ATTRIBUTE_DATA_TYPES.put("ssncountry", "varchar");
        ATTRIBUTE_DATA_TYPES.put("tin", "varchar");
        ATTRIBUTE_DATA_TYPES.put("tincountry", "varchar");
        ATTRIBUTE_DATA_TYPES.put("birthdate", "varchar");
        ATTRIBUTE_DATA_TYPES.put("loginname", "varchar");
        ATTRIBUTE_DATA_TYPES.put("extemail", "varchar");
        ATTRIBUTE_DATA_TYPES.put("mobilephone", "varchar");
    }

    private final String entityVersion;

    public HRMSDBView(String propertyFile) throws Exception{
        this(new PropertyReader(CONN_FILE_DIR+"/"+propertyFile));
    }

    public HRMSDBView(PropertyReader propReader) throws Exception{
        this(propReader.getProperty("databaseType"),propReader);
    }

    public HRMSDBView(String connector,PropertyReader propReader) throws Exception{
        super(connector,propReader);
        entityVersion = propReader.getProperty("entityVersion");
    }

    public Collection<AcademicPerson> fetchAll(HashMap<String, String> attributes) throws Exception{
        List<AcademicPerson> retVals = new LinkedList<>();
        String sql = "SELECT hp FROM HRMSPersonEntity_v"+entityVersion+" hp WHERE hp.";
        boolean firstElem=true;
        String gracePeriod=null;
        for (Map.Entry<String, String> entry: attributes.entrySet()){
            String attributeName=entry.getKey();
            String attributeValue=entry.getValue();
            if (attributeName.equals("disabledGracePeriod")){
                gracePeriod= attributeValue;
                continue;
            }

            String attributeType = ATTRIBUTE_DATA_TYPES.get(attributeName.toLowerCase());
            if(attributeType == null || attributeType.equals("")){
                throw new Exception("Field with name '"+attributeName+"' does not have a type registered");
            }
            if(attributeType.equals("number") && !attributeValue.matches("\\d+")){
                return new HashSet<>();
            }
            if (attributeName.equals("disabledGracePeriod")){
                gracePeriod= attributeValue;
                continue;
            }

            if (firstElem) firstElem=false;
            else sql = sql.concat(" AND hp.");

            if(attributeType.equals("varchar")){
                sql = sql.concat(attributeName+"='"+attributeValue+"'");
            }else if(attributeType.equals("number")){
                sql = sql.concat(attributeName+"="+attributeValue);
            }else{
                throw new Exception("Unknown data type '"+attributeType+"' encountered on attribute '"+attributeName+"' on CrossChecker:fetch");
            }
        }

        if(gracePeriod == null){
            sql += " AND hp.employeeStatus IN ('active','interim')";
        }else{
            sql += " AND (hp.employeeStatus IN ('active','interim') OR hp.employeeStatusDate > '"+gracePeriod+"')";
        }

        switch (entityVersion) {
            case "1": {
                List<HRMSPersonEntity_v1> results = select(sql, HRMSPersonEntity_v1.class);
                retVals.addAll(results);
                break;
            }
            case "2": {
                List<HRMSPersonEntity_v2> results = select(sql, HRMSPersonEntity_v2.class);
                retVals.addAll(results);
                break;
            }
            case "3": {
                List<HRMSPersonEntity_v3> results = select(sql, HRMSPersonEntity_v3.class);
                retVals.addAll(results);
                break;
            }
            case "4": {
                List<HRMSPersonEntity_v4> results = select(sql, HRMSPersonEntity_v4.class);
                retVals.addAll(results);
                break;
            }
            case "5": {
                List<HRMSPersonEntity_v5> results = select(sql, HRMSPersonEntity_v5.class);
                retVals.addAll(results);
                break;
            }
            default:
                throw new Exception("Unsupported entity version '" + entityVersion + "' on HRMS DB View.");
        }
        return retVals;
    }
}