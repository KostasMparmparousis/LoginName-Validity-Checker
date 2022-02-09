package gr.gunet.loginNameValidityChecker;
import gr.gunet.loginNameValidityChecker.routes.*;
import spark.Spark;
import spark.servlet.SparkApplication;

public class Main implements SparkApplication {
    public Main(){}
        @Override
        public void init(){
            Spark.post("/loginNameValidator/", new LoginNameValidatorRoute());
            Spark.post("/loginNameValidator", new LoginNameValidatorRoute());
            Spark.post("/loginNameSuggestor/", new LoginNameSuggestorRoute());
            Spark.post("/loginNameSuggestor", new LoginNameSuggestorRoute());
            Spark.post("/roleFinder/", new RoleFinderRoute());
            Spark.post("/roleFinder", new RoleFinderRoute());
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

