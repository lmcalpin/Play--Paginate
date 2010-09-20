package play.modules.paginate;

import java.io.Serializable;
import java.util.List;

import play.db.jpa.Model;

public class ModelPaginator<T extends Model> extends Paginator<Long, T> implements
		Serializable {
	private static final long serialVersionUID = -2064492602195638937L;

	public ModelPaginator(Class<T> typeToken, List<Long> keys, int pageSize) {
		super(typeToken, keys, pageSize);
	}

	public ModelPaginator(Class<T> typeToken, List<Long> keys) {
		super(typeToken, keys);
	}

	public ModelPaginator(List<T> values) {
		super(values);
	}

	protected ModelPaginator() {}
	
	@Override
	protected KeyedRecordLocator<Long, T> getRecordLocator() {
		if (typeToken == null)
			throw new IllegalStateException(
					"Record locators are only used when the list paginates over keys; a type token is required");
		if (locator == null) {
			locator = new JPAKeyedRecordLocator<Long, T>(typeToken);
		}
		return locator;
	}
}
