package gr.gunet.uLookup.endpoints.finder;

import gr.gunet.uLookup.tools.ResponseMessages;
import gr.gunet.uLookup.tools.parsers.CustomJsonParser;
import java.io.FileWriter;
import java.io.IOException;
import spark.Request;
import spark.Response;
import spark.Route;

public class FinderRoute implements Route {
    String loginName;
    String institution;
    ResponseMessages responses;
    boolean fromWeb;
    FileWriter myWriter;

    public FinderRoute(String institution) {
        this.institution = institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        responses = new ResponseMessages(req.session().attribute("web"));
        myWriter = new FileWriter("logs/finderLogFile.txt", true);
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
        println("-----------------------------------------------------------");
        println("");
        println("Request Date and Time: " + java.time.LocalDateTime.now());
        println("");
        println("Request Attributes: ");
        println("-LoginName: " + loginName);
        println("-institution: " + institution);

        String response = finder.findRoles(loginName, fromWeb);
        if (!fromWeb){
            println("Response: ");
            println(response);
        }
        myWriter.close();
        return response;
    }

    public void println(String textToPrint) throws IOException{
        textToPrint= textToPrint.concat("\n");
        try{
            myWriter.write(textToPrint);
        }
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}