/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gr.gunet.uLookup.routes.security;
import gr.gunet.uLookup.ServerConfigurations;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Base64;
import java.util.HashMap;
import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutHandle implements Route{
  @Override
  public Object handle(Request request, Response response) throws Exception {
      String confirm=request.queryParams("Confirmation");
      if (confirm.equals("Yes")){
        request.session().attribute("authorized", "false");
        request.session().attribute("institution","");
        request.session().maxInactiveInterval(3600);
        response.redirect(ServerConfigurations.getConfiguration("base_url"));
      }
//      else{
//        return "";
////        response.redirect(request.uri());
//      }
      return "";
  }
}
