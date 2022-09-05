package gr.gunet.uLookup.filters;
import gr.gunet.uLookup.ServerConfigurations;
import gr.gunet.uLookup.routes.security.ValidateToken;
import java.util.logging.Level;
import java.util.logging.Logger;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;


public class BasicAuthFilter implements Filter{
    ValidateToken validateToken;
    String institution;
  public BasicAuthFilter(String institution) {
    this.institution= institution;
  }
    
    @Override
    public void handle(Request request,Response response) {
        try{
            validateToken = new ValidateToken(institution);
        }catch(Exception e){
            e.printStackTrace(System.err);
            Spark.stop();
            return;
        }

        if(request.session().isNew()){
            unauthorizedHandle(request, response);
        }
        else if(request.session().attribute("authorized") == null){
            unauthorizedHandle(request, response);
        }
        else if(!request.session().attribute("authorized").equals("true")){
            unauthorizedHandle(request, response);
        }
    }
    
    private void unauthorizedHandle(Request request,Response response) {
        try {
          if (request.headers("Authorization")!=null) validateToken.handle(request, response);
          else{
            request.session().attribute("originalDestination", request.uri());
            response.redirect(ServerConfigurations.getConfiguration("base_url")+"/loginPage.html");
          }
        } catch (Exception ex) {
          Logger.getLogger(BasicAuthFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}