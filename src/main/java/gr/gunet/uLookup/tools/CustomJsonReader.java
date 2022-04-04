package gr.gunet.uLookup.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CustomJsonReader {
    private final String rootBody;
    private final JsonObject rootObject;
    private final JsonParser parser;

    public CustomJsonReader(String jsonBody) throws JsonSyntaxException{
        this.rootBody = jsonBody;
        parser = new JsonParser();
        rootObject = parser.parse(rootBody).getAsJsonObject();
    }

    private String formatedString(String unformatedString){
        String retVal;
        if(unformatedString == null){
            return null;
        }else{
            retVal = unformatedString.trim();
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
                return formatedString(array.toString().replaceAll("\"", ""));
                
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
        return formatedString(readPropertyAsStringRaw(propertyName));
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
        List<JsonObject> retVal = new LinkedList();
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
