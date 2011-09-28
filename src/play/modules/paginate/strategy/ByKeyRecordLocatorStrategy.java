package play.modules.paginate.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.ListSelectionEvent;

public class ByKeyRecordLocatorStrategy<K, T> implements RecordLocatorStrategy<T> {
    private final List<K> index;
    private final List<T> values;
    private final Map<K, T> store;

    public ByKeyRecordLocatorStrategy() {
        this(new HashMap<K,T>());
    }
    
    public ByKeyRecordLocatorStrategy(Map<K, T> store) {
        this.store = store;
        this.index = new ArrayList<K>(store.keySet());
        this.values = new ArrayList<T>(store.values());
    }

    @Override
    public List<T> fetchPage(int startRowIdx, int lastRowIdx) {
        if (index == null || values == null || startRowIdx > lastRowIdx)
            return Collections.emptyList();
        List<K> keys = index.subList(startRowIdx, lastRowIdx);
        return findByKey(keys);
    }

    @Override
    public int count() {
        return index.size();
    }

    @Override
    public int indexOf(T t) {
        return values.indexOf(t);
    }

    @Override
    public int lastIndexOf(T t) {
        return values.lastIndexOf(t);
    }

    private List<T> findByKey(List<K> input) {
        List<T> output = new ArrayList<T>();
        for (K key : input) {
            output.add(store.get(key));
        }
        return output;
    }
}
