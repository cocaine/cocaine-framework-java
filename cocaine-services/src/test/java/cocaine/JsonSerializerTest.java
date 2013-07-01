package cocaine;

import java.util.Collections;
import java.util.Map;

import cocaine.annotations.CocaineParameter;
import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class JsonSerializerTest {

    @Test
    public void serializeEmpty() throws Exception {
        Gson gson = new Gson();
        JsonSerializer serializer = new JsonSerializer(gson);

        Invokable<?, Object> method = Invokable.from(Methods.class.getMethod("empty"));
        Parameter[] parameters = Iterables.toArray(method.getParameters(), Parameter.class);

        byte[] bytes = serializer.serialize(parameters, new Object[] { });

        Assert.assertArrayEquals("[]".getBytes(Charsets.UTF_8), bytes);
    }

    @Test
    public void serializeOne() throws Exception {
        Gson gson = new Gson();
        JsonSerializer serializer = new JsonSerializer(gson);

        Invokable<?, Object> method = Invokable.from(Methods.class.getMethod("one", Pojo.class));
        Parameter[] parameters = Iterables.toArray(method.getParameters(), Parameter.class);

        Pojo pojo = new Pojo("Hello world!", 42);
        byte[] bytes = serializer.serialize(parameters, new Object[] { pojo });

        Assert.assertArrayEquals(gson.toJson(pojo).getBytes(Charsets.UTF_8), bytes);
    }

    @Test
    public void serializeOneAnnotated() throws Exception {
        Gson gson = new Gson();
        JsonSerializer serializer = new JsonSerializer(gson);

        Invokable<?, Object> method = Invokable.from(Methods.class.getMethod("oneAnnotated", Pojo.class));
        Parameter[] parameters = Iterables.toArray(method.getParameters(), Parameter.class);

        Pojo pojo = new Pojo("Hello world!", 42);
        byte[] bytes = serializer.serialize(parameters, new Object[] { pojo });

        String expected = gson.toJson(Collections.singletonMap("value", pojo));
        JSONAssert.assertEquals(expected, new String(bytes, Charsets.UTF_8), true);
    }

    @Test(expected = IllegalStateException.class)
    public void serializeMultiMissingAnnotation() throws Exception {
        Gson gson = new Gson();
        JsonSerializer serializer = new JsonSerializer(gson);

        Invokable<?, Object> method = Invokable.from(
                Methods.class.getMethod("multiMissingAnnotation", String.class, Pojo.class));
        Parameter[] parameters = Iterables.toArray(method.getParameters(), Parameter.class);

        Pojo pojo = new Pojo("Hello world!", 42);
        serializer.serialize(parameters, new Object[] { "bad", pojo });
    }

    @Test(expected = IllegalStateException.class)
    public void serializeMultiNameDuplication() throws Exception {
        Gson gson = new Gson();
        JsonSerializer serializer = new JsonSerializer(gson);

        Invokable<?, Object> method = Invokable.from(
                Methods.class.getMethod("multiNameDuplication", String.class, Pojo.class));
        Parameter[] parameters = Iterables.toArray(method.getParameters(), Parameter.class);

        Pojo pojo = new Pojo("Hello world!", 42);
        serializer.serialize(parameters, new Object[] { "bad", pojo });
    }

    @Test
    public void serializeMulti() throws Exception {
        Gson gson = new Gson();
        JsonSerializer serializer = new JsonSerializer(gson);

        Invokable<?, Object> method = Invokable.from(Methods.class.getMethod("multi", String.class, Pojo.class));
        Parameter[] parameters = Iterables.toArray(method.getParameters(), Parameter.class);

        Pojo pojo = new Pojo("Hello world!", 42);
        String string = "bad";
        byte[] bytes = serializer.serialize(parameters, new Object[] { string, pojo });

        Map<String, Object> map = Maps.newHashMap();
        map.put("string", string);
        map.put("pojo", pojo);
        String expected = gson.toJson(map);
        JSONAssert.assertEquals(expected, new String(bytes, Charsets.UTF_8), true);
    }

    @SuppressWarnings("unused")
    private static class Methods {

        public void empty() { }

        public void one(Pojo pojo) { }

        public void oneAnnotated(@CocaineParameter("value") Pojo pojo) { }

        public void multiMissingAnnotation(@CocaineParameter("value") String string, Pojo pojo) { }

        public void multiNameDuplication(
                @CocaineParameter("value") String string,
                @CocaineParameter("value") Pojo pojo)
        { }

        public void multi(@CocaineParameter("string") String string, @CocaineParameter("pojo") Pojo pojo) { }


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
