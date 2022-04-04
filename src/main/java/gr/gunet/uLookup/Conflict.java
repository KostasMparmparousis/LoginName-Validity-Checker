package gr.gunet.uLookup;

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
        String json = "\t\t{";

        json += "\n\t\t\t\"type\":";
        if(type == null || type.trim().equals("")){
            json += "null";
        }else{
            json += "\""+type+"\"";
        }

        json += ",\n\t\t\t\"description\":";
        if(description == null || description.trim().equals("")){
            json += "null";
        }else{
            json += "\""+description+"\"";
        }

        json += ",\n\t\t\t\"conflictingData\":";
        if(conflictingField == null || conflictingField.trim().equals("")){
            json += "null";
        }else{
            json += "\""+conflictingField+"\"";
        }

        json += ",\n\t\t\t\"conflictingRecordKey\":";
        if(conflictingRecordKey == null || conflictingRecordKey.trim().equals("")){
            json += "null";
        }else{
            json += "\""+conflictingRecordKey+"\"";
        }

        json += ",\n\t\t\t\"conflictSource\":";
        if(conflictSource == null || conflictSource.trim().equals("")){
            json += "null";
        }else{
            json += "\""+conflictSource+"\"";
        }
        
        json += ",\n\t\t\t\"requestValue\":";
        if(requestValue == null || requestValue.trim().equals("")){
            json += "null";
        }else{
            json += "\""+requestValue+"\"";
        }
        
        json += ",\n\t\t\t\"conflictingValue\":";
        if(conflictingValue == null || conflictingValue.trim().equals("")){
            json += "null";
        }else{
            json += "\""+conflictingValue+"\"";
        }

        return json+"\n\t\t}";
    }

    public String getConflictSource(){
        return this.conflictSource;
    }

    public String getConflictRecordKey(){
        return this.conflictingRecordKey;
    }
}
