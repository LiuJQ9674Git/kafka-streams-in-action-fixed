package bbejeck.chapter_4;

import bbejeck.chapter_4.partitioner.RewardsStreamPartitioner;
import bbejeck.chapter_4.transformer.PurchaseRewardTransformer;
import bbejeck.clients.producer.MockDataProducer;
import bbejeck.model.Purchase;
import bbejeck.model.PurchasePattern;
import bbejeck.model.RewardAccumulator;
import bbejeck.util.serde.StreamsSerdes;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class ZMartKafkaStreamsAddStateApp {

    private static final Logger LOG = LoggerFactory.getLogger(ZMartKafkaStreamsAddStateApp.class);

    public static void main(String[] args) throws Exception {
        
        StreamsConfig streamsConfig = new StreamsConfig(getProperties());
        // 购物票
        Serde<Purchase> purchaseSerde = StreamsSerdes.PurchaseSerde();
        // 购物行为分析
        Serde<PurchasePattern> purchasePatternSerde = StreamsSerdes.PurchasePatternSerde();
        // 积分
        Serde<RewardAccumulator> rewardAccumulatorSerde = StreamsSerdes.RewardAccumulatorSerde();
        Serde<String> stringSerde = Serdes.String();

        StreamsBuilder builder = new StreamsBuilder();

        // 交易事务。脱敏
        KStream<String,Purchase> purchaseKStream = builder.stream( "transactions",
                Consumed.with(stringSerde, purchaseSerde))
                .mapValues(p -> Purchase.builder(p).maskCreditCard().build());

        // 数据映射为另外一个模型，即Purchase转为PurchasePattern
        KStream<String, PurchasePattern> patternKStream = purchaseKStream.mapValues(purchase ->
                PurchasePattern.builder(purchase).build());

        patternKStream.print(Printed.<String, PurchasePattern>toSysOut().withLabel("patterns"));
        patternKStream.to("patterns", Produced.with(stringSerde, purchasePatternSerde));



        // adding State to processor
        // 积分的状态数据
        String rewardsStateStoreName = "rewardsPointsStore";

        // 积分需要加和
        RewardsStreamPartitioner streamPartitioner = new RewardsStreamPartitioner();

        // 积分的本地存储
        KeyValueBytesStoreSupplier storeSupplier =
                Stores.inMemoryKeyValueStore(rewardsStateStoreName);

        // 积分按照Key-Value存储
        StoreBuilder<KeyValueStore<String, Integer>> storeBuilder =
                Stores.keyValueStoreBuilder(storeSupplier,
                Serdes.String(), Serdes.Integer());

        // 存储加入Builer
        builder.addStateStore(storeBuilder);

        // 按照String，购物票，分片来产生主题customer_transactions
        KStream<String, Purchase> transByCustomerStream =
                purchaseKStream.through( "customer_transactions",
                Produced.with(stringSerde, purchaseSerde, streamPartitioner));

        // 有状态数据
        // 可以访问状态存储实例来完成其任务
        KStream<String, RewardAccumulator> statefulRewardAccumulator =
                transByCustomerStream.transformValues(() ->
                        new PurchaseRewardTransformer(rewardsStateStoreName),
                rewardsStateStoreName);

        statefulRewardAccumulator.print(Printed.<String, RewardAccumulator>toSysOut().withLabel("rewards"));
        // 有状态数据
        statefulRewardAccumulator.to("rewards",
                Produced.with(stringSerde, rewardAccumulatorSerde));



        // used only to produce data for this application, not typical usage
        MockDataProducer.producePurchaseData();

        
        LOG.info("Starting Adding State Example");
        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(),streamsConfig);
        LOG.info("ZMart Adding State Application Started");
        kafkaStreams.cleanUp();
        kafkaStreams.start();
        Thread.sleep(65000);
        LOG.info("Shutting down the Add State Application now");
        kafkaStreams.close();
        MockDataProducer.shutdown();
    }




    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "AddingStateConsumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "AddingStateGroupId");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "AddingStateAppId");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG,
                WallclockTimestampExtractor.class);
        return props;
    }

}
