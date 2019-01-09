package upm.lssp.worker;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import upm.lssp.Config;
import upm.lssp.messages.Message;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class KafkaWorker {


    private static final Integer MESSAGE_COUNT = 1000;

    private static final String TOPIC_NAME = "demo";
    private static final String GROUP_ID_CONFIG = "consumerGroup1";
    private static final Integer MAX_NO_MESSAGE_FOUND_COUNT = 100;
    private static final String OFFSET_RESET_LATEST = "latest";
    private static final String OFFSET_RESET_EARLIER = "earliest";
    private static final Integer MAX_POLL_RECORDS = 1;

    private Producer<String, Message> producer;
    private KafkaConsumer<String, Message> consumer;

    private Properties props;
    private AtomicBoolean closed;


    public KafkaWorker(String username) {

        props = new Properties();
        props.put("bootstrap.servers", Config.KAFKABROKER);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "upm.lssp.messages.MessageSerializer");
        props.put("key.deserializer", "upm.lssp.messages.MessageDeserializer");
        props.put("value.serializer", "upm.lssp.messages.MessageSerializer");
        props.put("value.deserializer", "upm.lssp.messages.MessageDeserializer");

        this.producer = new KafkaProducer<>(props);
        this.consumer = new KafkaConsumer<>(props);

        //this.closed = new AtomicBoolean(false);

    }

    public boolean sendMessage(Message message) {

        String topic = getKafkaTopicName(message.getSender(), message.getReceiver());


        this.producer.send(new ProducerRecord<>(topic, message));

        return true;

    }

    public void subscribeConsumer(String username, List<String> topics) {
        topics = topics.stream().map(t -> getKafkaTopicName(username, t)).collect(Collectors.toList());
        this.consumer.unsubscribe();
        this.consumer.subscribe(topics);

    }


    private String getKafkaTopicName(String user1, String user2) {
        List<String> topic = Arrays.asList(user1, user2);
        Collections.sort(topic);
        String input = topic.get(0) + topic.get(1);
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input.getBytes(StandardCharsets.UTF_8), 0, input.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha1;
    }


    public void receiveMessages() {


        //int noMessageFound = 0;
        while (true) {
            ConsumerRecords<String, Message> consumerRecords = consumer.poll(1000);

            /*if (consumerRecords.count() == 0) {
                noMessageFound++;
                if (noMessageFound > IKafkaConstants.MAX_NO_MESSAGE_FOUND_COUNT)
                    // If no message found count is reached to threshold exit loop.
                    break;
                else
                    continue;
            }*/
            //print each record.

            ArrayList<Message> messages = new ArrayList<>();
            consumerRecords.forEach(m -> messages.add(m.value()));

            consumerRecords.forEach(record -> {

                System.out.println("Record Key " + record.key());
                System.out.println("Record value " + record.value());
                System.out.println("Record partition " + record.partition());
                System.out.println("Record offset " + record.offset());


            });

            // commits the offset of record to broker.
            consumer.commitAsync();
        }

    }


}
