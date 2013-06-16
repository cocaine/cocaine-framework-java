package cocaine;

import java.net.SocketAddress;
import java.util.Map;

import com.google.common.base.Joiner;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ServiceInfo {

    private final String name;
    private final SocketAddress endpoint;
    private final Map<Integer, String> api;

    public ServiceInfo(String name, SocketAddress endpoint, Map<Integer, String> api) {
        this.name = name;
        this.endpoint = endpoint;
        this.api = api;
    }

    public SocketAddress getEndpoint() {
        return endpoint;
    }

    public int getMethod(String method) {
        for (Map.Entry<Integer, String> entry : api.entrySet()) {
            if (entry.getValue().equals(method)) {
                return entry.getKey();
            }
        }
        throw new UnknownMethodException(name, method);
    }

    public Map<Integer, String> getApi() {
        return api;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Endpoint: " + endpoint + "; Service API: { "
                + Joiner.on(", ").withKeyValueSeparator(": ").join(api) + " }";
    }
}
