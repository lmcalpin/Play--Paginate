package play.modules.paginate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.PersistenceException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class ModelPaginatorTest extends UnitTest {
    @Before
    public void before() {
        if (MockModel.count() == 0) {
            for (int i = 0; i < 15; i++) {
                MockModel model = new MockModel();
                model.testValue = String.valueOf(i + 1);
                model.save();
                AltMockModel model2 = new AltMockModel();
                model2.testValue = String.valueOf(i + 1);
                model2.save();
            }
        }
    }
    
    @Test
    public void testCount() {
        ModelPaginator paginator = new ModelPaginator(MockModel.class);
        Assert.assertEquals(15, paginator.size());
    }

    @Test
    public void testOrderByDescending() {
        ModelPaginator<MockModel> paginator = new ModelPaginator(MockModel.class).orderBy("ID DESC");
        Assert.assertEquals("15", paginator.get(0).testValue);
        Assert.assertEquals("1", paginator.get(14).testValue);
    }

    @Test
    public void testOrderByAscending() {
        ModelPaginator<MockModel> paginator = new ModelPaginator(MockModel.class).orderBy("ID ASC");
        Assert.assertEquals("1", paginator.get(0).testValue);
        Assert.assertEquals("15", paginator.get(14).testValue);
    }

    @Test
    public void testPaginateByKey() {
        List<Long> keys = new ArrayList<Long>();
        keys.add(10L);
        keys.add(12L);
        ModelPaginator<MockModel> paginator = new ModelPaginator(MockModel.class, keys);
        Assert.assertEquals(2, paginator.size());
        Assert.assertEquals("10", paginator.get(0).testValue);
        Assert.assertEquals("12", paginator.get(1).testValue);
    }

    @Test
    public void testPaginateByKeyAndOrder() {
        List<Long> keys = new ArrayList<Long>();
        keys.add(10L);
        keys.add(12L);
        ModelPaginator<MockModel> paginator = new ModelPaginator(MockModel.class, keys).orderBy("ID DESC");
        Assert.assertEquals(2, paginator.size());
        Assert.assertEquals("12", paginator.get(0).testValue);
        Assert.assertEquals("10", paginator.get(1).testValue);
    }

    @Test
    public void testWhereClause() {
        List<Long> keys = new ArrayList<Long>();
        keys.add(10L);
        keys.add(12L);
        ModelPaginator<MockModel> paginator = new ModelPaginator(MockModel.class, "testValue <> '13'");
        Assert.assertEquals(14, paginator.size());
        for (int i = 0; i < 14; i++) {
            Assert.assertFalse("13".equals(paginator.get(0).testValue));
        }
    }

    // let's make sure this still works when @Entity(name="...") has an alternate name declared
    @Test
    public void testModelsWithAlternateEntityNames() {
        ModelPaginator paginator = new ModelPaginator(AltMockModel.class);
        Assert.assertEquals(15, paginator.size());
    }

    @Test(expected=PersistenceException.class)
    public void testErrorsOutIfInvalidKeyNameIsProvided() {
        // since the key is actually named "id" this should fail
        JPAPaginator paginator = new ModelPaginator(MockModel.class, Arrays.asList(10L)).withKeyNamed("foo");
        paginator.size();
        fail();
    }

}
