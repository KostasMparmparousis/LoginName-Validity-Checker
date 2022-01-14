package gr.gunet.loginNameValidityChecker.ldap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.ldaptive.LdapException;

public class LdapConnectionPool {
  private static final long CONNECTION_LIFETIME = 60000;
  private final static HashMap<String,LdapManager> connections = new HashMap();
  private final static HashMap<String,Long> connections_last_usage = new HashMap();
  private String View;

  public LdapConnectionPool(String View){
    this.View=View;
  }

  public boolean isActive(String View){
    return connections.containsKey(View);
  }

  public LdapManager getConn() throws Exception,LdapException{
    if (isActive(View)){
      long currentTime = System.currentTimeMillis();
      connections_last_usage.put(View, currentTime);
      return connections.get(View);
    }
    else {
      LdapManager connection = new LdapManager(View+"_ds.properties");
      connection.openConnection();
      connections.put(View, connection);
      long currentTime = System.currentTimeMillis();
      connections_last_usage.put(View, currentTime);
      return connection;
    }
  }

  public static void clean(){

    connections_last_usage.entrySet().forEach((entry) ->{
      Long usedAt = entry.getValue();
      long currentTime = System.currentTimeMillis();
      if((currentTime - usedAt) > CONNECTION_LIFETIME) {
        String View = entry.getKey();
        connections_last_usage.remove(View);
        for(Map.Entry<String,LdapManager> conn : connections.entrySet()){
          if (conn.getKey().equals(View)){
            conn.getValue().closeConnection();
            connections.remove(View);
          }
        }
      }
    });
  }
}