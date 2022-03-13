package gr.gunet.loginNameValidityChecker;

import java.util.HashMap;

public class ServerConfigurations {
    private static HashMap<String,String> configurations = null;
    
    public static String getConfiguration(String confParam){
        if(configurations == null){
            configurations = new HashMap();
//            configurations.put("base_url", "https://uassign.gunet.gr");
            configurations.put("base_url", "https://logassign.gunet.gr");
//            configurations.put("base_url", "http://localhost:4567");
        }
        return configurations.get(confParam);
    }
}
