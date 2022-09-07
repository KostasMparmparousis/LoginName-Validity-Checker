package gr.gunet.uLookup.tools.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CustomJsonParser {
    private final JsonObject rootObject;

    public CustomJsonParser(String jsonBody) throws JsonSyntaxException{
        JsonParser parser = new JsonParser();
        rootObject = parser.parse(jsonBody).getAsJsonObject();
    }

    private String formattedString(String unformattedString){
        String retVal;
        if(unformattedString == null){
            return null;
        }else{
            retVal = unformattedString.trim();
        }
        if(retVal.equals("")){
            return null;
        }else{
            return retVal;
        }
    }

    public String readPropertyAsStringRaw(String propertyName){
        JsonElement element = rootObject.get(propertyName);
        if(element == null || JsonNull.INSTANCE.equals(element)){
            return null;
        }else if(element.isJsonArray()){
            JsonArray array = element.getAsJsonArray();
            if(array != null){
                return formattedString(array.toString().replaceAll("\"", ""));
                
            }else{
                return null;
            }
        }else if(element.isJsonObject()){
            String temp = element.toString();
            if(temp.equals("{}")){
                return null;
            }else{
                return temp;
            }
        }else{
            return element.getAsString();
        }
    }

    public String readPropertyAsString(String propertyName){
        return formattedString(readPropertyAsStringRaw(propertyName));
    }

    public Boolean readPropertyAsBoolean(String propertyName){
      JsonElement element = rootObject.get(propertyName);
      if (element==null) return false;
      return element.getAsBoolean();
    }

    public String[] readPropertyAsStringArray(String propertyName) {
        JsonElement element = rootObject.get(propertyName);
        if(element == null || JsonNull.INSTANCE.equals(element)){
            return null;
        }else if(element.isJsonArray()){
            JsonArray array = element.getAsJsonArray();
            if(array != null){
                Iterator<JsonElement> arrayIter = array.iterator();
                String[] strArray = new String[array.size()];
                int i = 0;
                while(arrayIter.hasNext()){
                    strArray[i] = arrayIter.next().getAsString();
                    i++;
                }
                return strArray;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    public List<JsonObject> readJsonArrayAsObjectList(){
        LinkedList<JsonObject> retVal = new LinkedList<>();
        if(rootObject.isJsonArray()){
            JsonArray array = rootObject.getAsJsonArray();
            if(array != null){
                for(JsonElement arrayElem : array){
                    if(arrayElem.isJsonObject()){
                        retVal.add(arrayElem.getAsJsonObject());
                    }else{
                        return null;
                    }
                }
            }else{
                return null;
            }
        }else{
            retVal.add(rootObject);
        }
        return retVal;
    }
}
