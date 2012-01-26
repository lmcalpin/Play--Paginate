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
package play.modules.paginate;

import java.io.Serializable;
import java.util.List;

import play.db.jpa.GenericModel;
import play.modules.paginate.strategy.JPARecordLocatorStrategy;

/**
 * This implementation of {@link Paginator} lets you paginate over the rows
 * for a specified JPA entity.
 * 
 * @author Lawrence McAlpin
 *
 * @param <K>
 * @param <T>
 */
public class GenericModelJPAPaginator<K, T extends GenericModel> extends Paginator<K, T> implements Serializable {
    private static final long serialVersionUID = -2064492602195638937L;

    public GenericModelJPAPaginator(Class<T> typeToken, List<K> keys) {
        super(new JPARecordLocatorStrategy(typeToken, keys));
    }

    public GenericModelJPAPaginator(Class<T> typeToken) {
        super(new JPARecordLocatorStrategy(typeToken));
    }

    public GenericModelJPAPaginator(Class<T> typeToken, String filter, Object... params) {
        super(new JPARecordLocatorStrategy(typeToken, filter, params));
    }

    public GenericModelJPAPaginator<K,T> orderBy(String orderByClause) {
        jpaStrategy().setOrderBy(orderByClause);
        return this;
    }

    protected JPARecordLocatorStrategy jpaStrategy() {
        return (JPARecordLocatorStrategy)getRecordLocatorStrategy();
    }
}
