package cocaine;

import java.io.IOException;
import java.util.Map;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class RunlistTemplate extends AbstractTemplate<Runlist> {

    @Override
    public void write(Packer packer, Runlist runlist, boolean required) throws IOException {
        if (runlist == null || runlist.isEmpty()) {
            packer.writeNil();
        } else {
            packer.write(runlist.getRunlist());
        }
    }

    @Override
    public Runlist read(Unpacker unpacker, Runlist runlist, boolean required) throws IOException {
        if (!required && unpacker.trySkipNil()) {
            return Runlist.empty();
        }
        Map<String, String> list = unpacker.read(Templates.tMap(Templates.TString, Templates.TString));
        return new Runlist(list);
    }
}