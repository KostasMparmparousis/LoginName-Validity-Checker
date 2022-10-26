package gr.gunet.uLookup.tools;
import java.util.HashMap;

public final class ResponseMessages {
    boolean fromWeb;
    String htmlHeader= "<html><head><meta charset=\"ISO-8859-1\"><title>Response</title><link rel=\"stylesheet\" href=\"../css/style.css\"></head><body>{";
    String htmlFooter="<br>}<br></div></body></html>";
    private static HashMap<String,String> Validator = null;
    private static HashMap<String,String> Proposer = null;
    private static HashMap<String,String> Finder = null;
    private static HashMap<String,String> Error=null;

    public ResponseMessages(String web){
        fromWeb= web.equals("true");

        if (Validator==null){
            Validator = new HashMap<>();
            initValidator();
        }
        if (Proposer==null){
            Proposer = new HashMap<>();
            initProposer();
        }
        if (Finder==null){
            Finder = new HashMap<>();
            initFinder();
        }
        if (Error==null){
            Error = new HashMap<>();
            initError();
        }
    }

    public void initValidator(){
        Validator.put("100", "No conflicts. Also this will probably be the Person's first Account in any registry.");
        Validator.put("101", "No conflicts. The Person has already reserved the requested loginName.");
        Validator.put("102", "No conflicts. The Person owns the requested loginName.");
        Validator.put("110", "No conflicts. But this Person already owns a different loginName.");

        Validator.put("200", "Conflicts found. After the lookup, one or more of the remaining attributes were found to be different.");
        Validator.put("210", "Conflicts found. The requested loginName belongs to someone else. However this Person is already paired with a different loginName.");
        Validator.put("220", "Conflicts found. The requested loginName belongs to someone else.");

        Validator.put("300", "A user with that loginName was found in the DS, but he was not created following the typical Account generation procedure.");
        Validator.put("310", "LoginName was found after lookup, however it is currently linked to a discontinued Account.");
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
        Finder.put("200", "A Person with that loginName was not found.");
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
        return source;
      }
    }
    
    public String getResponse(String code, String content, String title){
        boolean noError = !code.equals("500") && !code.equals("400") && !code.equals("401");
        if (!fromWeb){
            String response="{";
            String response_code = "\"Response code\": " + code;
            response_code = formattedString(response_code, 1) + ",";
            String message;

            if (noError){
                switch (title) {
                    case "Suggested LoginNames":
                        message= "\"Message\": \"" + getProposerMessage(code)+ "\"";
                        break;
                    case "Roles Found":
                        message= "\"Message\": \"" + getFinderMessage(code)+ "\"";
                        break;
                    default:
                        message= "\"Message\": \"" + getValidatorMessage(code)+ "\"";
                        break;
                }
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
        else{
            if (title.equals("")){
              switch(code.charAt(0)){
                case '1':
                    title="No conflicts";
                    break;
                case '2':
                    title="Conflicts found";
                    break;
                case '3':
                    title="Warning";
                    break;
                default:
                    title="Error";
              }
            }
            String htmlBody="<header><h1 style=\"color: #ed7b42;\">" + title + "</h1></header><hr class=\"new1\"><div class=\"sidenav\"><a href=\"../index.html\">Main Hub</a><a href=\"../validator.html\">Validator</a><a href=\"../proposer.html\">Proposer</a><a href=\"../roleFinder.html\">Finder</a></div><div class=\"main\">{";
            String response_code = "\"Response code\": " + boldWord(code);
            response_code = formattedString(response_code, 1) + ",";
            String message;

            if (noError){
                String messageContent;
                switch (title) {
                    case "Suggested LoginNames":
                        messageContent= boldWord(getProposerMessage(code));
                        break;
                    case "Roles Found":
                        messageContent= boldWord(getFinderMessage(code));
                        break;
                    default:
                        messageContent=boldWord(getValidatorMessage(code));
                        break;
                }
                message= "\"Message\": \"" + messageContent + "\"";
                message = formattedString(message,1);
                message+=content;
            }
            else{
                message= "\"Message\": \"" + boldWord(getErrorMessage(code, content))+ "\"";
                message = formattedString(message,1);
            }

            htmlBody= htmlBody + response_code + message;
            return htmlHeader + htmlBody + htmlFooter;
        }
    }

    public String formattedString(String line, int tabs){
      String formattedString="";
      String newLine;
      String tab;
      if (!fromWeb){
        newLine = "\n";
        tab = "\t";
      }
      else{
        newLine = "<br>";
        tab = "&emsp;";
      }
      formattedString+=newLine;
      for (int i=0; i<tabs; i++) formattedString= formattedString.concat(tab);
      formattedString+=line;
      return formattedString;
    }

    public String boldWord(String word){
        return "<b>" + word + "</b>";
    }

}
