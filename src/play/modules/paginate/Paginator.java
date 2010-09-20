package play.modules.paginate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.db.jpa.Model;

/**
 * Controls pagination for a list. It can be used in one of two ways:
 * 
 * 1. PaginatingList(Class<T> typeToken, List<K> keys): receives a list of keys,
 * and lazily loads each page.
 * 
 * 2. PaginatingList(List<T> values): receives all values in advance, and
 * exposes helper methods to display one page at a time.
 * 
 * The former approach is useful if you are displaying thousands or tens of
 * thousands of records. It is much faster to load just the keys
 * ("SELECT id FROM Model") than it is to load all of the data, which is
 * required if you use the second constructor.
 * 
 * @author Lawrence McAlpin
 * 
 * @param <K>
 * @param <T>
 */
public abstract class Paginator<K, T> implements List<T>, Serializable {
	private static final long serialVersionUID = -2064492602195638937L;

	protected Class<T> typeToken;

	private List<K> index;

	private int pageSize;

	private int rowCount;

	private Map<Long, List<T>> pages;

	private List<T> values;

	private int pageNumber;

	protected transient KeyedRecordLocator<K, T> locator;

	private Map<String, Object> viewParams;

	private static final int DEFAULT_PAGE_SIZE = 4;

	protected Paginator() {
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.pages = new HashMap<Long, List<T>>();
		this.pageNumber = 0;
		
		// capture view parameters from Play!
		this.viewParams = LocalVariablesNamesTracer.getLocalVariables();
	}

	public Paginator(List<T> values) {
		this(values, DEFAULT_PAGE_SIZE);
	}

	@SuppressWarnings("unchecked")
	public Paginator(List<T> values, int pageSize) {
		this();
		this.values = values;
		this.pageSize = pageSize;
		this.rowCount = values.size();
	}

	public Paginator(Class<T> typeToken, List<K> keys) {
		this(typeToken, keys, DEFAULT_PAGE_SIZE);
	}

	public Paginator(Class<T> typeToken, List<K> keys, int pageSize) {
		this();
		if (keys == null)
			throw new NullPointerException("Keys must not be null");
		this.typeToken = typeToken;
		this.index = keys;
		this.pageSize = pageSize;
		this.rowCount = index.size();
	}

	protected abstract KeyedRecordLocator<K, T> getRecordLocator();

	public boolean add(T o) {
		throw new UnsupportedOperationException(
				"PaginatingLists are immutable.");
	}

	public void add(int index, T element) {
		throw new UnsupportedOperationException(
				"PaginatingLists are immutable.");
	}

	public void clear() {
		throw new UnsupportedOperationException(
				"PaginatingLists are immutable.");
	}

	public boolean contains(Object o) {
		Model po = (Model) o;
		return index.contains(po.getId());
	}

	public boolean containsAll(Collection<?> c) {
		Collection<Long> justTheKeys = new ArrayList<Long>();
		Iterator<?> it = c.iterator();
		while (it.hasNext()) {
			Model po = (Model) it.next();
			justTheKeys.add(po.getId());
		}
		return index.containsAll(c);
	}

