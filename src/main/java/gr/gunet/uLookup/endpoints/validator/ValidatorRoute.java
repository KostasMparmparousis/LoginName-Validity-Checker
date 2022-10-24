package gr.gunet.uLookup.endpoints.validator;

import gr.gunet.uLookup.db.personInstances.RequestPerson;
import gr.gunet.uLookup.tools.ResponseMessages;
import gr.gunet.uLookup.tools.parsers.CustomJsonParser;
import spark.Request;
import spark.Response;
import spark.Route;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class ValidatorRoute implements Route {
    boolean fromWeb;
    boolean verbose = false;
    String disabledGracePeriod = null;
    String institution;
    ResponseMessages responses;
    FileWriter myWriter;

    public ValidatorRoute(String institution) {
        this.institution = institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        responses = new ResponseMessages(req.session().attribute("web"));
        myWriter = new FileWriter("logs/validatorLogFile.txt", true);

        RequestPerson reqPerson;
        if (!req.session().attribute("authorized").equals("true")) {
            String errorMessage = "You were not authorized";
            return responses.getResponse("401", errorMessage, "");
        }

        if (!req.session().attribute("web").equals("true")) {
            fromWeb = false;
            res.type("application/json");
            CustomJsonParser jsonReader = new CustomJsonParser(req.body());
            disabledGracePeriod = jsonReader.readPropertyAsString("disabledGracePeriod");
            try {
                reqPerson = new RequestPerson(jsonReader);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                String errorMessage = e.getMessage();
                return responses.getResponse("400", errorMessage, "");
            }
        } else {
            fromWeb = true;
            res.type("text/html");
            disabledGracePeriod = req.queryParams("disabledGracePeriod");
            try {
                reqPerson = new RequestPerson(req);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                String errorMessage = e.getMessage();
                return responses.getResponse("400", errorMessage, "");
            }
        }
        if (disabledGracePeriod == null || disabledGracePeriod.trim().equals("")) {
            disabledGracePeriod = null;
        } else if (disabledGracePeriod.length() < 3) {
            LocalDate ld = java.time.LocalDate.now().minusMonths(Integer.parseInt(disabledGracePeriod));
            disabledGracePeriod = ld.toString();
            disabledGracePeriod = disabledGracePeriod.replace("-", "");
        }
        Validator validator= new Validator(institution, disabledGracePeriod, responses);
        verbose = reqPerson.getVerbose();

        println("-----------------------------------------------------------");
        println("");
        println("Request Date and Time: " + java.time.LocalDateTime.now());
        println("");
        println("Request Attributes: ");
        println("-SSN: " + reqPerson.getSSN());
        println("-SSNCountry: " + reqPerson.getSSNCountry());
        println("-TIN: " + reqPerson.getTIN());
        println("-TINCountry: " + reqPerson.getTINCountry());
        println("-birthDate: " + reqPerson.getBirthDate());
        println("-disabled Grace Period: " + disabledGracePeriod);
        println("-loginName: " + reqPerson.getLoginName());
        println("-institution: " + institution);
        if (verbose) println("-verbose: YES");
        else println("-verbose: NO");
        println("");
        String response= validator.validateLoginName(reqPerson, fromWeb);
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