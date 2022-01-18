package gr.gunet.loginNameValidityChecker;
import gr.gunet.loginNameValidityChecker.routes.HelpPageRoute;
import gr.gunet.loginNameValidityChecker.routes.LoginNameConflictDetectorRoute;
import gr.gunet.loginNameValidityChecker.routes.LoginNameGeneratorRoute;
import gr.gunet.loginNameValidityChecker.routes.LoginNameSearcherRoute;
import spark.Spark;
import spark.servlet.SparkApplication;

public class Main implements SparkApplication {
    public Main(){}
        @Override
        public void init(){
            Spark.post("/conflictDetector/", new LoginNameConflictDetectorRoute());
            Spark.post("/conflictDetector", new LoginNameConflictDetectorRoute());
            Spark.post("/loginNameSearcher/", new LoginNameSearcherRoute());
            Spark.post("/loginNameSearcher", new LoginNameSearcherRoute());
            Spark.post("/loginNameGenerator/", new LoginNameGeneratorRoute());
            Spark.post("/loginNameGenerator", new LoginNameGeneratorRoute());
            Spark.get("/help/", new HelpPageRoute());
            Spark.get("/help", new HelpPageRoute());
        }

        @Override
        public void destroy(){}

        public static void main(String[] args){
            new Main().init();
            CleanupThread cleanThread= new CleanupThread();
            cleanThread.run();
        }
}

