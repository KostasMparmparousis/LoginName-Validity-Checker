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
  ServerConfigurations configs;
  public LogoutHandle(ServerConfigurations configs){
    this.configs=configs;
  }
  @Override
  public Object handle(Request request, Response response) throws Exception {
      String confirm=request.queryParams("Confirmation");
      if (confirm.equals("Yes")){
        request.session().attribute("authorized", "false");
        request.session().maxInactiveInterval(3600);
        response.redirect(configs.getConfiguration("base_url"));
      }
      return "";
  }
}