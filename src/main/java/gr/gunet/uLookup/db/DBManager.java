package gr.gunet.uLookup.db;

import gr.gunet.uLookup.tools.PropertyReader;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class DBManager {
    protected final static String CONN_FILE_DIR = "/etc/v_vd/conn";
    private final EntityManagerFactory emFactory;
    private EntityManager entityManager;

    public DBManager(String connector,PropertyReader propReader){
        emFactory = Persistence.createEntityManagerFactory(connector,propReader.getPropertiesObject());
        entityManager = null;
    }

    public <T> List<T> select(String sql,Class<T> entityClass) throws Exception{
        return select(sql,entityClass,null,null);
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
        return query.getResultList();
    }
    
    public void inactivate(){
        if(entityManager != null){
            entityManager.close();
            entityManager=null;
        }
    }

    public void activate(){
        entityManager = emFactory.createEntityManager();
    }
}