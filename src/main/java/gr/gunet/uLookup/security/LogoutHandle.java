package gr.gunet.uLookup.security;
import gr.gunet.uLookup.tools.ServerConfigurations;
import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutHandle implements Route{
  ServerConfigurations configs;
  public LogoutHandle(ServerConfigurations configs){
    this.configs=configs;
  }
  @Override
  public Object handle(Request request, Response response) {
      String confirm=request.queryParams("Confirmation");
      if (confirm.equals("Yes")){
        request.session().attribute("authorized", "false");
        request.session().maxInactiveInterval(3600);
        response.redirect(ServerConfigurations.getConfiguration("base_url"));
      }
      return "";
  }
}