package gr.gunet.loginNameValidityChecker;
import gr.gunet.loginNameValidityChecker.routes.*;
import spark.Spark;
import spark.servlet.SparkApplication;


public class Main{
    public Main(){}
    public static void main(String[] args) throws Exception {
        //String req = "{'ssn':'287921928',  'ssnCountry': 'GR',  'birthDate': '19920728',  'birthYear': '1992',  'loginName':  'epechlidou', 'verbose': true, 'findExisting': true, 'institution':'ihu'}";
        //String req = "{'ssn':'12312312312',  'ssnCountry': 'GR',  'birthDate': '19770824',  'birthYear': '1977',  'loginName':  'g.vekiaris', 'verbose': true, 'findExisting': true, 'institution':'ihu'}";
        String req = "{'loginName': 'n.aravanis', 'institution': 'ihu'}";
        //String req = "{'ssn':'12312312312',  'ssnCountry': 'GR', 'institution': 'ihu'}";
        //String req = "{'ssn':'18088100898',  'ssnCountry': 'GR', 'institution': 'ihu'}";
        //LoginNameValidatorRoute route = new LoginNameValidatorRoute();
        //LoginNameSuggestorRoute route= new LoginNameSuggestorRoute();
        RoleFinderRoute route= new RoleFinderRoute();
        System.out.println(route.handle(req));
    }

}


// public class Main implements SparkApplication {
//     public Main(){}
//         @Override
//         public void init(){
//             Spark.post("/loginNameValidator/", new LoginNameValidatorRoute());
//             Spark.post("/loginNameValidator", new LoginNameValidatorRoute());
//             Spark.post("/loginNameSuggestor/", new LoginNameSuggestorRoute());
//             Spark.post("/loginNameSuggestor", new LoginNameSuggestorRoute());
//             Spark.post("/roleFinder/", new RoleFinderRoute());
//             Spark.post("/roleFinder", new RoleFinderRoute());
//             Spark.get("/help/", new HelpPageRoute());
//             Spark.get("/help", new HelpPageRoute());
//         }

//         @Override
//         public void destroy(){}

//         public static void main(String[] args){
//             new Main().init();
//             CleanupThread cleanThread= new CleanupThread();
//             cleanThread.run();
//         }
// }

