package gr.gunet.loginNameValidityChecker;

public class Conflict {
    private String type;
    private String description;
    private String conflictingField;
    private String conflictingRecordKey;
    private String conflictSource;
    private String requestValue;
    private String conflictingValue;
    
    
    public Conflict(String type,String desc,String conflField,String conflRecKey,String conflSrc, String requestValue, String conflictingValue){
        this.type = type;
        this.description = desc;
        this.conflictingField = conflField;
        this.conflictingRecordKey = conflRecKey;
        this.conflictSource = conflSrc;
        this.requestValue= requestValue;
        this.conflictingValue= conflictingValue;
    }
    
    public String toJson(){
        String json = "    {";

        json += "\n      \"type\":";
        if(type == null || type.trim().equals("")){
            json += "null";
        }else{
            json += "\""+type+"\"";
        }

        json += ",\n      \"description\":";
        if(description == null || description.trim().equals("")){
            json += "null";
        }else{
            json += "\""+description+"\"";
        }

        json += ",\n      \"conflictingData\":";
        if(conflictingField == null || conflictingField.trim().equals("")){
            json += "null";
        }else{
            json += "\""+conflictingField+"\"";
        }

        json += ",\n      \"conflictingRecordKey\":";
        if(conflictingRecordKey == null || conflictingRecordKey.trim().equals("")){
            json += "null";
        }else{
            json += "\""+conflictingRecordKey+"\"";
        }

        json += ",\n      \"conflictSource\":";
        if(conflictSource == null || conflictSource.trim().equals("")){
            json += "null";
        }else{
            json += "\""+conflictSource+"\"";
        }
        
        json += ",\n      \"requestValue\":";
        if(requestValue == null || requestValue.trim().equals("")){
            json += "null";
        }else{
            json += "\""+requestValue+"\"";
        }
        
        json += ",\n      \"conflictingValue\":";
        if(conflictingValue == null || conflictingValue.trim().equals("")){
            json += "null";
        }else{
            json += "\""+conflictingValue+"\"";
        }

        return json+"\n    }";
    }

    public String getConflictSource(){
        return this.conflictSource;
    }

    public String getConflictRecordKey(){
        return this.conflictingRecordKey;
    }
}
