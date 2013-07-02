package cocaine.services;

import java.util.Map;

import com.google.common.base.Joiner;
import org.msgpack.type.Value;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class NodeInfo {

    private final Map<String, Value> runlist;
    private final String identity;
    private final int uptime;

    public NodeInfo(Map<String, Value> runlist, String identity, int uptime) {
        this.runlist = runlist;
        this.identity = identity;
        this.uptime = uptime;
    }

    public Map<String, Value> getRunlist() {
        return runlist;
    }

    public String getIdentity() {
        return identity;
    }

    public int getUptime() {
        return uptime;
    }

    @Override
    public String toString() {
        return "Identity: " + identity + "; Uptime: " + uptime + "; Apps: " + Joiner.on(", ").join(runlist.keySet());
    }

}