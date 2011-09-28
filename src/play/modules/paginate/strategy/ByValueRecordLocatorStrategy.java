package play.modules.paginate.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ByValueRecordLocatorStrategy<T> implements RecordLocatorStrategy<T> {
    private final List<T> values;
    
    public ByValueRecordLocatorStrategy() {
        values = new ArrayList<T>();
    }
    
    public ByValueRecordLocatorStrategy(List<T> values) {
        this.values = values;
    }

    public ByValueRecordLocatorStrategy(Collection<T> values) {
        this.values = new ArrayList(values);
    }

    @Override
    public List<T> fetchPage(int startRowIdx, int lastRowIdx) {
        if (values == null || startRowIdx > lastRowIdx)
            return Collections.emptyList();
        List<T> pageValues = values.subList(startRowIdx, lastRowIdx);
        return pageValues;
    }
    
    @Override
    public int count() {
        return values.size();
    }

    @Override
    public int indexOf(T t) {
        return values.indexOf(t);
    }

    @Override
    public int lastIndexOf(T t) {
        return values.lastIndexOf(t);
    }

}
