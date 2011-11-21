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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import play.modules.paginate.Paginator;
import play.modules.paginate.strategy.ByValueRecordLocatorStrategy;

/**
 * This class allows you to paginate over a prepopulated Collection.
 * 
 * @author Lawrence McAlpin
 *
 * @param <T>
 */
public class ValuePaginator<T> extends Paginator<Object, T> {
    private static final long serialVersionUID = -2064492602195638937L;

    protected ValuePaginator() {
        this(new ArrayList<T>());
    }
    
    public ValuePaginator(List<T> values) {
        super(new ByValueRecordLocatorStrategy<T>(values));
    }

    public ValuePaginator(Map<?,T> values) {
        super(new ByValueRecordLocatorStrategy<T>(values.values()));
    }

    public ValuePaginator(Collection<T> values) {
        super(new ByValueRecordLocatorStrategy<T>(values));
    }
}
