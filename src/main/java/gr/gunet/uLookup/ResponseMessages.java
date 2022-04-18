package gr.gunet.uLookup;
import java.util.HashMap;

public class ResponseMessages {
    private static HashMap<String,String> Validator = null;
    private static HashMap<String,String> Proposer = null;
    private static HashMap<String,String> Finder = null;
    private static HashMap<String,String> Error=null;

    public ResponseMessages(){
        if (Validator==null){
            Validator = new HashMap();
            initValidator();
        }
        if (Proposer==null){
            Proposer = new HashMap();
            initProposer();
        }
        if (Finder==null){
            Finder = new HashMap();
            initFinder();
        }
        if (Error==null){
            Error = new HashMap();
            initError();
        }
    }

    public void initValidator(){
        Validator.put("100", "No conflicts. Also this will probably be the Person's first Account in any registry.");
        Validator.put("101", "No conflicts. The Person has already reserved the requested loginName.");
        Validator.put("102", "No conflicts. The Person owns the requested loginName.");
        Validator.put("110", "No conflicts. But this Person already owns a different loginName.");

        Validator.put("200", "Conflicts found. After the lookup SSN-SSNCountry were identical, but one or more of the remaining attributes were different.");
        Validator.put("210", "Conflicts found. The requested loginName belongs to someone else. However this Person is already paired with a different loginName.");
        Validator.put("220", "Conflicts found. The requested loginName belongs to someone else.");

        Validator.put("300", "A user with that loginName was found in the DS, but he was not created following the typical Account generation procedure.");
        Validator.put("310", "LoginName was found after lookup, however we can not examine if it belongs to the same Person. One or more primary identifiers were NULL.");
    }

    public void initProposer(){
        Proposer.put("100", "Person already owns a loginName. Also the following loginNames are available.");
        Proposer.put("110", "Person already owns a loginName.");
        Proposer.put("200", "The following loginNames are available.");
        Proposer.put("210", "No suggestions available.");
        Proposer.put("300", "The following loginNames are available.");
        Proposer.put("310", "No suggestions available.");
    }

    public void initFinder(){
        Finder.put("000", "LoginName given was not found between active Accounts in any registries.");
    }

    public void initError(){
        Error.put("500", "Could not connect to the ");
    }

    public String getValidatorMessage(String code){
        return Validator.get(code);
    }

    public String getProposerMessage(String code){
        return Proposer.get(code);
    }

    public String getFinderMessage(String code){
        return Finder.getOrDefault(code, "Person with given loginName was found.");
    }

    public String getErrorMessage(String code, String source){
      if (code.equals("500")) return Error.get(code) + source + ".";
      else {
        String content= source;
        return content;
      }
    }

    public String getValidatorResponse(String code, String content){
      String response="{";
      String response_code = "\"Response code\": " + code;
        response_code = formattedString(response_code, 1) + ",";
        String message="";

        if (!code.equals("500") && !code.equals("400") && !code.equals("401")){
            message= "\"Message\": \"" + getValidatorMessage(code)+ "\"";
            message = formattedString(message,1);
            message+=content;
        }
        else{
            message= "\"Message\": \"" + getErrorMessage(code, content)+ "\"";
            message = formattedString(message,1);
        }

        response= response + response_code + message + "\n}";
        return response;
    }

    public String formattedString(String line, int tabs){
        String formattedString="";
        String newLine = "\n";
        String tab = "\t";
        formattedString+=newLine;
        for (int i=0; i<tabs; i++) formattedString+=tab;
        formattedString+=line;
        return formattedString;
    }

}
