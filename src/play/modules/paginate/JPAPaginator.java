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

import play.db.jpa.Model;
import play.modules.paginate.locator.JPAIndexedRecordLocator;
import play.modules.paginate.locator.JPAKeyedRecordLocator;

public class JPAPaginator<K, T extends Model> extends Paginator<K, T> implements Serializable {
    private static final long serialVersionUID = -2064492602195638937L;

    private KeyedRecordLocator<K, T> keyedRecordLocator;
    private IndexedRecordLocator<K, T> indexedRecordLocator;

    public JPAPaginator(Class<T> typeToken, List<K> keys) {
        super(typeToken, keys);
    }

    public JPAPaginator(List<T> values) {
        super(values);
    }

    @Deprecated
    public JPAPaginator(Class<T> typeToken, long rowCount) {
        this(typeToken, (int) rowCount);
    }

    /**
     * Use JPAPaginator(typeToken, indexedRecordLocator) instead
     */
    @Deprecated
    public JPAPaginator(Class<T> typeToken, long rowCount, IndexedRecordLocator<K, T> locator) {
        this(typeToken, (int) rowCount, locator);
    }

    @Deprecated
    public JPAPaginator(Class<T> typeToken, int rowCount) {
        super(typeToken, rowCount);
    }

    /**
     * Use JPAPaginator(typeToken, indexedRecordLocator) instead
     */
    @Deprecated
    public JPAPaginator(Class<T> typeToken, int rowCount, IndexedRecordLocator<K, T> locator) {
        super(typeToken, rowCount);
        this.indexedRecordLocator = locator;
    }

    public JPAPaginator(Class<T> typeToken, IndexedRecordLocator<K, T> locator) {
        super(typeToken, locator.count());
        this.indexedRecordLocator = locator;
    }
    
    public JPAPaginator(JPAIndexedRecordLocator<K, T> locator) {
        super(locator.getModel(), locator.count());
        this.indexedRecordLocator = locator;
    }
    
    /**
     * Convenience method that creates a JPAIndexedRecordLocator with a
     * where clause filter.
     */
    public JPAPaginator(Class<T> typeToken, String filter, Object... params) {
        super(typeToken);
        this.indexedRecordLocator = new JPAIndexedRecordLocator<K, T>(typeToken, filter, params);
        this.setRowCount(indexedRecordLocator.count());
    }

    protected JPAPaginator() {
    }
    
    @Override
    protected KeyedRecordLocator<K, T> getKeyedRecordLocator() {
        if (typeToken == null)
            throw new IllegalStateException(
                    "Record locators are only used when the list paginates over keys; a type token is required");
        if (keyedRecordLocator == null) {
            keyedRecordLocator = new JPAKeyedRecordLocator<K, T>(typeToken);
        }
        return keyedRecordLocator;
    }

    @Override
    protected IndexedRecordLocator<K, T> getIndexedRecordLocator() {
        if (typeToken == null)
            throw new IllegalStateException(
                    "Record locators are only used when the list paginates over keys; a type token is required");
        if (indexedRecordLocator == null) {
            indexedRecordLocator = new JPAIndexedRecordLocator<K, T>(typeToken);
        }
        return indexedRecordLocator;
    }
}
