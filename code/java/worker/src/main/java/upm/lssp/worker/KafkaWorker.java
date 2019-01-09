package upm.lssp.worker;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.WakeupException;
import upm.lssp.Config;
import upm.lssp.messages.Message;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaWorker {

    private Producer<String, String> producer;
    private KafkaConsumer<String, String> consumer;

    private Properties props;
    private AtomicBoolean closed = new AtomicBoolean(false);


    public KafkaWorker(String username) {

        props = new Properties();
        props.put("bootstrap.servers", Config.KAFKABROKER);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "WB");

        this.producer = new KafkaProducer<>(props);
        this.consumer = new KafkaConsumer<>(props);

        new Thread(() -> startConsumer(username));


    }

    public void producer(Message message) {

        //this.producer.send(new ProducerRecord<>(message.getReceiver(),message.getSender(),message.getText()));
        final ProducerRecord<String, String> record = new ProducerRecord<>("topic", "sender", "message");
        try {
            RecordMetadata metadata = producer.send(record).get();
            System.out.println("Record sent to partition " + metadata.partition()
                    + " with offset " + metadata.offset() + " to " + metadata.topic());
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Error in sending record");
            System.out.println(e);
        }


    }

    public void startConsumer(String username) {
        try {
            consumer.subscribe(Collections.singletonList(username));
            while (!closed.get()) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records)
                    System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        } catch (WakeupException e) {
            if (!closed.get()) throw e;
        } finally {
            consumer.close();
        }
    }


    public void shutdownConsumer() {
        closed.set(true);
        consumer.wakeup();
    }





}
