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
public class JPAPaginator<K, T> extends Paginator<K, T> implements Serializable {
    private static final long serialVersionUID = -2064492602195638937L;

    public JPAPaginator(Class<T> typeToken, List<K> keys) {
        super(new JPARecordLocatorStrategy(typeToken, keys));
    }

    public JPAPaginator(Class<T> typeToken) {
        super(new JPARecordLocatorStrategy(typeToken));
    }

    public JPAPaginator(Class<T> typeToken, String filter, Object... params) {
        super(new JPARecordLocatorStrategy(typeToken, filter, params));
    }

    public JPAPaginator<K,T> orderBy(String orderByClause) {
        jpaStrategy().setOrderBy(orderByClause);
        return this;
    }

    protected JPARecordLocatorStrategy jpaStrategy() {
        return (JPARecordLocatorStrategy)getRecordLocatorStrategy();
    }
    
    // TODO: try to determine this automagically
    public JPAPaginator withKeyNamed(String key) {
        jpaStrategy().withKeyNamed(key);
        return this;
    }
    
}
