package gr.gunet.uLookup.db.ldap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.ldaptive.LdapException;

public class LdapConnectionPool {
  private static final long CONNECTION_LIFETIME = 60000;
  private final static HashMap<String, LdapManager> connections = new HashMap<>();
  private final static HashMap<String,Long> connections_last_usage = new HashMap<>();
  private final String View;

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
      String CONN_FILE_DIR = "/etc/v_vd/conn/";
      Path path= Paths.get(CONN_FILE_DIR + View + "_ds.properties");
      LdapManager connection;
      if (!Files.exists(path)) {
        return null;
      }
      try {
        connection = new LdapManager(View+"_ds.properties");
        connection.openConnection();
        connections.put(View, connection);
      }
      catch(LdapException e){
        e.printStackTrace(System.err);
        throw new Exception("DS");
      }

      long currentTime = System.currentTimeMillis();
      connections_last_usage.put(View, currentTime);
      return connection;
    }
  }

  public static void clean(){

    connections_last_usage.forEach((View, usedAt) -> {
      long currentTime = System.currentTimeMillis();
      if ((currentTime - usedAt) > CONNECTION_LIFETIME) {
        connections_last_usage.remove(View);
        for (Map.Entry<String, LdapManager> conn : connections.entrySet()) {
          if (conn.getKey().equals(View)) {
            conn.getValue().closeConnection();
            connections.remove(View);
          }
        }
      }
    });
  }
}