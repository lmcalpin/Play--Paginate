package play.modules.paginate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import play.test.BaseTest;

public class MappedPaginatorTest extends BaseTest {
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
        MappedPaginator<String, MockModel> paginator = new MappedPaginator<String, MockModel>(models);
        paginator.setPageSize(4);
        List<MockModel> page1 = (List<MockModel>) paginator.getCurrentPage();
        Assert.assertEquals(4, page1.size());
        for (MockModel model : models.values()) {
            Assert.assertTrue(keys.contains(model.testKey));
        }
    }
}
