package gr.gunet.loginNameValidityChecker;

import java.util.HashMap;

public class ServerConfigurations {
    private static HashMap<String,String> configurations = null;
    
    public static String getConfiguration(String confParam){
        if(configurations == null){
            configurations = new HashMap();
            configurations.put("base_url", "https://uassign.gunet.gr");
        }
        return configurations.get(confParam);
    }
}
