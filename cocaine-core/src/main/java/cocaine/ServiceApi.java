package cocaine;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceApi {

    private final String name;
    private final ImmutableMap<String, Integer> api;

    public ServiceApi(String name, ImmutableMap<String, Integer> api) {
        this.name = name;
        this.api = api;
    }

    public static ServiceApi of(String name, String method, int id) {
        return new ServiceApi(name, ImmutableMap.of(method, id));
    }

    public static ServiceApi of(String name, ImmutableMap<String, Integer> methods) {
        return new ServiceApi(name, methods);
    }

    public int getMethod(String method) {
        Integer number = api.get(method);
        if (number == null) {
            throw new UnknownMethodException(name, method);
        }
        return number;
    }

    @Override
    public String toString() {
        return "Service: " + name + "; Service API: { "
                + Joiner.on(", ").withKeyValueSeparator(": ").join(api) + " }";
    }
}
