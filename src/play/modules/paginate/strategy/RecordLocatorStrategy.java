package play.modules.paginate.strategy;

import java.util.List;

public interface RecordLocatorStrategy<T> {
    public List<T> fetchPage(int startRowIdx, int lastRowIdx);
    public int count();
    public int indexOf(T t);
    public int lastIndexOf(T t);
}
