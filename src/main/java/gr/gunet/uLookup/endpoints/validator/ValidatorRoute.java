package gr.gunet.uLookup.endpoints.validator;

import gr.gunet.uLookup.db.personInstances.RequestPerson;
import gr.gunet.uLookup.tools.ResponseMessages;
import gr.gunet.uLookup.tools.parsers.CustomJsonParser;
import spark.Request;
import spark.Response;
import spark.Route;

import java.time.LocalDate;

public class ValidatorRoute implements Route {
    boolean fromWeb;
    boolean verbose = false;
    String disabledGracePeriod = null;
    String institution;
    ResponseMessages responses;

    public ValidatorRoute(String institution) {
        this.institution = institution;
    }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        responses = new ResponseMessages(req.session().attribute("web"));

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

        System.out.println("-----------------------------------------------------------");
        System.out.println();
        System.out.println("Request Date and Time: " + java.time.LocalDateTime.now());
        System.out.println();
        System.out.println("Request Attributes: ");
        System.out.println("-SSN: " + reqPerson.getSSN());
        System.out.println("-SSNCountry: " + reqPerson.getSSNCountry());
        System.out.println("-TIN: " + reqPerson.getTIN());
        System.out.println("-TINCountry: " + reqPerson.getTINCountry());
        System.out.println("-birthDate: " + reqPerson.getBirthDate());
        System.out.println("-disabled Grace Period: " + disabledGracePeriod);
        System.out.println("-loginName: " + reqPerson.getLoginName());
        System.out.println("-institution: " + institution);
        if (verbose) System.out.println("-verbose: YES");
        else System.out.println("-verbose: NO");
        System.out.println();
        System.out.println("Response: ");

        return validator.validateLoginName(reqPerson, fromWeb);
    }

}