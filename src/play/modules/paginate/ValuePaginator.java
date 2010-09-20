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
