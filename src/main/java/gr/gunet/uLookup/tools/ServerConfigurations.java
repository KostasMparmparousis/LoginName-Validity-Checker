package gr.gunet.uLookup.tools;

import java.util.HashMap;

public class ServerConfigurations {
    private static HashMap<String,String> configurations = null;
    public ServerConfigurations(String institution, String mode){
      configurations = new HashMap<>();
      if (mode.equals("local")) configurations.put("base_url", "http://localhost:4567");
      else {
        String base_url= "https://ulookup." + institution + ".gr";
        configurations.put("base_url", base_url);
      }
    }

    public static String getConfiguration(String confParam){
        return configurations.get(confParam);
    }
}