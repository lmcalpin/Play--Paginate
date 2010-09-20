package play.modules.paginate;

import java.util.List;

public interface KeyedRecordLocator<K,T>
{
	public abstract List<T> findByKey(List<K> input);
}
