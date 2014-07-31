package cocaine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.base.Throwables;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
class Names {

    private final List<String> names;
    private final Random random;

    private Names(List<String> names) {
        this.names = Collections.unmodifiableList(names);
        this.random = new Random();
    }

    public static Names create(String resource) {
        List<String> names = new ArrayList<>(2900);
        try (InputStream stream = Runner.class.getClassLoader().getResourceAsStream(resource)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            for (String name = reader.readLine(); name != null; name = reader.readLine()) {
                names.add(name);
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return new Names(names);
    }

    public String randomName() {
        return names.get(random.nextInt(names.size()));
    }

}
