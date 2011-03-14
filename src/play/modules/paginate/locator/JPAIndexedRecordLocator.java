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
public class JPAIndexedRecordLocator<K,Model> implements IndexedRecordLocator<K,Model>, Serializable {
	private static final long serialVersionUID = 1847759900112779643L;

	private final Class<Model> typeToken;
	private final String filter;
	private final String orderBy;
	private final Object[] params;

	public JPAIndexedRecordLocator(Class<Model> typeToken) {
		this(typeToken, null, null);
	}

	public JPAIndexedRecordLocator(Class<Model> typeToken, String orderBy, String filter, Object... params) {
		this.typeToken = typeToken;
		this.filter = filter;
		this.params = params;
		this.orderBy = orderBy;
	}

	@Override
	public int count() {
		return ((Long)this.query("COUNT(*)").getSingleResult()).intValue();
	}

	@Override
	public List<Model> findByIndex(int firstResult, int pageSize) {
		@SuppressWarnings("unchecked")
		List<Model> returnMe = this.query(null).setFirstResult(firstResult).setMaxResults(pageSize).getResultList();
		return returnMe;
	}

	private static final String SELECT = "SELECT ";
	private Query query(String select) {
		StringBuilder hql = new StringBuilder();
		if (select != null) {
			if (!select.regionMatches(true, 0, SELECT, 0, SELECT.length())) {
				hql.append("SELECT ");
			}
			hql.append(select);
			hql.append(' ');
		}
		hql.append("FROM " + this.typeToken.getSimpleName());
		if (this.filter != null) {
			hql.append(" WHERE " + this.filter);
		}
		if (this.orderBy != null) {
			hql.append(" ORDER BY " + this.orderBy);
		}

		Query query = JPA.em().createQuery(hql.toString());
		if (this.params != null) {
			for (int i = 0; i < this.params.length; i++) {
				query.setParameter(i+1, this.params[i]);
			}
		}
		return query;
	}
}
