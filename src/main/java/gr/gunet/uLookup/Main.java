package gr.gunet.uLookup;
import gr.gunet.uLookup.routes.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.servlet.SparkApplication;
import spark.staticfiles.StaticFilesConfiguration;
import gr.gunet.uLookup.filters.BasicAuthFilter;
import gr.gunet.uLookup.tools.CommandLineParser;

public class Main implements SparkApplication {
    String institution = null;
    public Main(){}
    @Override
    public void init(){
        Spark.ipAddress("127.0.0.1");
        BasicAuthFilter authFilter = new BasicAuthFilter();
        Spark.before("/validator/", authFilter);
        Spark.before("/validator", authFilter);
        Spark.before("/proposer/", authFilter);
        Spark.before("/proposer", authFilter);
        Spark.before("/finder/", authFilter);
        Spark.before("/finder", authFilter);

        Spark.post("/validator/", new LoginNameValidatorRoute(institution));
        Spark.post("/validator", new LoginNameValidatorRoute(institution));
        Spark.post("/proposer/", new LoginNameProposerRoute(institution));
        Spark.post("/proposer", new LoginNameProposerRoute(institution));
        Spark.post("/finder/", new RoleFinderRoute(institution));
        Spark.post("/finder", new RoleFinderRoute(institution));
        Spark.get("/help/", new HelpPageRoute());
        Spark.get("/help", new HelpPageRoute());

        CleanupThread cleanThread= new CleanupThread();
        cleanThread.run();
    }

    @Override
    public void destroy(){}

    public static void main(String[] args){
        CommandLineParser clp = new CommandLineParser(args);
        Main Application= new Main();
        Application.getInstitution(clp);
        Application.init();
    }

    public void getInstitution(CommandLineParser clp){
      institution=clp.getInstitution();
    }
}