package gr.gunet.uLookup;
import gr.gunet.uLookup.endpoints.HelpPageRoute;
import gr.gunet.uLookup.endpoints.finder.FinderRoute;
import gr.gunet.uLookup.endpoints.proposer.ProposerRoute;
import gr.gunet.uLookup.endpoints.validator.ValidatorRoute;
import gr.gunet.uLookup.tools.CleanupThread;
import gr.gunet.uLookup.tools.ServerConfigurations;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.servlet.SparkApplication;
import spark.staticfiles.StaticFilesConfiguration;
import gr.gunet.uLookup.security.BasicAuthFilter;
import gr.gunet.uLookup.security.LogoutHandle;
import gr.gunet.uLookup.security.ValidateToken;
import gr.gunet.uLookup.tools.parsers.CommandLineParser;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Main implements SparkApplication {
    String institution = null;
    String mode = null;
    File logFile;
    String log4jConfPath;

    public Main(){}
    @Override
    public void init(){
        Spark.ipAddress("127.0.0.1");
        ServerConfigurations configs= new ServerConfigurations(institution, mode);
        BasicAuthFilter authFilter = new BasicAuthFilter(institution);
        logFile = new File("logs/validatorLogFile.txt");
        try {
            logFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        logFile = new File("logs/proposerLogFile.txt");
        try {
            logFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        logFile = new File("logs/finderLogFile.txt");
        try {
            logFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        String log4jConfPath = "src/main/resources/log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);
        Spark.before("/validator/", authFilter);
        Spark.before("/validator", authFilter);
        Spark.before("/proposer/", authFilter);
        Spark.before("/proposer", authFilter);
        Spark.before("/finder/", authFilter);
        Spark.before("/finder", authFilter);
        Spark.before("/help/", authFilter);
        Spark.before("/help", authFilter);
        Spark.before("/validator.html", authFilter);
        Spark.before("/proposer.html", authFilter);
        Spark.before("/roleFinder.html", authFilter);

        ValidateToken validateToken;
        try{
            validateToken = new ValidateToken(institution, configs);
        }catch(Exception e){
            e.printStackTrace(System.err);
            Spark.stop();
            return;
        }

        LogoutHandle logoutHandle;
        try{
            logoutHandle = new LogoutHandle(configs);
        }catch(Exception e){
            e.printStackTrace(System.err);
            Spark.stop();
            return;
        }

        Spark.post("/validate-token",validateToken);
        Spark.post("/validate-token/",validateToken);
        Spark.post("/logout",logoutHandle);
        Spark.post("/logout/",logoutHandle);

        Spark.post("/validator/", new ValidatorRoute(institution));
        Spark.post("/validator", new ValidatorRoute(institution));
        Spark.post("/proposer/", new ProposerRoute(institution));
        Spark.post("/proposer", new ProposerRoute(institution));
        Spark.post("/finder/", new FinderRoute(institution));
        Spark.post("/finder", new FinderRoute(institution));
        Spark.post("/help/", new HelpPageRoute());
        Spark.post("/help", new HelpPageRoute());
        
        StaticFilesConfiguration staticHandler = new StaticFilesConfiguration();
        staticHandler.configure("/static");
        Spark.before((request, response) -> staticHandler.consume(request.raw(), response.raw()));

        Spark.get("/index/", this::returnStatic);

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
      mode=clp.getMode();
    }

    public String returnStatic(Request request,Response response){
        response.redirect(ServerConfigurations.getConfiguration("base_url")+"index.html");
        return null;
    }
}