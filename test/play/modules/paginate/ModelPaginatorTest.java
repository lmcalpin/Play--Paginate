package play.modules.paginate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.modules.paginate.locator.JPAIndexedRecordLocator;
import play.test.UnitTest;

public class ModelPaginatorTest extends UnitTest {
    @Before
    public void before() {
        if (MockModel.count() == 0) {
            for (int i = 0; i < 15; i++) {
                MockModel model = new MockModel();
                model.testValue = String.valueOf(i + 1);
                model.save();
            }
        }
    }
    
    @Test
    public void testCount() {
        ModelPaginator paginator = new ModelPaginator(MockModel.class);
        Assert.assertEquals(15, paginator.size());
    }

    @Test
    public void testOrderByDescendingWithJPAIndexedRecordLocator() {
        ModelPaginator<MockModel> paginator = new ModelPaginator(
                new JPAIndexedRecordLocator(MockModel.class).setOrderBy("ID DESC"));
        Assert.assertEquals("15", paginator.get(0).testValue);
        Assert.assertEquals("1", paginator.get(14).testValue);
    }
    
    @Test
    public void testOrderByDescending() {
        ModelPaginator<MockModel> paginator2 = new ModelPaginator(MockModel.class);
        paginator2.orderBy("ID DESC");
        Assert.assertEquals("15", paginator2.get(0).testValue);
        Assert.assertEquals("1", paginator2.get(14).testValue);
    }

    @Test
    public void testOrderByAscending() {
        ModelPaginator<MockModel> paginator = new ModelPaginator(
                new JPAIndexedRecordLocator(MockModel.class).setOrderBy("ID ASC"));
        Assert.assertEquals("1", paginator.get(0).testValue);
        Assert.assertEquals("15", paginator.get(14).testValue);
    }

}
