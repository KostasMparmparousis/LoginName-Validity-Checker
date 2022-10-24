package gr.gunet.uLookup.endpoints.proposer;

import gr.gunet.uLookup.tools.ResponseMessages;
import gr.gunet.uLookup.tools.parsers.CustomJsonParser;
import java.io.FileWriter;
import java.io.IOException;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;

public class ProposerRoute implements Route {
    boolean fromWeb;
    String SSN;
    String SSNCountry;
    String FN;
    String LN;
    String institution;
    FileWriter myWriter;

    ResponseMessages responses;
    String title;

    public ProposerRoute(String institution) {
        this.institution = institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        responses = new ResponseMessages(req.session().attribute("web"));
        myWriter = new FileWriter("logs/proposerLogFile.txt", true);
        if (!req.session().attribute("authorized").equals("true")) {
            String errorMessage = "You were not authorized";
            return responses.getResponse("401", errorMessage, title);
        }
        if (!req.session().attribute("web").equals("true")) {
            fromWeb = false;
            res.type("application/json");
            CustomJsonParser jsonReader = new CustomJsonParser(req.body());
            SSN = jsonReader.readPropertyAsString("ssn");
            SSNCountry = jsonReader.readPropertyAsString("ssnCountry");
            FN = jsonReader.readPropertyAsString("firstName");
            LN = jsonReader.readPropertyAsString("lastName");
        } else {
            fromWeb = true;
            res.type("text/html");
            SSN = req.queryParams("ssn");
            SSNCountry = req.queryParams("ssnCountry");
            FN = req.queryParams("firstName");
            LN = req.queryParams("lastName");
        }

        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("SSN", SSN);
        attributes.put("ssnCountry", SSNCountry);
        attributes.put("fn", FN);
        attributes.put("ln", LN);

        Proposer proposer= new Proposer(institution,responses);

        println("-----------------------------------------------------------");
        println("");
        println("Request Date and Time: " + java.time.LocalDateTime.now());
        println("");
        println("Request Attributes: ");
        println("-SSN: " + SSN);
        println("-SSNCountry: " + SSNCountry);
        println("-FN: " + FN);
        println("-LN: " + LN);
        println("-institution: " + institution);

        String response= proposer.proposeNames(attributes, fromWeb);
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
