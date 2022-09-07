package gr.gunet.uLookup.tools;

import gr.gunet.uLookup.db.ldap.LdapConnectionPool;
import gr.gunet.uLookup.db.DBConnectionPool;

public class CleanupThread extends Thread{
  private final static long CLEANUP_INTERVAL = 60000;
  @Override
  public void run() {
    while(true){
        try{
            Thread.sleep(CLEANUP_INTERVAL);
        }
        catch(InterruptedException e){
            System.err.println("CleanThread sleep Interrupted");
        }
        LdapConnectionPool.clean();
        DBConnectionPool.clean();
    }
  }
}
