package gr.gunet.uLookup.endpoints.validator;

import gr.gunet.uLookup.tools.Conflict;
import gr.gunet.uLookup.db.personInstances.RequestPerson;
import gr.gunet.uLookup.tools.ResponseMessages;
import gr.gunet.uLookup.endpoints.Results;

import static gr.gunet.uLookup.endpoints.Results.sendResponse;
import java.util.Collection;
import java.util.HashMap;

public class ValidatorResults implements Results {
    RequestPerson reqPerson;
    Collection<Conflict> conflicts;
    Collection<String> previousLoginNames;
    Collection<String> nameSources;
    Collection<String> nullAttributes;
    ResponseMessages responses;
    public ValidatorResults(RequestPerson reqPerson, Collection<Conflict> conflicts, Collection<String> previousLoginNames, Collection<String> nameSources, Collection<String> nullAttrs, ResponseMessages responses){
        this.reqPerson=reqPerson;
        this.conflicts=conflicts;
        this.previousLoginNames= previousLoginNames;
        this.nameSources= nameSources;
        this.nullAttributes= nullAttrs;
        this.responses=responses;
    }

    @Override
    public HashMap<String,String> getResults(boolean fromWeb) {
        String responseCode = "",responseContent ="",title="";
        boolean verbose=reqPerson.getVerbose();
        if (!conflicts.isEmpty()){
            responseCode+="2";
            if (verbose){
                responseContent+= ("," + responses.formattedString("\"conflicts\": ", 1));
                boolean firstElem = true;
                for(Conflict conflict : conflicts){
                    if(firstElem){
                        firstElem = false;
                        responseContent= responseContent.concat("[");
                    }else{
                        responseContent= responseContent.concat(",");
                    }
                    responseContent= responseContent.concat(conflict.toJson(fromWeb));
                }
                responseContent+= responses.formattedString("]", 1);
            }
        }
        else{
            if (nullAttributes!=null && !nullAttributes.isEmpty()){
                if (nullAttributes.contains("ssn") || nullAttributes.contains("ssnCountry")){
                    responseCode="310";
                    responseContent="";
                    return sendResponse(responseCode, responseContent, title);
                }
            }
            responseCode+="1";
        }

        String foundNames="";
        if (!previousLoginNames.isEmpty()){
            foundNames+= ( "," + responses.formattedString("\"personPairedWith\": [", 1));
            boolean firstElem = true;
            for (String loginName: previousLoginNames){
                if (loginName.equals(reqPerson.getLoginName())) continue;
                if (firstElem) firstElem=false;
                else foundNames=foundNames.concat(",");
                foundNames = foundNames.concat(responses.formattedString("\"" + loginName + "\"", 2));
            }
            if (previousLoginNames.contains(reqPerson.getLoginName())) { //
                responseCode+="0";
                if (conflicts.isEmpty()){
                    if (nameSources.isEmpty()) responseCode+="1"; //101
                    else responseCode+="2"; //102
                }
                else responseCode+="0"; //200
            }
            else{
                responseCode += "1"; //X10
                foundNames+= responses.formattedString("]", 1);
                responseContent += foundNames;
            }
        }
        else {
            if (conflicts.isEmpty()) responseCode += "00"; //100
            else responseCode+="20"; //220
        }
        if (responseCode.length()<3) responseCode+="0";
        return Results.sendResponse(responseCode,responseContent,title);
    }

}
