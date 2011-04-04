package play.modules.paginate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import play.modules.paginate.locator.MappedKeyedRecordLocator;

public class PaginatorTest {
    @Test
    public void testPaginateByValue() {
        List<MockModel> models = new ArrayList<MockModel>();
        for (int i = 0; i < 100; i++) {
            MockModel mm = new MockModel();
            mm.testValue = String.valueOf(i + 1);
            models.add(mm);
        }
        ValuePaginator<MockModel> paginator = new ValuePaginator<MockModel>(models);
        paginator.setPageSize(4);
        List<MockModel> page1 = (List<MockModel>) paginator.getCurrentPage();
        Assert.assertEquals(4, page1.size());
        Assert.assertFalse(paginator.getHasPreviousPage());
        Assert.assertTrue(paginator.getHasNextPage());
        Assert.assertEquals(25, paginator.getPageCount());
    }

    @Test
    public void testPaginateByKey() {
        Map<String, MockModel> models = new HashMap<String, MockModel>();
        List<String> keys = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            MockModel mm = new MockModel();
            mm.testKey = String.valueOf(i + 1);
            mm.testValue = "value " + String.valueOf(i + 1);
            models.put(mm.testKey, mm);
            keys.add(mm.testKey);
        }
        MappedPaginator<String, MockModel> paginator = new MappedPaginator<String, MockModel>(
                new MappedKeyedRecordLocator<String, MockModel>(models), MockModel.class, keys);
        paginator.setPageSize(4);
        List<MockModel> page1 = (List<MockModel>) paginator.getCurrentPage();
        Assert.assertEquals(4, page1.size());
        for (MockModel model : models.values()) {
            Assert.assertTrue(keys.contains(model.testKey));
        }
    }

    @Test
    public void testSinglePage() {
        List<MockModel> models = new ArrayList<MockModel>();
        for (int i = 0; i < 100; i++) {
            MockModel mm = new MockModel();
            mm.testValue = String.valueOf(i + 1);
            models.add(mm);
        }
        ValuePaginator<MockModel> paginator = new ValuePaginator<MockModel>(models);
        paginator.setPageSize(100);
        Assert.assertEquals(1, paginator.getPageCount());
        Assert.assertFalse(paginator.getHasPreviousPage());
        Assert.assertFalse(paginator.getHasNextPage());

    }

    @Test
    public void testHasNextReturnsFalseOnLastPage() {
        List<MockModel> models = new ArrayList<MockModel>();
        for (int i = 0; i < 100; i++) {
            MockModel mm = new MockModel();
            mm.testValue = String.valueOf(i + 1);
            models.add(mm);
        }
        ValuePaginator<MockModel> paginator = new ValuePaginator<MockModel>(models);
        paginator.setPageSize(51);
        Assert.assertEquals(2, paginator.getPageCount());
        Assert.assertFalse(paginator.getHasPreviousPage());
        Assert.assertTrue(paginator.getHasNextPage());
        paginator.setPageNumber(2);
        Assert.assertTrue(paginator.getHasPreviousPage());
        Assert.assertFalse(paginator.getHasNextPage());

    }
}
