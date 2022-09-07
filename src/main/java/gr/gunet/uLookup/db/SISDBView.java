package gr.gunet.uLookup.db;

import gr.gunet.uLookup.db.personInstances.AcademicPerson;
import gr.gunet.uLookup.db.viewEntities.SISPersonEntity_v1;
import gr.gunet.uLookup.db.viewEntities.SISPersonEntity_v2;
import gr.gunet.uLookup.db.viewEntities.SISPersonEntity_v3;
import gr.gunet.uLookup.tools.parsers.PropertyParser;

import java.util.*;

public class SISDBView extends DBManager{
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

    public SISDBView(String propertyFile) throws Exception{
        this(new PropertyParser(CONN_FILE_DIR+"/"+propertyFile));
    }

    public SISDBView(PropertyParser propReader) throws Exception{
        this(propReader.getProperty("databaseType"),propReader);
    }

    public SISDBView(String connector, PropertyParser propReader) throws Exception{
        super(connector,propReader);
        this.entityVersion = propReader.getProperty("entityVersion");
    }

    public Collection<AcademicPerson> fetchAll(HashMap<String, String> attributes) throws Exception{
        List<AcademicPerson> retVals = new LinkedList<>();
        String sql = "SELECT sp FROM SISPersonEntity_v"+entityVersion+" sp WHERE sp.";
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

            if (firstElem) firstElem=false;
            else sql = sql.concat(" AND sp.");

            if(attributeType.equals("varchar")){
                sql = sql.concat(attributeName+"='"+attributeValue+"'");
            }else if(attributeType.equals("number")){
                sql = sql.concat(attributeName+"="+attributeValue);
            }else{
                throw new Exception("Unknown data type '"+attributeType+"' encountered on attribute '"+attributeName+"' on CrossChecker:fetch");
            }
        }

        if(gracePeriod == null){
            sql += " AND sp.enrollmentStatus IN ('active','interim')";
        }else{
            sql += " AND (sp.enrollmentStatus IN ('active','interim') OR sp.enrollmentStatusDate > '"+gracePeriod+"')";
        }

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
}
