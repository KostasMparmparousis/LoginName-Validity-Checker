package gr.gunet.loginNameValidityChecker;
import gr.gunet.loginNameValidityChecker.routes.*;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.servlet.SparkApplication;

public class Main implements SparkApplication {
    public Main(){}
    @Override
    public void init(){
        Spark.post("/loginNameValidator/", new LoginNameValidatorRoute());
        Spark.post("/loginNameValidator", new LoginNameValidatorRoute());
        Spark.post("/loginNameSuggester/", new LoginNameSuggesterRoute());
        Spark.post("/loginNameSuggester", new LoginNameSuggesterRoute());
        Spark.post("/roleFinder/", new RoleFinderRoute());
        Spark.post("/roleFinder", new RoleFinderRoute());
        Spark.get("/help/", new HelpPageRoute());
        Spark.get("/help", new HelpPageRoute());
    }

    @Override
    public void destroy(){}

    public static void main(String[] args){
        Spark.staticFiles.location("/static");
        new Main().init();
        Spark.get("/index/", (req,res) -> returnStatic(req,res));
        CleanupThread cleanThread= new CleanupThread();
        cleanThread.run();
    }
    
    public static String returnStatic(Request request,Response response){
        response.redirect("index.html");
        return null;
    }
}