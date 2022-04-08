package gr.gunet.uLookup.db;

import gr.gunet.uLookup.tools.PropertyReader;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

public class DBManager {
    protected final static String CONN_FILE_DIR = "/etc/v_vd/conn";

    private final EntityManagerFactory emFactory;
    private EntityManager entityManager;
    
    private EntityTransaction transaction;
    private boolean active;
    private boolean activeDueToTransaction;

    private String connector;
    private PropertyReader propReader;

    public DBManager(String connector,String propFileName) throws Exception{
        this(connector,new PropertyReader(CONN_FILE_DIR+"/"+propFileName));
    }

    public DBManager(String connector,PropertyReader propReader){
        emFactory = Persistence.createEntityManagerFactory(connector,propReader.getPropertiesObject());
        entityManager = null;
        transaction = null;
        active = true;
        activeDueToTransaction = true;
        this.connector = connector;
        this.propReader = propReader;
    }

    public DBManager(DBManager dbmanager) throws Exception{
        this(dbmanager.connector,dbmanager.propReader);
    }

    public <T> List<T> select(String sql,Class<T> entityClass) throws Exception{
        return select(sql,entityClass,null,null);
    }

    public <T> List<T> select(String sql,Class<T> entityClass,Integer limit) throws Exception{
        return select(sql,entityClass,null,limit);
    }

    public <T> List<T> select(String sql,Class<T> entityClass,Integer page,Integer pageSize) throws Exception{
        if(entityManager == null){
          throw new Exception("DBManager does not have a connection open");
        }
        TypedQuery<T> query = entityManager.createQuery(sql,entityClass);
        if(pageSize != null){
            query.setMaxResults(pageSize);
            if(page != null){
                query.setFirstResult((page-1)*pageSize);
            }
        }
        List<T> results = query.getResultList();

        return results;
    }
    
    public void inactivate(){
        if(entityManager != null){
            entityManager.close();
            entityManager=null;
        }
        active = false;
        activeDueToTransaction = false;
    }

    public void activate(){
        active= true;
        entityManager = emFactory.createEntityManager();
    }

    public boolean isActive(){
        return active;
    }

    public void close(){
        if(active){
            inactivate();
        }
        emFactory.close();
    }
}