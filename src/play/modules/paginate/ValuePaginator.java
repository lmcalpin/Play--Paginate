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

public class ValuePaginator<V> extends Paginator<Object, V> implements
		Serializable {
	private static final long serialVersionUID = -2064492602195638937L;

	public ValuePaginator(List<V> values) {
		super(values);
	}

	public ValuePaginator(List<V> values, int pageSize) {
		super(values, pageSize);
	}

	protected ValuePaginator() {}
	
	// unused
	@Override
	protected KeyedRecordLocator<Object, V> getRecordLocator() {
		return null;
	}
}
