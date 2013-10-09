package cocaine;

import java.net.SocketAddress;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceInfo {

    private final String name;
    private final SocketAddress endpoint;
    private final ImmutableMap<String, Integer> api;

    public ServiceInfo(String name, SocketAddress endpoint, ImmutableMap<String, Integer> api) {
        this.name = name;
        this.endpoint = endpoint;
        this.api = api;
    }

    public int getMethod(String method) {
        Integer number = api.get(method);
        if (number == null) {
            throw new UnknownMethodException(name, method);
        }
        return number;
    }

    public SocketAddress getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        return "Service: " + name + "; Endpoint: " + endpoint + "; Service API: { "
                + Joiner.on(", ").withKeyValueSeparator(": ").join(api) + " }";
    }
}
