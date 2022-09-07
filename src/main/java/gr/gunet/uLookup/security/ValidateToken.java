package gr.gunet.uLookup.security;
import gr.gunet.uLookup.tools.ServerConfigurations;
import spark.Request;
import spark.Response;
import spark.Route;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class ValidateToken implements Route{
    private static final String API_TOKEN_FILE = "/etc/v_vd/tokens/apiAccessKey";
    private static final String WEB_TOKEN_FILE = "/etc/v_vd/tokens/webAccessKey";
    private final String institution;
    boolean fromWeb;
    ServerConfigurations configs;

    private final ArrayList<String> apiAuthenticationKeys;
    private final ArrayList<String> webAuthenticationKeys;

    public ValidateToken(String institution, ServerConfigurations configs) throws Exception{
        this.institution=institution;
        apiAuthenticationKeys= new ArrayList<>();
        webAuthenticationKeys= new ArrayList<>();

        FileReader apiTokenFReader = new FileReader(API_TOKEN_FILE);
        BufferedReader apiTokenBReader = new BufferedReader(apiTokenFReader);

        FileReader webTokenFReader = new FileReader(WEB_TOKEN_FILE);
        BufferedReader webTokenBReader = new BufferedReader(webTokenFReader);

        String line;
        while((line = apiTokenBReader.readLine()) != null){
          apiAuthenticationKeys.add(line);
        }
        while((line = webTokenBReader.readLine()) != null){
          webAuthenticationKeys.add(line);
        }
        this.configs=configs;
    }
    
    public ValidateToken(String institution) throws Exception{
        this.institution=institution;
        apiAuthenticationKeys= new ArrayList<>();
        webAuthenticationKeys= new ArrayList<>();

        FileReader apiTokenFReader = new FileReader(API_TOKEN_FILE);
        BufferedReader apiTokenBReader = new BufferedReader(apiTokenFReader);

        FileReader webTokenFReader = new FileReader(WEB_TOKEN_FILE);
        BufferedReader webTokenBReader = new BufferedReader(webTokenFReader);

        String line;
        while((line = apiTokenBReader.readLine()) != null){
          apiAuthenticationKeys.add(line);
        }
        while((line = webTokenBReader.readLine()) != null){
          webAuthenticationKeys.add(line);
        }
    }

    @Override
    public Object handle(Request request, Response response) {
        String password;
        fromWeb=false;
        if (request.queryParams("password")!=null) {
          request.session().attribute("web", "true");
          fromWeb=true;
          password=request.queryParams("password");
        }
        else if (request.headers("Authorization")!=null){
          request.session().attribute("web", "false");
          password=request.headers("Authorization");
          if (password.contains(" ")) password= password.split(" ")[1];
        }
        else return failedValidationHandle(request, response);

        if (password.contains(" ")) password= password.split(" ")[1];
        if (password!=null) password=password.trim();

        if(request.session().isNew() || request.session().attribute("authorized") == null || !request.session().attribute("authorized").equals("true")){
            if (fromWeb){
                if (password!=null && !webAuthenticationKeys.contains(password)) return failedValidationHandle(request, response);
            }
            else{
                if (password!=null && !apiAuthenticationKeys.contains(password)) return failedValidationHandle(request, response);
            }
        }
        return successfulValidationHandle(request, response);
    }
    
    private Boolean failedValidationHandle(Request request,Response response){
        request.session().attribute("authorized", "false");
        if (fromWeb) response.redirect(ServerConfigurations.getConfiguration("base_url")+"/error.html");
        return false;
    }

    private Boolean successfulValidationHandle(Request request,Response response){
        request.session().attribute("authorized", "true");
        request.session().attribute("institution",institution);
        request.session().maxInactiveInterval(3600);
        String redirectTo = request.session().attribute("originalDestination");

        if (fromWeb){
          if(redirectTo == null || redirectTo.equals("")){
              response.redirect(ServerConfigurations.getConfiguration("base_url")+"/index.html");
          }else{
              response.redirect(ServerConfigurations.getConfiguration("base_url")+ redirectTo);
          }
        }
        return true;
    }
}