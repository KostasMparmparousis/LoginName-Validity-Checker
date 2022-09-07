package gr.gunet.uLookup.endpoints;

import gr.gunet.uLookup.tools.ResponseMessages;
import spark.Request;
import spark.Response;
import spark.Route;


public class HelpPageRoute implements Route{
    String title;
    ResponseMessages responses;
    @Override
    public Object handle(Request req, Response res) {
        responses= new ResponseMessages(req.session().attribute("web"));
        title="Suggested LoginNames";
        String message= "{}";
        if (!req.session().attribute("authorized").equals("true")){
          String errorMessage= "You were not authorized";
          return responses.getResponse("401", errorMessage, title);
        }
        if (!req.session().attribute("web").equals("true")){
          res.type("application/json");
          message= "{ \"message\": \"This is the help section\" }";
        }
        return message;
    }
    
}