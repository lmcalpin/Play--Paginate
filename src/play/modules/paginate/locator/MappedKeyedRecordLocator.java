package play.modules.paginate.locator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import play.modules.paginate.KeyedRecordLocator;

public class MappedKeyedRecordLocator<K,V> implements KeyedRecordLocator<K,V> {
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
