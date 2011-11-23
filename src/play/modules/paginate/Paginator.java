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
import play.Play;
import play.db.jpa.Model;
import play.modules.paginate.strategy.ByKeyRecordLocatorStrategy;
import play.modules.paginate.strategy.ByValueRecordLocatorStrategy;
import play.modules.paginate.strategy.RecordLocatorStrategy;
import play.mvc.Http.Request;
import play.mvc.Router;
import play.mvc.Scope;

/**
 * Base class to control pagination for a list.
 * Typically, a user would use the {@link JPAPaginator}, {@link ModelPaginator}, 
 * or {@link ValuePaginator} implementations.
 * 
 * @author Lawrence McAlpin
 * 
 * @param <K>
 * @param <T>
 */
public abstract class Paginator<K, T> implements List<T>, Serializable {
    private static final String DEFAULT_PAGE_PARAM = "page";

    private static final long serialVersionUID = -2064492602195638937L;

    private int pageSize;
    private Map<Long, List<T>> pages;
    private RecordLocatorStrategy<T> recordLocatorStrategy;

    private int pageNumber;
    private Integer rowCount;
    private final String action;
    private String paramName;
    private final Map<String, Object> viewParams;

    // control options
    private boolean boundaryControlsEnabled;
    // replaced with summary tag
    // private boolean rowCountSummaryEnabled;
    private int pagesDisplayed;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private Paginator() {
        this.pageSize = DEFAULT_PAGE_SIZE;
        this.pages = new HashMap<Long, List<T>>();
        this.pageNumber = 0;

        // capture controller and action that created this Paginator
        Request request = Request.current();
        if (request != null) {
            this.action = request.action;
        } else {
            this.action = null;
        }

        // set the current page
        Scope.Params params = Scope.Params.current();
        this.viewParams = new HashMap<String, Object>();
        if (params != null) {
            setParameterName(Play.configuration.getProperty("paginator.parameter.name", DEFAULT_PAGE_PARAM));
            this.viewParams.putAll(params.allSimple());
        } else {
            this.paramName = DEFAULT_PAGE_PARAM;
        }
        this.viewParams.remove("body");

        // default view options
        this.boundaryControlsEnabled = true;
        // this.rowCountSummaryEnabled = false;
        this.pagesDisplayed = 5;
    }

    protected Paginator(RecordLocatorStrategy recordLocatorStrategy) {
        this();
        this.recordLocatorStrategy = recordLocatorStrategy;
    }
    
    protected RecordLocatorStrategy getRecordLocatorStrategy() {
        return recordLocatorStrategy;
    }

    public String getCallbackURL(int page) {
        viewParams.put(paramName, String.valueOf(page));
        return Router.reverse(action, viewParams).url;
    }

    public boolean add(T o) {
        throw new UnsupportedOperationException("PaginatingLists are immutable.");
    }

    public void add(int index, T element) {
        throw new UnsupportedOperationException("PaginatingLists are immutable.");
    }

    public void clear() {
        throw new UnsupportedOperationException("PaginatingLists are immutable.");
    }

    public boolean contains(Object o) {
        if (indexOf(o) >= 0)
            return true;
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        Iterator<?> it = c.iterator();
        while (it.hasNext()) {
            int idx = indexOf(it.next());
            if (idx < 0)
                return false;
        }
        return true;
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

    public Paginator<K, T> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public int getRowCount() {
        return size();
    }

    public String getParameterName() {
        return paramName;
    }

    public void setParameterName(String paramName) {
        this.paramName = paramName;
        parsePageParameter();
    }

    /**
     * @return the current page number (1-based)
     */
    public int getPageNumber() {
        return pageNumber + 1;
    }

    /**
     * @param pageNumber
     *            the 1-based index of the page we are currently viewing
     */
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber - 1;
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

    public boolean isBoundaryControlsEnabled() {
        return boundaryControlsEnabled;
    }

    public Paginator<K, T> setBoundaryControlsEnabled(boolean showBoundaryControls) {
        this.boundaryControlsEnabled = showBoundaryControls;
        return this;
    }

    public int getPagesDisplayed() {
        return pagesDisplayed;
    }

    public Paginator<K, T> setPagesDisplayed(int pagesDisplayed) {
        this.pagesDisplayed = pagesDisplayed;
        return this;
    }

    protected int getPagesLoaded() {
        return pages.keySet().size();
    }

    public int indexOf(Object o) {
        return recordLocatorStrategy.indexOf((T)o);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Iterator<T> iterator() {
        return new ListItr<K, T>(this);
    }

    public int lastIndexOf(Object o) {
        return recordLocatorStrategy.lastIndexOf((T) o);
    }

    public ListIterator<T> listIterator() {
        return new ListItr<K, T>(this);
    }

    public ListIterator<T> listIterator(int index) {
        return new ListItr<K, T>(this, index);
    }

    public T remove(int index) {
        throw new UnsupportedOperationException("PaginatingLists are immutable.");
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException("PaginatingLists are immutable.");
    }

    public T set(int index, T element) {
        throw new UnsupportedOperationException("PaginatingLists are immutable.");
    }

    public int size() {
        if (this.rowCount == null) {
            this.rowCount = recordLocatorStrategy.count();
        }
        return this.rowCount;
    }

    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("PaginatingLists can not be transformed.");
    }

    public T[] toArray() {
        throw new UnsupportedOperationException("PaginatingLists can not be transformed into arrays.");
    }

    private static int getLastRowForPage(int startRow, int pageSize, int rowCount) {
        int lastRow = startRow + pageSize;
        if (lastRow > rowCount) {
            lastRow = rowCount;
        }
        return lastRow;
    }

    private List<T> fetchPage(int startRowIdx) {
        int lastRowIdx = getLastRowForPage(startRowIdx, pageSize, getRowCount());
        return recordLocatorStrategy.fetchPage(startRowIdx, lastRowIdx);
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
            throw new UnsupportedOperationException("PaginatingLists are immutable.");
        }

        public void add(T arg0) {
            throw new UnsupportedOperationException("PaginatingLists are immutable.");
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
            throw new UnsupportedOperationException("PaginatingLists are immutable.");
        }
    }

    public boolean addAll(Collection<? extends T> arg0) {
        return false;
    }

    public boolean addAll(int arg0, Collection<? extends T> arg1) {
        return false;
    }

    public boolean removeAll(Collection<?> arg0) {
        throw new UnsupportedOperationException("PaginatingLists are immutable.");
    }

    public boolean retainAll(Collection<?> arg0) {
        throw new UnsupportedOperationException("PaginatingLists are immutable.");
    }

    @SuppressWarnings("hiding")
    public <T> T[] toArray(T[] arg0) {
        throw new UnsupportedOperationException("PaginatingLists can not be transformed into arrays.");
    }

    private void parsePageParameter() {
        Scope.Params params = Scope.Params.current();
        String page = (String) params.get(paramName);
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
    }
}
