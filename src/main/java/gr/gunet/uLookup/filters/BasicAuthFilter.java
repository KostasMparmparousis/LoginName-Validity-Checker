/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
    ServerConfigurations configs;
  public BasicAuthFilter(String institution, ServerConfigurations configs) {
    this.institution= institution;
    this.configs=configs;
  }
    
    @Override
    public void handle(Request request,Response response) throws Exception {
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
    
    private void unauthorizedHandle(Request request,Response response) throws Exception{
        try {
          if (request.headers("Authorization")!=null) validateToken.handle(request, response);
          else{
            request.session().attribute("originalDestination", request.uri());
            response.redirect(configs.getConfiguration("base_url")+"/loginPage.html");
          }
        } catch (Exception ex) {
          Logger.getLogger(BasicAuthFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}