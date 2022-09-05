package gr.gunet.uLookup.ldap;

import gr.gunet.uLookup.tools.PropertyReader;
import org.ldaptive.*;
import java.util.Collection;

public class LdapManager {
    protected final static String CONN_FILE_DIR = "/etc/v_vd/conn/";

    private final ConnectionFactory cf;
    private Connection conn = null;
    private final String baseDN;

    public LdapManager(String propFileName) throws Exception{
        this(new PropertyReader(CONN_FILE_DIR+propFileName));
    }

    public LdapManager(PropertyReader institutionProperties) throws Exception{
        String bindDN = institutionProperties.getProperty("LdapManager.bindDN");
        String password = institutionProperties.getProperty("LdapManager.password");
        String url = institutionProperties.getProperty("LdapManager.url");
        this.baseDN = institutionProperties.getProperty("LdapManager.baseDN");

        Credential passwordCred = new Credential(password);
        BindConnectionInitializer connectionInitializer = new BindConnectionInitializer(bindDN, passwordCred);
        ConnectionConfig connConf = new ConnectionConfig(url);
        connConf.setConnectionInitializer(connectionInitializer);
        connConf.setUseSSL(true);
        this.cf = new DefaultConnectionFactory(connConf);
    }

    public void openConnection() throws LdapException{
        conn = cf.getConnection();
        conn.open();
    }

    public void closeConnection(){
        if(conn != null){
            conn.close();
            conn = null;
        }
    }

    public Collection<LdapEntry> search(LdapSearchFilter searchFilter) throws LdapException,Exception{
        if(conn == null){
          throw new Exception("LdapManager does not have a connection open");
        }
        SearchOperation search = new SearchOperation(conn);
        SearchResult result = search.execute(new SearchRequest(searchFilter.getDN(),searchFilter.toString())).getResult();
        return result.getEntries();
    }
    public LdapSearchFilter createSearchFilter(String filter){
        return new LdapSearchFilter(filter,this.baseDN);
    }

    public LdapSearchFilter createSearchFilter(String filter1, String filter2) throws Exception{
        LdapSearchFilter Filter1= createSearchFilter(filter1);
        LdapSearchFilter Filter2= createSearchFilter(filter2);
        return new LdapSearchFilter("and", Filter1,Filter2);
    }
}
