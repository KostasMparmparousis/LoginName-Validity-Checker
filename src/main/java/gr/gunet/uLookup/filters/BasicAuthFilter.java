/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gr.gunet.uLookup.filters;
//import gr.gunet.uLookup.ServerConfigurations;
import spark.Filter;
import spark.Request;
import spark.Response;

public class BasicAuthFilter implements Filter{
    @Override
    public void handle(Request request,Response response) throws Exception {
        if(request.session().isNew()){
            System.out.println(unauthorizedHandle(request, response));
        }
        if(request.session().attribute("authorized") == null){
            System.out.println(unauthorizedHandle(request, response));
        }
        if(!request.session().attribute("authorized").equals("true")){
            System.out.println(unauthorizedHandle(request, response));
        }
    }
    
    private Object unauthorizedHandle(Request request,Response response){
      return "{ error: You were not authorised }";
//        request.session().attribute("originalDestination", request.uri());
//        response.redirect(ServerConfigurations.getConfiguration("base_url")+"/loginPage.html");
    }
}