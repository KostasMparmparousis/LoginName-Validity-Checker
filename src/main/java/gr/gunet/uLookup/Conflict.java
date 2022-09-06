package gr.gunet.uLookup;

import java.util.LinkedHashMap;
import java.util.Map;

public class Conflict {
    LinkedHashMap<String, String> attributes;
    
    public Conflict(String type,String desc,String conflField,String conflRecKey,String conflSrc, String requestValue, String conflictingValue){
        attributes= new LinkedHashMap<>();
        attributes.put("type", type);
        attributes.put("description", desc);
        attributes.put("conflictingData", conflField);
        attributes.put("conflictingRecordKey", conflRecKey);
        attributes.put("conflictSource", conflSrc);
        attributes.put("requestValue", requestValue);
        attributes.put("conflictingValue", conflictingValue);
    }
    
    public String toJson(boolean fromWeb){
        String doubleTab;
        String tripleTab;
        String lineBreak;
        if (!fromWeb){
            doubleTab="\t\t";
            tripleTab="\t\t\t";
            lineBreak="\n";
        }
        else{
            doubleTab="&emsp;&emsp;";
            tripleTab="&emsp;&emsp;&emsp;";
            lineBreak="<br>";
        }
        String json=lineBreak+doubleTab+"{";

        boolean firstElem=true;
        for (Map.Entry<String, String> entry: attributes.entrySet()) {
            String attributeName = "\"" + entry.getKey() + "\":";
            String attributeValue = entry.getValue();

            if (firstElem) firstElem=false;
            else json+=",";
            json+=lineBreak+tripleTab+attributeName;
            if(attributeValue == null || attributeValue.trim().equals("")){
                json += "null";
            }else{
                json += "\""+attributeValue+"\"";
            }
        }
        return json+ lineBreak + doubleTab + "}";
    }
}
