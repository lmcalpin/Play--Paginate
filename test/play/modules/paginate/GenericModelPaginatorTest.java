package play.modules.paginate;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import play.test.UnitTest;

public class GenericModelPaginatorTest extends UnitTest {
    @Before
    public void before() {
        if (MockGenericModel.count() == 0) {
            for (int i = 0; i < 15; i++) {
                MockGenericModel model = new MockGenericModel();
                model.testKey = "test" + (i + 1);
                model.testValue = String.valueOf(i + 1);
                model.save();
            }
        }
    }
    
    @Test
    public void testCount() {
        ModelPaginator paginator = new ModelPaginator(MockGenericModel.class);
        Assert.assertEquals(15, paginator.size());
    }

    @Test
    public void testFindByKey() {
        JPAPaginator<String,MockGenericModel> paginator = new JPAPaginator(MockGenericModel.class, Arrays.asList("test10")).withKeyNamed("testKey");
        Assert.assertEquals(1, paginator.size());
        Assert.assertEquals("test10", paginator.get(0).testKey);
        Assert.assertEquals("10", paginator.get(0).testValue);
    }
}
