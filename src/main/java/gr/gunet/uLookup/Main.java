package gr.gunet.uLookup;
import gr.gunet.uLookup.routes.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.servlet.SparkApplication;
import spark.staticfiles.StaticFilesConfiguration;


public class Main implements SparkApplication {
    public Main(){}
    @Override
    public void init(){
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