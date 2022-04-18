/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gr.gunet.uLookup.routes.security;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class ValidateToken implements Route{
    private static final String DEFAULT_AUTH_TOKEN_FILE = "/etc/v_vd/tokens/authorizationTokens";
    private static final String UOP_AUTH_TOKEN_FILE = "/etc/v_vd/tokens/uopTokens";

    private final ArrayList<String> authenticationKeys;

    public ValidateToken() throws Exception{
        this(UOP_AUTH_TOKEN_FILE);
    }

    public ValidateToken(String authorizationTokenFileName) throws Exception{
        FileReader tokenFReader = new FileReader(authorizationTokenFileName);
        BufferedReader tokenBReader = new BufferedReader(tokenFReader);
        authenticationKeys= new ArrayList<>();
        String line;
        while((line = tokenBReader.readLine()) != null){
          authenticationKeys.add(line);
        }
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
//        String reqInstitution=request.queryParams("institution");
//        String password=request.queryParams("password");
//        reqInstitution=reqInstitution.trim().toLowerCase();
        Set<String> headers= request.headers();
        String password= request.headers("Authorization");
        if(request.session().isNew() || request.session().attribute("authorized") == null || !request.session().attribute("authorized").equals("true")){
          if (!authenticationKeys.contains(password)) return failedValidationHandle(request, response);
        }
        return successfulValidationHandle(request, response, "uop");
    }

    private String failedValidationHandle(Request request,Response response){
        return "{\"validated\":false}";
    }

    private String successfulValidationHandle(Request request,Response response, String institution){
        request.session().attribute("authorized", "true");
        request.session().attribute("institution",institution);
        request.session().maxInactiveInterval(3600);
        String redirectTo = request.session().attribute("originalDestination");
        return "{\"validated\":true}";
    }
}