package play.modules.paginate.strategy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.db.jpa.JPA;
import play.exceptions.UnexpectedException;

public class JPARecordLocatorStrategy<K, T> implements RecordLocatorStrategy<T> {
    private String filter;
    // set when we are filtering the queries to a specific list of ids
    private String keyFilter;
    
    private Object[] params;
    private String orderBy;
    private final Class<T> typeToken;
    private boolean useQueryCache;
    private String key = "id";

    public JPARecordLocatorStrategy(Class<T> typeToken) {
        this.typeToken = typeToken;
        String useQueryCacheStr = Play.configuration.getProperty("paginator.jpa.useQueryCache", "true");
        this.useQueryCache = Boolean.parseBoolean(useQueryCacheStr);
    }

    /**
     * Return the models that have the keys provided.
     * 
     * @param typeToken
     * @param keys
     */
    public JPARecordLocatorStrategy(Class<T> typeToken, List<K> keys) {
        this(typeToken);
        String preparedStatementParameters = StringUtils.repeat("?,", keys.size());
        preparedStatementParameters = preparedStatementParameters.substring(0, preparedStatementParameters.length() - 1);
        this.keyFilter = "IN (" + preparedStatementParameters + ")";
        this.params = keys.toArray();
    }

    /**
     * Return the models that satisfy the provided where clause.
     * 
     * @param typeToken
     * @param filter
     * @param params
     */
    public JPARecordLocatorStrategy(Class<T> typeToken, String filter, Object... params) {
        this(typeToken);
        this.filter = filter;
        this.params = params;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public void withKeyNamed(String key) {
        this.key = key;
    }

    public Class<T> getModel() {
        return typeToken;
    }

    @Override
    public int count() {
        return ((Long) query("COUNT(*)", false).getSingleResult()).intValue();
    }

    @Override
    public int indexOf(T t) {
        return -1;
    }

    @Override
    public int lastIndexOf(T t) {
        return -1;
    }

    @Override
    public List<T> fetchPage(int startRowIdx, int lastRowIdx) {
        List<T> pageValues = findByIndex(startRowIdx, lastRowIdx);
        return pageValues;
    }

    private List<T> findByIndex(int firstRowIdx, int lastRowIdx) {
        int pageSize = lastRowIdx - firstRowIdx;
        @SuppressWarnings("unchecked")
        List<T> returnMe = query(null, true).setFirstResult(firstRowIdx).setMaxResults(pageSize).getResultList();
        return returnMe;
    }

    private static final String SELECT = "SELECT ";

    protected Query query(String select, boolean applyOrderBy) {
        StringBuilder hql = new StringBuilder();
        if (select != null) {
            if (!select.regionMatches(true, 0, SELECT, 0, SELECT.length()))
                hql.append("SELECT ");
            hql.append(select);
            hql.append(' ');
        }
        hql.append("FROM " + getEntityName());
        if (filter != null) {
            hql.append(" WHERE " + filter);
        } else if (keyFilter != null) {
            hql.append(" WHERE " + key + " " + keyFilter);
        }
        if (applyOrderBy) {
            if (orderBy != null) {
                hql.append(" ORDER BY " + orderBy);
            }
        }
        EntityManager em = JPA.em();
        // Play! <= 1.2.3 did not have built-in support for multiple databases...
        // To ensure we are backwards compatible, we use reflection to check that this
        // API is available, so this code still works for people using Play! <= 1.2.3.
        if (typeToken.isAnnotationPresent(PersistenceUnit.class)) {
            String unitName = typeToken.getAnnotation(PersistenceUnit.class).name();
            try {
                Method getJPAConfigMethod = JPA.class.getMethod("getJPAConfig", String.class);
                // guard: only call this code if the user is using a version of Play! that 
                // has the static getJPAConfig method on the JPA class (Play! <= 1.2.3 does not)
                if (getJPAConfigMethod != null) {
                    // use reflection to support Play! <= 1.2.3
                    //em = JPA.getJPAConfig(unitName).getJPAContext().em();
                    Object config = getJPAConfigMethod.invoke(JPA.class, unitName);
                    Method getJPAContextMethod = config.getClass().getMethod("getJPAContext");
                    Object context = getJPAContextMethod.invoke(config);
                    Method emMethod = context.getClass().getMethod("em");
                    em = (EntityManager) emMethod.invoke(context);
                }
            } catch (SecurityException e) {
                // checked exceptions are stupid
                throw new UnexpectedException(e);
            } catch (IllegalArgumentException e) {
                // checked exceptions are stupid
                throw new UnexpectedException(e);
            } catch (NoSuchMethodException e) {
                // checked exceptions are still stupid
                throw new UnexpectedException(e);
            } catch (IllegalAccessException e) {
                // checked exceptions are still stupid
                throw new UnexpectedException(e);
            } catch (InvocationTargetException e) {
                // checked exceptions are still stupid
                throw new UnexpectedException(e);
            }
        }
        Query query = em.createQuery(hql.toString());
        if (useQueryCache) {
            query.setHint("org.hibernate.cacheable", true); 
        }
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
        }
        return query;
    }
    
    private String getEntityName() {
        String entityName = typeToken.getAnnotation(Entity.class).name();
        if (entityName.length() == 0) {
            entityName = typeToken.getSimpleName();
        }
        return entityName;
    }
}
