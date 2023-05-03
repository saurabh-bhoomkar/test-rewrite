import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.consumer.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class KafkaAdminClientExample {

    private static final String KAFKA_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String TOPIC_NAME = "test-topic";

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Create admin client properties
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);

        // Create admin client instance
        try (AdminClient adminClient = AdminClient.create(properties)) {

            // Get list of consumer groups
            ListConsumerGroupsResult listConsumerGroupsResult = adminClient.listConsumerGroups();
            KafkaFuture<Collection<ConsumerGroupListing>> consumerGroupListings = listConsumerGroupsResult.all();
            Set<String> consumerGroups = consumerGroupListings.get().stream().map(ConsumerGroupListing::groupId).collect(Collectors.toSet());

            // Print consumer group IDs
            System.out.println("Consumer groups consuming from topic " + TOPIC_NAME + ":");
            for (String consumerGroup : consumerGroups) {
                if (adminClient.describeConsumerGroup(consumerGroup).all().get().stream()
                        .flatMap(consumerGroupDescription -> consumerGroupDescription.members().stream())
                        .flatMap(consumerGroupMember -> consumerGroupMember.assignment().topicPartitions().stream())
                        .anyMatch(topicPartition -> topicPartition.topic().equals(TOPIC_NAME))) {
                    System.out.println(consumerGroup);
                }
            }
        }
    }
}
