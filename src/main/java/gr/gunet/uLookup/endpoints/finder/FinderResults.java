package gr.gunet.uLookup.endpoints.finder;

import gr.gunet.uLookup.endpoints.Results;
import gr.gunet.uLookup.tools.ResponseMessages;

import java.util.Collection;
import java.util.HashMap;

public class FinderResults implements Results {
    Collection<String> currentRoles;
    String primaryAffiliation;
    ResponseMessages responses;

    public FinderResults(Collection<String> currentRoles, String primaryAffiliation, ResponseMessages responses){
        this.currentRoles=currentRoles;
        this.primaryAffiliation=primaryAffiliation;
        this.responses=responses;
    }
    @Override
    public HashMap<String, String> getResults(boolean fromWeb) {
        String responseCode="100",responseContent,title="Roles Found";
        responseContent= getArray("Affiliations", currentRoles, fromWeb);

        if (primaryAffiliation!=null){
            if (!fromWeb) responseContent= responseContent.concat(",\n\t\"primaryAffiliation\" : \"" + primaryAffiliation + "\"");
            else responseContent= responseContent.concat(",<br>&emsp;\"primaryAffiliation\" : \"" + primaryAffiliation + "\"");
        }
        return Results.sendResponse(responseCode, responseContent, title);
    }

    public String getArray(String arrayName, Collection<String> Names, boolean fromWeb){
        String returnArray="";
        if (Names!=null && !Names.isEmpty()) {
            if (!fromWeb) returnArray = ",\n\t\"" + arrayName + "\":\t[";
            else returnArray = ",<br>&emsp;\"" + arrayName + "\":&emsp;[";
            boolean firstElem = true;

            for (String name : Names) {
                if (firstElem) firstElem = false;
                else returnArray = returnArray.concat(",");

                if (!fromWeb) returnArray = returnArray.concat("\n\t\t");
                else returnArray = returnArray.concat("<br>&emsp;&emsp;");
                returnArray = returnArray.concat("\"" + name + "\"");
            }
            if (!fromWeb) returnArray= returnArray.concat("\n\t]");
            else returnArray= returnArray.concat("<br>&emsp;]");
        }
        return returnArray;
    }
}
