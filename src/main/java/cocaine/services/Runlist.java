package cocaine.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Runlist {

    private final Map<String, String> runlist;

    public static Runlist empty() {
        return new Runlist(new HashMap<String, String>());
    }

    public Runlist(Map<String, String> runlist) {
        this.runlist = runlist;
    }

    public void addApp(String name, String profile) {
        runlist.put(name, profile);
    }

    public String getProfile(String name) {
        return runlist.get(name);
    }

    public Set<String> getRunningApps() {
        return runlist.keySet();
    }

    public boolean isEmpty() {
        return runlist.isEmpty();
    }

    public Map<String, String> getRunlist() {
        return runlist;
    }
}
