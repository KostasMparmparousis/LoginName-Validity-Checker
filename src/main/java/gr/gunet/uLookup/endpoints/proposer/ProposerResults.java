package gr.gunet.uLookup.endpoints.proposer;

import gr.gunet.uLookup.endpoints.Results;
import gr.gunet.uLookup.tools.ResponseMessages;

import java.util.Collection;
import java.util.HashMap;

public class ProposerResults implements Results {
    Collection<String> existingNames;
    Collection<String> newNames;
    HashMap<String,String> attributes;
    ResponseMessages responses;
    public ProposerResults(Collection<String> existingNames, Collection<String> newNames, HashMap<String,String> attributes, ResponseMessages responses){
        this.existingNames=existingNames;
        this.newNames=newNames;
        this.attributes=attributes;
        this.responses=responses;
    }

    @Override
    public HashMap<String, String> getResults(boolean fromWeb) {
        String responseCode,responseContent,title="Suggested LoginNames";
        String suggestedNames;
        String pairedNames;
        String SSN= attributes.get("SSN"), SSNCountry=attributes.get("ssnCountry");

        pairedNames= getArray("personPairedWith", existingNames, fromWeb);
        suggestedNames= getArray("suggestions", newNames, fromWeb);
        responseContent= pairedNames + suggestedNames;

        String firstDigit;
        if (SSN.trim().equals("") || SSNCountry.trim().equals("")){
            firstDigit="3";
        }
        else{
            if (existingNames!=null && !existingNames.isEmpty()){
                firstDigit="1";
            }
            else firstDigit="2";
        }
        if (newNames!=null && !newNames.isEmpty()) responseCode= firstDigit + "00";
        else  responseCode= firstDigit + "10";
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
