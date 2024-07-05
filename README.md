@Component
public class DataProcessor {

    @Autowired
    private StreamsBuilderFactory streamsBuilderFactory;

    @Bean
    public KStream<String, String> processStream() {
        // Configure Kafka consumer and producer properties (refer to Spring Kafka docs)
        Map<String, Object> consumerProps = consumerProperties();
        Map<String, Object> producerProps = producerProperties();

        // Build the Kafka Streams topology
        KStreamBuilder builder = streamsBuilderFactory.getBuilder();
        KStream<String, String> stream = builder.stream("topic-A", Consumed.with(Serdes.String(), Serdes.String()));

        // Process the data (modify specific parts)
        KStream<String, String> processedStream = stream.map((key, value) -> {
            // Deserialize value to DataHolder object
            DataHolder dataHolder = JsonNodeDeserializer.deserialize(value, DataHolder.class);

            // Implement your data processing logic here
            // Modify data in the DataHolder object

            // Create a ProcessedDataHolder object
            ProcessedDataHolder processedDataHolder = new ProcessedDataHolder();
            // Map data from DataHolder to ProcessedDataHolder

            // Serialize ProcessedDataHolder to String
            String processedValue = JsonNodeSerializer.serialize(processedDataHolder);

            return new KeyValue<>(key, processedValue);
        });

        // Write the processed data to topic B
        processedStream.to("topic-B", Produced.with(Serdes.String(), Serdes.String()));

        return processedStream;
    }

    // Define consumer and producer property configuration methods (optional)
}
