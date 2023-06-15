Open sidebar
Reactive Kafka Java Example


User
Java code for reactive kafka

ChatGPT
Sure! Here's an example of Java code for using Reactive Kafka to consume messages from a Kafka topic:

java
Copy code
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReactiveKafkaConsumerExample {
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC = "my-topic";

    public static void main(String[] args) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(props);

        Flux<ConsumerRecord<String, String>> kafkaFlux = KafkaReceiver.create(receiverOptions)
                .receiveAutoAck()
                .flatMapIterable(consumerRecords -> consumerRecords)
                .doOnNext(record -> {
                    System.out.println("Received message: key=" + record.key() + ", value=" + record.value() +
                            ", partition=" + record.partition() + ", offset=" + record.offset());
                });

        Disposable disposable = kafkaFlux.subscribe();

        // Wait for some time or perform other operations
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        disposable.dispose(); // Stop consuming
    }
}
Make sure to replace "localhost:9092" with the actual bootstrap servers of your Kafka cluster, and "my-topic" with the topic you want to consume messages from.

This code sets up a Reactive Kafka consumer using the KafkaReceiver class from the Reactor Kafka library. It configures the consumer with the necessary properties and creates a Flux of ConsumerRecord objects. Each received record is printed to the console.

The Disposable object returned from kafkaFlux.subscribe() is used to stop consuming after a certain period of time. In this example, it stops after waiting for 5 seconds. You can adjust the waiting time according to your needs or remove it to keep consuming indefinitely.

Remember to include the necessary dependencies in your project, such as Reactor Kafka and the Kafka clients:

xml
Copy code
<dependency>
    <groupId>io.projectreactor.kafka</groupId>
    <artifactId>reactor-kafka</artifactId>
    <version>1.4.8</version>
</dependency>
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
    <version>2.8.0</version>
</dependency>
Feel free to modify the code according to your specific requirements or add error handling and processing logic for the received messages.





Send a message.


Free Research Preview. ChatGPT may produce inaccurate information about people, places, or facts. ChatGPT May 24 Version
