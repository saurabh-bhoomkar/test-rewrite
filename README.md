Properties props = new Properties();
props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-broker1:9092,kafka-broker2:9092");
AdminClient adminClient = AdminClient.create(props);

ListTopicsResult topicsResult = adminClient.listTopics();
Set<String> topicNames = topicsResult.names().get();

long totalMessages = 0;

for (String topicName : topicNames) {
    Map<String, TopicDescription> topicDescriptions = adminClient.describeTopics(Collections.singleton(topicName)).all().get();
    int numPartitions = topicDescriptions.get(topicName).partitions().size();
    List<TopicPartition> partitions = new ArrayList<>();
    for (int i = 0; i < numPartitions; i++) {
        partitions.add(new TopicPartition(topicName, i));
    }
    Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> offsets = adminClient.listOffsets(new ListOffsetsOptions().listMode(ListOffsetsRequest.ListOffsetsMode.LATEST), partitions).partitionsOffset().get();

    for (ListOffsetsResult.ListOffsetsResultInfo offset : offsets.values()) {
        totalMessages += offset.offset();
    }
}

adminClient.close();

System.out.println("Total messages: " + totalMessages);
