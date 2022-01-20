package gr.gunet.loginNameValidityChecker.ldap;

import gr.gunet.loginNameValidityChecker.tools.PropertyReader;
import gr.gunet.loginNameValidityChecker.AcademicPerson;
import java.util.Collection;
import java.util.LinkedList;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import java.util.List;

public class LdapManager {
    protected final static String CONN_FILE_DIR = "./etc/v_vd/conn/";

    private final ConnectionFactory cf;
    private Connection conn = null;
    private String baseDN;

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

    public boolean isActive(){
        if (conn==null) return false;
        return true;
    }

    public Collection<LdapEntry> search(LdapSearchFilter searchFilter) throws LdapException,Exception{
        if(conn == null){
          throw new Exception("LdapManager does not have a connection open");
        }

        SearchOperation search = new SearchOperation(conn);
        SearchResult result = search.execute(new SearchRequest(searchFilter.getDN(),searchFilter.toString())).getResult();
        Collection<LdapEntry> entries = result.getEntries();
        
        return entries;
    }

    public Collection<LdapEntry> search(String attribute,String operation,String value) throws Exception{
        return search(createSearchFilter(attribute,operation,value));
    }

    public Collection<LdapEntry> search(String filter) throws Exception{
        return search(createSearchFilter(filter));
    }

    public LdapSearchFilter createSearchFilter(String attribute,String operation,String value){
        return new LdapSearchFilter(attribute,operation,value,this.baseDN);
    }

    public LdapSearchFilter createSearchFilter(String filter){
        return new LdapSearchFilter(filter,this.baseDN);
    }

    public LdapSearchFilter createSearchFilter(String filter1, String filter2) throws Exception{
        LdapSearchFilter Filter1= createSearchFilter(filter1);
        LdapSearchFilter Filter2= createSearchFilter(filter2);
        return new LdapSearchFilter("and", Filter1,Filter2);
    }

    public LdapSearchFilter createSearchFilter(AcademicPerson person) throws Exception{
        List<LdapSearchFilter> filters = new LinkedList();
        filters.add(createSearchFilter(("(schGrAcPersonID=*)")));
        filters.add(createSearchFilter(("schGrAcPersonSSN="+person.getSsn())));
        if (person.getTin()!=null) filters.add(createSearchFilter(("schGrAcPersonTIN="+person.getTin())));
        if (person.getBirthDate()!=null) filters.add(createSearchFilter(("schacDateOfBirth="+person.getBirthDate())));
        if (person.getBirthYear()!=null) filters.add(createSearchFilter(("schacYearOfBirth="+person.getBirthYear())));
        return new LdapSearchFilter("and", filters);
    }
}
