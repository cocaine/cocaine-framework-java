package cocaine.combinatorics.worker;

import java.util.Map;

import cocaine.EventHandler;
import cocaine.Runner;
import com.google.common.collect.Maps;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Logger root = Logger.getRootLogger();
        root.addAppender(new FileAppender(
                new PatternLayout("%d %-5p %t%X{ndc}/%c: %m%n"),
                "/var/log/cocaine/cocaine-combinatorics.log",
                true
        ));

        MessagePack messagePack = new MessagePack();
        Map<String, EventHandler> handlers = Maps.newHashMap();
        handlers.put("permutations", new Permutations(messagePack));
        handlers.put("sequences", new Sequences(messagePack));
        handlers.put("inversions", new Inversions(messagePack));

        Runner.run(handlers, args);
    }

}
