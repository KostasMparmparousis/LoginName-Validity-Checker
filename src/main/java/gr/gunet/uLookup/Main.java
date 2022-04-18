package gr.gunet.uLookup;
import gr.gunet.uLookup.routes.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.servlet.SparkApplication;
import spark.staticfiles.StaticFilesConfiguration;
import gr.gunet.uLookup.filters.BasicAuthFilter;
import gr.gunet.uLookup.routes.security.ValidateToken;


public class Main implements SparkApplication {
    public Main(){}
    @Override
    public void init(){
        BasicAuthFilter authFilter = new BasicAuthFilter();
        Spark.before("/validator/", authFilter);
        Spark.before("/validator", authFilter);
        Spark.before("/proposer/", authFilter);
        Spark.before("/proposer", authFilter);
        Spark.before("/finder/", authFilter);
        Spark.before("/finder", authFilter);

        ValidateToken validateToken;
        try{
            validateToken = new ValidateToken();
        }catch(Exception e){
            e.printStackTrace(System.err);
            Spark.stop();
            return;
        }

        Spark.post("/validate-token",validateToken);
        Spark.post("/validate-token/",validateToken);

        Spark.post("/validator/", new LoginNameValidatorRoute());
        Spark.post("/validator", new LoginNameValidatorRoute());
        Spark.post("/proposer/", new LoginNameProposerRoute());
        Spark.post("/proposer", new LoginNameProposerRoute());
        Spark.post("/finder/", new RoleFinderRoute());
        Spark.post("/finder", new RoleFinderRoute());
        Spark.get("/help/", new HelpPageRoute());
        Spark.get("/help", new HelpPageRoute());

        CleanupThread cleanThread= new CleanupThread();
        cleanThread.run();
    }

    @Override
    public void destroy(){}

    public static void main(String[] args){
        new Main().init();
    }
}