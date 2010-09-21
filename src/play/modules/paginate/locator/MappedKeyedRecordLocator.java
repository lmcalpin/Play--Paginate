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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.modules.paginate.KeyedRecordLocator;

public class MappedKeyedRecordLocator<K,V> implements KeyedRecordLocator<K,V>, Serializable {
	private static final long serialVersionUID = 4224849298526331518L;
	
	private final Map<K,V> store;
	
	public MappedKeyedRecordLocator(Map<K,V> store) {
		this.store = store;
	}
	
	@Override
	public List<V> findByKey(List<K> input) {
		List<V> output = new ArrayList<V>();
		for (K key : input) {
			output.add(store.get(key));
		}
		return output;
	}

}
