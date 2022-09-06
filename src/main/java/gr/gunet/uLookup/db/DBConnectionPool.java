package gr.gunet.uLookup.db;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DBConnectionPool {
  private final static long CONNECTION_LIFETIME = 60000;
  private final static HashMap<String,SISDBView> SISconnections = new HashMap<>();
  private final static HashMap<String,Long> sis_last_usage = new HashMap<>();
  private final static HashMap<String,HRMSDBView> HRMSconnections = new HashMap<>();
  private final static HashMap<String,Long> hrms_last_usage = new HashMap<>();
  private final static HashMap<String,HRMSDBView> HRMS2connections = new HashMap<>();
  private final static HashMap<String,Long> hrms2_last_usage = new HashMap<>();
  private final String View;
  private final String CONN_FILE_DIR = "/etc/v_vd/conn/";
  
  public DBConnectionPool(String View){
    this.View=View;
  }

  public SISDBView getSISConn() throws Exception{
    Path path= Paths.get(CONN_FILE_DIR+ View + ".properties");
    if (!Files.exists(path)) {
      return null;
    }
    if (SISconnections.containsKey(View)){
      long currentTime = System.currentTimeMillis();
      sis_last_usage.put(View, currentTime);
      return SISconnections.get(View);
    }
    else{
      long currentTime = System.currentTimeMillis();
      sis_last_usage.put(View, currentTime);
      SISDBView connection;
      connection= new SISDBView(View+".properties");
      connection.activate();
      SISconnections.put(View, connection);
      return connection;
    }
  }

  public HRMSDBView getHRMSConn() throws Exception{
    Path path= Paths.get(CONN_FILE_DIR+ View + "_hrms.properties");
    if (!Files.exists(path)) {
      return null;
    }
    if (HRMSconnections.containsKey(View)){
      long currentTime = System.currentTimeMillis();
      hrms_last_usage.put(View, currentTime);
      return HRMSconnections.get(View);
    }
    else{
      long currentTime = System.currentTimeMillis();
      hrms_last_usage.put(View, currentTime);
      HRMSDBView connection;
      connection= new HRMSDBView(View+"_hrms.properties");
      connection.activate();
      HRMSconnections.put(View, connection);
      return connection;
    }
  }

  public HRMSDBView getHRMS2Conn() throws Exception{
    Path path= Paths.get(CONN_FILE_DIR+ View + "_hrms2.properties");
    if (!Files.exists(path)) {
      return null;
    }
    if (HRMS2connections.containsKey(View)){
      long currentTime = System.currentTimeMillis();
      hrms2_last_usage.put(View, currentTime);
      return HRMS2connections.get(View);
    }
    else{
      long currentTime = System.currentTimeMillis();
      hrms2_last_usage.put(View, currentTime);
      HRMSDBView connection;
      connection= new HRMSDBView(View+"_hrms2.properties");
      connection.activate();
      HRMS2connections.put(View, connection);
      return connection;
    }
  }

  public static void clean(){
    sis_last_usage.forEach((View, usedAt) -> {
      long currentTime = System.currentTimeMillis();
      if ((currentTime - usedAt) > CONNECTION_LIFETIME) {
        sis_last_usage.remove(View);
        for (Map.Entry<String, SISDBView> conn : SISconnections.entrySet()) {
          if (conn.getKey().equals(View)) {
            conn.getValue().inactivate();
            SISconnections.remove(View);
          }
        }
      }
    });

    hrms_last_usage.forEach((View, usedAt) -> {
      long currentTime = System.currentTimeMillis();
      if ((currentTime - usedAt) > CONNECTION_LIFETIME) {
        hrms_last_usage.remove(View);
        for (Map.Entry<String, HRMSDBView> conn : HRMSconnections.entrySet()) {
          if (conn.getKey().equals(View)) {
            conn.getValue().inactivate();
            HRMSconnections.remove(View);
          }
        }
      }
    });

    hrms2_last_usage.forEach((View, usedAt) -> {
      long currentTime = System.currentTimeMillis();
      if ((currentTime - usedAt) > CONNECTION_LIFETIME) {
        hrms2_last_usage.remove(View);
        for (Map.Entry<String, HRMSDBView> conn : HRMS2connections.entrySet()) {
          if (conn.getKey().equals(View)) {
            conn.getValue().inactivate();
            HRMS2connections.remove(View);
          }
        }
      }
    });
  }
}
