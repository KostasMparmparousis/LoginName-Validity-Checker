/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.gunet.loginNameValidityChecker.routes.security;

import gr.gunet.loginNameValidityChecker.ServerConfigurations;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Base64;
import java.util.HashMap;
import spark.Request;
import spark.Response;
import spark.Route;

public class ValidateToken implements Route{
    private static final String DEFAULT_AUTH_TOKEN_FILE = "/etc/v_vd/tokens/authorizationTokens";
    
    private final HashMap<String,String> keysToInstitutions;
    
    public ValidateToken() throws Exception{
        this(DEFAULT_AUTH_TOKEN_FILE);
    }
    
    public ValidateToken(String authorizationTokenFileName) throws Exception{
        FileReader tokenFReader = new FileReader(authorizationTokenFileName);
        BufferedReader tokenBReader = new BufferedReader(tokenFReader);
        keysToInstitutions = new HashMap();
        String line;
        while((line = tokenBReader.readLine()) != null){
            String[] split = line.split(":");
            String institution = split[0];
            String key = split[1];
            keysToInstitutions.put(key,institution);
        }
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String reqInstitution=request.queryParams("institution");
        String password=request.queryParams("password");
        reqInstitution=reqInstitution.trim().toLowerCase();
        if(request.session().isNew() || request.session().attribute("authorized") == null || !request.session().attribute("authorized").equals("true")){
            String tokenInstitution = keysToInstitutions.get(password);
            if(tokenInstitution == null || tokenInstitution.trim().equals("")){
                return failedValidationHandle(request, response);
            }
            
            if(!reqInstitution.equals(tokenInstitution)){
                return failedValidationHandle(request, response);
            }
        }
        
        return successfulValidationHandle(request, response, reqInstitution);
    }
    
    private String failedValidationHandle(Request request,Response response){
        response.redirect(ServerConfigurations.getConfiguration("base_url")+"/error.html");
        return "{\"validated\":false}";
    }
    
    private String successfulValidationHandle(Request request,Response response, String institution){
        request.session().attribute("authorized", "true");
        request.session().attribute("institution",institution);
        request.session().maxInactiveInterval(3600);
        String redirectTo = request.session().attribute("originalDestination");
        if(redirectTo == null || redirectTo.equals("")){
            response.redirect(ServerConfigurations.getConfiguration("base_url")+"/index.html");
        }else{
            response.redirect(ServerConfigurations.getConfiguration("base_url")+ redirectTo);
        }
        return "{\"validated\":true}";
    }
}
