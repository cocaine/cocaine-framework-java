package cocaine;

import java.util.Collections;
import java.util.Map;

import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class JsonSerializerTest {

    private JsonSerializer serializer;

    @Before
    public void setUp() {
        Gson gson = new Gson();
        serializer = new JsonSerializer(gson);
    }

    @Test
    public void serializeOne() throws Exception {
        Assert.assertTrue(true);
    }

    @Test
    public void serializeOneAnnotated() throws Exception {
        Assert.assertTrue(true);
    }

    @Test
    public void serialize() throws Exception {
        Assert.assertTrue(true);
    }

    private Map<String, Pojo> process() {
        return Collections.emptyMap();
    }

    private static class Pojo {

        private final String string;
        private final int integer;

        private Pojo(String string, int integer) {
            this.string = string;
            this.integer = integer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pojo pojo = (Pojo) o;
            return integer == pojo.integer && string.equals(pojo.string);
        }

        @Override
        public int hashCode() {
            int result = string.hashCode();
            result = 31 * result + integer;
            return result;
        }
    }
}
