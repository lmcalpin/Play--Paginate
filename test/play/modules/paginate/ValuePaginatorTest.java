package play.modules.paginate;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import play.test.BaseTest;

public class ValuePaginatorTest extends BaseTest {
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
