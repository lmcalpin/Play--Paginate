package play.modules.paginate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import play.db.jpa.JPA;

public class JPAKeyedRecordLocator<K,Model> implements KeyedRecordLocator<K,Model> {
	private Class<Model> typeToken;
	
	public JPAKeyedRecordLocator(Class<Model> typeToken) {
		this.typeToken = typeToken;
	}
	
	@Override
	public List<Model> findByKey(List<K> input) {
		Session session = (Session)JPA.em().getDelegate();
		Criteria criteria = session.createCriteria(typeToken);
		@SuppressWarnings("unchecked")
		List<Model> returnMe = criteria.add(Restrictions.in("id", input)).addOrder(Order.asc("id")).list();
		return returnMe;
	}

}