	public T get(int rowIndex) {
		List<T> page = getPage(rowIndex);
		int idx = rowIndex % pageSize;
		if (idx >= page.size())
			return null;
		return page.get(idx);
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getRowCount() {
		return size();
	}

	public int getPageNumber() {
		return pageNumber + 1;
	}

	public int getPageCount() {
		int numberOfRows = size();
		int numberOfFullPages = numberOfRows / pageSize;
		int numberOfPages = numberOfFullPages;
		if (numberOfRows % pageSize > 0)
			numberOfPages++;
		return numberOfPages;
	}

	public List<T> getCurrentPage() {
		return getPage(pageNumber);
	}

	public boolean getHasPreviousPage() {
		return pageNumber != 0;
	}

	public boolean getHasNextPage() {
		return pageNumber < (getPageCount() - 1);
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber - 1;
	}

	/**
	 * @return the index of the first row displayed onscreen (0-based)
	 */
	public int getFirstRowIndex() {
		int startRow = pageNumber * pageSize;
		return startRow;
	}

	private List<T> getPage(int rowIndex) {
		if (pageSize == 0) {
			return new ArrayList<T>();
		}
		int pageNumber = rowIndex / pageSize;
		int startRow = pageNumber * pageSize;
		Long key = Long.valueOf(startRow);
		List<T> page = pages.get(key);
		if (page == null) {
			page = fetchPage(startRow);
			pages.put(key, page);
		}
		return page;
	}

	public void previous() {
		if (getHasPreviousPage()) {
			setPageNumber(getPageNumber() - 1);
		}
	}

	public void next() {
		if (getHasNextPage()) {
			setPageNumber(getPageNumber() + 1);
		}
	}

	protected int getPagesLoaded() {
		return pages.keySet().size();
	}

	public int indexOf(Object o) {
		Model po = (Model) o;
		return index.indexOf(po.getId());
	}

	public boolean isEmpty() {
		return index.isEmpty();
	}

	public Iterator<T> iterator() {
		return new ListItr<K, T>(this);
	}

	public int lastIndexOf(Object o) {
		if (o instanceof Model) {
			Model po = (Model) o;
			return index.lastIndexOf(po.getId());
		}
		return -1;
	}

	public ListIterator<T> listIterator() {
		return new ListItr<K, T>(this);
	}

	public ListIterator<T> listIterator(int index) {
		return new ListItr<K, T>(this, index);
	}

	public T remove(int index) {
		throw new UnsupportedOperationException(
				"PaginatingLists are immutable.");
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException(
				"PaginatingLists are immutable.");
	}

	public T set(int index, T element) {
		throw new UnsupportedOperationException(
				"PaginatingLists are immutable.");
	}

	public int size() {
		if (index != null)
			return index.size();
		else
			return values.size();
	}

	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException(
				"PaginatingLists can not be transformed.");
	}

	public T[] toArray() {
		throw new UnsupportedOperationException(
				"PaginatingLists can not be transformed into arrays.");
	}

	private int getLastRow(int startRow, int pageSize) {
		int lastRow = startRow + pageSize;
		if (lastRow > rowCount) {
			lastRow = rowCount;
		}
		return lastRow;
	}

	private List<T> fetchPage(int startRow) {
		int lastRow = getLastRow(startRow, pageSize);
		if (values == null) {
			List<K> keys = index.subList(startRow, lastRow);
			return fetchPage(keys);
		} else {
			List<T> pageValues = values.subList(startRow, lastRow);
			return pageValues;
		}
	}

	private List<T> fetchPage(List<K> keys) {
		return locator.findByKey(keys);
	}

	private static class ListItr<K, T> implements Iterator<T>, ListIterator<T> {
		Paginator<K, T> theList;

		int currentIndex;

		int maxRecords;

		public ListItr(Paginator<K, T> list) {
			this(list, 0);
		}

		public ListItr(Paginator<K, T> list, int startIndex) {
			this.theList = list;
			this.currentIndex = startIndex;
			this.maxRecords = theList.size();
		}

		public boolean hasNext() {
			if (currentIndex < maxRecords)
				return true;
			return false;
		}

		public T next() {
			return theList.get(currentIndex++);
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"PaginatingLists are immutable.");
		}

		public void add(T arg0) {
			throw new UnsupportedOperationException(
					"PaginatingLists are immutable.");
		}

		public boolean hasPrevious() {
			if (currentIndex > 0)
				return true;
			return false;
		}

		public int nextIndex() {
			return currentIndex;
		}

		public T previous() {
			return theList.get(--currentIndex);
		}

		public int previousIndex() {
			return currentIndex - 1;
		}

		public void set(T arg0) {
			throw new UnsupportedOperationException(
					"PaginatingLists are immutable.");
		}
	}

	public boolean addAll(Collection<? extends T> arg0) {
		return false;
	}

	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		return false;
	}

	public boolean removeAll(Collection<?> arg0) {
		throw new UnsupportedOperationException(
				"PaginatingLists are immutable.");
	}

	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException(
				"PaginatingLists are immutable.");
	}

	@SuppressWarnings("hiding")
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException(
				"PaginatingLists can not be transformed into arrays.");
	}
}
