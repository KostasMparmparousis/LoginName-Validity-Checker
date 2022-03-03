/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.gunet.loginNameValidityChecker.filters;
import spark.Filter;
import spark.Request;
import spark.Response;

public class BasicAuthFilter implements Filter{
    @Override
    public void handle(Request request,Response response) throws Exception {
        if(request.session().isNew()){
            unauthorizedHandle(request, response);
        }

        if(request.session().attribute("authorized") == null){
            unauthorizedHandle(request, response);
        }

        if(!request.session().attribute("authorized").equals("true")){
            unauthorizedHandle(request, response);
        }
    }
    
    private void unauthorizedHandle(Request request,Response response){
        request.session().attribute("originalDestination", request.uri());
        String test1= request.uri();
        String test2= request.url();
        
        response.redirect("/loginPage.html");
    }
}
