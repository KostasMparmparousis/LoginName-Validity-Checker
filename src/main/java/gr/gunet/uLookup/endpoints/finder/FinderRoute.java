package gr.gunet.uLookup.endpoints.finder;

import gr.gunet.uLookup.tools.ResponseMessages;
import gr.gunet.uLookup.tools.parsers.CustomJsonParser;
import spark.Request;
import spark.Response;
import spark.Route;

public class FinderRoute implements Route {
    String loginName;
    String institution;
    ResponseMessages responses;

    public FinderRoute(String institution) {
        this.institution = institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        boolean fromWeb;

        responses = new ResponseMessages(req.session().attribute("web"));
        if (!req.session().attribute("authorized").equals("true")) {
            String errorMessage = "You were not authorized";
            return responses.getResponse("401", errorMessage, "Roles Found");
        }

        if (!req.session().attribute("web").equals("true")) {
            res.type("application/json");
            CustomJsonParser jsonReader = new CustomJsonParser(req.body());
            loginName = jsonReader.readPropertyAsString("loginName");
            fromWeb = false;
        } else {
            res.type("text/html");
            loginName = req.queryParams("loginName");
            fromWeb = true;
        }

        Finder finder = new Finder(institution, responses);
        return finder.findRoles(loginName, fromWeb);
    }
}