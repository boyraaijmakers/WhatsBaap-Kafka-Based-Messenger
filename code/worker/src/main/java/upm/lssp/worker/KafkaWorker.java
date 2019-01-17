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
import upm.lssp.View;
import upm.lssp.messages.Message;

import java.util.*;
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
        props.put("retries", 1);
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


        new Thread(() -> startConsumer(username)).start();


    }

    public void producer(Message message) {


        if (Config.DEBUG)
            System.out.println("Send producer: to@" + message.getReceiver() + " - from@" + message.getSender() + " - msg@" + message.getText());
        final ProducerRecord<String, String> record = new ProducerRecord<>(message.getReceiver(), message.getSender(), message.getText());
        try {
            RecordMetadata metadata = producer.send(record).get();
            if (Config.DEBUG) System.out.println("Record sent to partition " + metadata.partition()
                    + " with offset " + metadata.offset() + " to " + metadata.topic());
        } catch (ExecutionException | InterruptedException e) {
            if (Config.DEBUG) System.out.println("Error in sending record: " + e.getMessage());
        }


    }

    public void startConsumer(String username) {
        if (Config.DEBUG) System.out.println("Consumer started");
        try {
            consumer.subscribe(Collections.singletonList(username));

            while (!closed.get()) {
                List<Message> messages = new ArrayList<>();

                ConsumerRecords<String, String> records = consumer.poll(1000);

                for (ConsumerRecord<String, String> record : records) {
                    Message newMessage = new Message(record.key(), username, new Date(record.timestamp()), record.value());
                    messages.add(newMessage);
                }
                View.receiveMessage(messages);
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
