package gr.gunet.uLookup.routes;

import spark.Request;
import spark.Response;
import spark.Route;

public class HelpPageRoute implements Route{

    @Override
    public Object handle(Request rqst, Response rspns) throws Exception {
        return "<html><body><p>Placeholder for help section</p></body></html>";
    }
    
}