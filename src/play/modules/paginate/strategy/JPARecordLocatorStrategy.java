package play.modules.paginate.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import play.db.jpa.JPA;
import play.db.jpa.Model;

public class JPARecordLocatorStrategy<K, T extends Model> implements RecordLocatorStrategy<T> {
    private String filter;
    private Object[] params;
    private String orderBy;
    private final Class<T> typeToken;
    
    public JPARecordLocatorStrategy(Class<T> typeToken) {
        this.typeToken = typeToken;
    }
    
    /**
     * Return the models that have the keys provided.
     * @param typeToken
     * @param keys
     */
    public JPARecordLocatorStrategy(Class<T> typeToken, List<K> keys) {
        this(typeToken);
        String preparedStatementParameters = StringUtils.repeat("?,", keys.size());
        preparedStatementParameters = preparedStatementParameters.substring(0, preparedStatementParameters.length() - 1);
        this.filter = "id IN (" + preparedStatementParameters + ")";
        this.params = keys.toArray();
    }

    /**
     * Return the models that satisfy the provided where clause.
     * @param typeToken
     * @param filter
     * @param params
     */
    public JPARecordLocatorStrategy(Class<T> typeToken, String filter, Object... params) {
        this.typeToken = typeToken;
        this.filter = filter;
        this.params = params;
    }
    
    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
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
        hql.append("FROM " + typeToken.getAnnotation(Entity.class).name());
        if (filter != null) {
            hql.append(" WHERE " + filter);
        }
        if (applyOrderBy) {
            if (orderBy != null) {
                hql.append(" ORDER BY " + orderBy);
            }
        }
        Query query = JPA.em().createQuery(hql.toString());
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
        }
        return query;
    }
}
