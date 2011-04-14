/**
 *
 * Copyright 2010, Lawrence McAlpin.
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package play.modules.paginate.locator;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import play.db.jpa.JPA;
import play.modules.paginate.IndexedRecordLocator;

/**
 * Locates a List of JPA-based entities, optionally filtering the results.
 * 
 * @author Lawrence
 * 
 * @param <K>
 * @param <Model>
 */
public class JPAIndexedRecordLocator<K, Model> implements IndexedRecordLocator<K, Model>, Serializable {
    private static final long serialVersionUID = 1847759900112779643L;

    private final Class<Model> typeToken;
    private final String filter;
    private String orderBy;
    private final Object[] params;

    public JPAIndexedRecordLocator(Class<Model> typeToken) {
        this(typeToken, null);
    }

    public JPAIndexedRecordLocator(Class<Model> typeToken, String filter, Object... params) {
        this.typeToken = typeToken;
        this.filter = filter;
        this.params = params;
    }

    public Class<Model> getModel() {
        return typeToken;
    }
    
    @Override
    public int count() {
        return ((Long) query("COUNT(*)", false).getSingleResult()).intValue();
    }

    /**
     * Specifies order by clause for JPAQL query.
     * @param orderBy
     * @return
     */
    public JPAIndexedRecordLocator setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }
    
    @Override
    public List<Model> findByIndex(int firstResult, int pageSize) {
        @SuppressWarnings("unchecked")
        List<Model> returnMe = query(null, true).setFirstResult(firstResult).setMaxResults(pageSize).getResultList();
        return returnMe;
    }

    private static final String SELECT = "SELECT ";

    private Query query(String select, boolean applyOrderBy) {
        StringBuilder hql = new StringBuilder();
        if (select != null) {
            if (!select.regionMatches(true, 0, SELECT, 0, SELECT.length()))
                hql.append("SELECT ");
            hql.append(select);
            hql.append(' ');
        }
        hql.append("FROM " + typeToken.getSimpleName());
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
