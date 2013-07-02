package cocaine.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class NodeInfoTemplate  extends AbstractTemplate<NodeInfo> {

    private static final NodeInfoTemplate instance = new NodeInfoTemplate();

    private NodeInfoTemplate() { }

    public static Template<NodeInfo> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, NodeInfo info, boolean required) throws IOException {
        throw new UnsupportedOperationException("NodeInfoTemplate.write");
    }

    @Override
    public NodeInfo read(Unpacker unpacker, NodeInfo info, boolean required) throws IOException {
        Map<String, Value> map = unpacker.read(Templates.tMap(Templates.TString, Templates.TValue));
        String identity = map.get("identity").asRawValue().getString();
        int uptime = map.get("uptime").asIntegerValue().getInt();
        Map<String, Value> runlist = map.get("apps") != null
                ? new Converter(map.get("apps")).read(Templates.tMap(Templates.TString, Templates.TValue))
                : new HashMap<String, Value>();

        return new NodeInfo(runlist, identity, uptime);
    }
}