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
package play.modules.paginate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import play.Logger;
import play.db.jpa.Model;
import play.mvc.Http.Request;
import play.mvc.Router;
import play.mvc.Scope;

/**
 * Controls pagination for a list. It can be used in one of three ways:
 * 
 * 1. Paginator(Class<T> typeToken, List<K> keys): receives a list of keys,
 * and lazily loads each page.
 * 
 * 2. Paginator(List<T> values): receives all values in advance, and
 * exposes helper methods to display one page at a time.
 * 
 * 3. Paginator(Class<T> typeToken, int recordCount): does a callback to 
 * load each page using an implementation of IndexedRecordLocator provided
 * by a subclass of Paginator.
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

	private enum PaginationStyle { BY_VALUE, BY_KEY, BY_CALLBACK };
	
	protected Class<T> typeToken;

	private List<K> index;

	private int pageSize;

	private int rowCount;

	private Map<Long, List<T>> pages;

	private List<T> values;

	private int pageNumber;
	
	private PaginationStyle paginationStyle;

	private String action;
	private Map<String, Object> viewParams;

	public static final int DEFAULT_PAGE_SIZE = 20;

	protected Paginator() {
		this.pageSize = DEFAULT_PAGE_SIZE;
		this.pages = new HashMap<Long, List<T>>();
		this.pageNumber = 0;
		this.paginationStyle = PaginationStyle.BY_VALUE;
		
		// capture view parameters from Play!
		//this.viewParams = LocalVariablesNamesTracer.getLocalVariables();
		
		// capture controller and action that created this Paginator
		Request request = Request.current();
		if (request != null) {
			this.action = request.action;
		}

		// set the current page
		Scope.Params params = Scope.Params.current();
		if (params != null) {
			String page = (String) params.get("page");
			if (page == null) {
				setPageNumber(1);
			} else {
				try {
					int pageNumber = Integer.parseInt(page);
					setPageNumber(pageNumber);
				} catch (Throwable t) {
					Logger.warn(t, "Error parsing page: %s", page);
				}
			}
			this.viewParams = new HashMap<String,Object>();
			this.viewParams.putAll(params.allSimple());
		}
	}

	public Paginator(List<T> values) {
		this(values, DEFAULT_PAGE_SIZE);
		this.paginationStyle = PaginationStyle.BY_VALUE;
	}

	@SuppressWarnings("unchecked")
	public Paginator(List<T> values, int pageSize) {
		this();
		this.values = values;
		this.pageSize = pageSize;
		this.rowCount = values.size();
		this.paginationStyle = PaginationStyle.BY_VALUE;
	}

	public Paginator(Class<T> typeToken, List<K> keys) {
		this(typeToken, keys, DEFAULT_PAGE_SIZE);
		this.paginationStyle = PaginationStyle.BY_KEY;
	}

	public Paginator(Class<T> typeToken, List<K> keys, int pageSize) {
		this();
		if (keys == null)
			throw new NullPointerException("Keys must not be null");
		this.typeToken = typeToken;
		this.index = keys;
		this.pageSize = pageSize;
		this.rowCount = index.size();
		this.paginationStyle = PaginationStyle.BY_KEY;
	}

	public Paginator(Class<T> typeToken, int rowCount) {
		this(typeToken, rowCount, DEFAULT_PAGE_SIZE);
		this.paginationStyle = PaginationStyle.BY_CALLBACK;
	}
	
	public Paginator(Class<T> typeToken, int rowCount, int pageSize) {
		this();
		this.paginationStyle = PaginationStyle.BY_CALLBACK;
		this.rowCount = rowCount;
		this.typeToken = typeToken;
		this.pageSize = pageSize;
	}

	protected abstract KeyedRecordLocator<K, T> getKeyedRecordLocator();

	protected abstract IndexedRecordLocator<K, T> getIndexedRecordLocator();
	
	public String getCallbackURL(int page) {
		viewParams.put("page", String.valueOf(page));
		return Router.reverse(action, viewParams).url;
	}

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
		return getPage(getFirstRowIndex());
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

	/**
	 * @return the index of the last row displayed onscreen (0-based)
	 */
	public int getLastRowIndex() {
		int startRow = getFirstRowIndex();
		int lastRow = Math.min(getRowCount(), startRow + pageSize);
		return lastRow - 1;
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
		return rowCount;
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
		switch (paginationStyle) {
		case BY_KEY:
			List<K> keys = index.subList(startRow, lastRow);
			return getKeyedRecordLocator().findByKey(keys);
		case BY_CALLBACK:
			return getIndexedRecordLocator().findByIndex(startRow, pageSize);
		default: // BY_VALUE
			if (values == null)
				return Collections.emptyList();
			List<T> pageValues = values.subList(startRow, lastRow);
			return pageValues;
		}
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
