package upm.lssp.messages;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class MessageDeserializer implements Deserializer {
    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> arg0, boolean arg1) {
    }

    @Override
    public Message deserialize(String arg0, byte[] arg1) {
        ObjectMapper mapper = new ObjectMapper();
        Message message = null;
        try {
            message = mapper.readValue(arg1, Message.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }
}