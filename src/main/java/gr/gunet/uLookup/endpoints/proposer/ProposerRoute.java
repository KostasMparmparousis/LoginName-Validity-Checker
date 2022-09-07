package gr.gunet.uLookup.endpoints.proposer;

import gr.gunet.uLookup.tools.ResponseMessages;
import gr.gunet.uLookup.tools.parsers.CustomJsonParser;
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

    ResponseMessages responses;
    String title;

    public ProposerRoute(String institution) {
        this.institution = institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        responses = new ResponseMessages(req.session().attribute("web"));
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

        System.out.println("-----------------------------------------------------------");
        System.out.println();
        System.out.println("Request Date and Time: " + java.time.LocalDateTime.now());
        System.out.println();
        System.out.println("Request Attributes: ");
        System.out.println("-SSN: " + SSN);
        System.out.println("-SSNCountry: " + SSNCountry);
        System.out.println("-FN: " + FN);
        System.out.println("-LN: " + LN);
        System.out.println("-institution: " + institution);

        System.out.println("Response: ");
        return proposer.proposeNames(attributes, fromWeb);
    }
}
