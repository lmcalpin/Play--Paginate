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

public class IndexedPaginator<K,V> extends Paginator<K, V> {
	private static final long serialVersionUID = 8888238998297494494L;
	
	private IndexedRecordLocator<K, V> locator;
	
	public IndexedPaginator(IndexedRecordLocator<K, V> locator, Class<V> typeToken, List<K> keys) {
		super(typeToken, keys);
		this.locator = locator;
	}

	protected IndexedPaginator() {}
	
	// unused
	@Override
	protected KeyedRecordLocator<K, V> getKeyedRecordLocator() {
		return null;
	}

	@Override
	protected IndexedRecordLocator<K, V> getIndexedRecordLocator() {
		return locator;
	}
}
