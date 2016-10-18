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

import java.util.List;

import play.db.jpa.GenericModel;

/**
 * This implementation of {@link Paginator} lets you paginate over the rows
 * for a specified Play! framework Model.  The class must extend from the Model
 * helper class. 
 * 
 * @author Lawrence McAlpin
 *
 * @param <T>
 */
public class GenericModelPaginator<T extends GenericModel> extends GenericModelJPAPaginator<Long, T> {
    private static final long serialVersionUID = -2064492602195638937L;

    public GenericModelPaginator(Class<T> typeToken) {
        super(typeToken);
    }
    
    public GenericModelPaginator(Class<T> typeToken, List<Long> keys) {
        super(typeToken, keys);
    }

    public GenericModelPaginator(Class<T> typeToken, String filter, Object... params) {
        super(typeToken, filter, params);
    }
    
    public GenericModelPaginator<T> orderBy(String orderByClause) {
        jpaStrategy().setOrderBy(orderByClause);
        return this;
    }

}
