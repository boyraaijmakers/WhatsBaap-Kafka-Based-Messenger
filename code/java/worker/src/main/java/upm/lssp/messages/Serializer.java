package upm.lssp.messages;

import java.io.Closeable;
import java.util.Map;

public interface Serializer extends Closeable {
    void configure(Map<String, ?> var1, boolean var2);

    byte[] serialize(String var1, Message var2);

    void close();
}