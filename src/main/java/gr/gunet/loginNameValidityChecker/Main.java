package gr.gunet.loginNameValidityChecker;
import gr.gunet.loginNameValidityChecker.routes.LoginNameValidityCheckRoute;
import gr.gunet.loginNameValidityChecker.routes.HelpPageRoute;
import spark.Spark;
import spark.servlet.SparkApplication;

  public class Main implements SparkApplication{
        public Main(){}
        @Override
        public void init(){
            Spark.post("/", new LoginNameValidityCheckRoute());
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