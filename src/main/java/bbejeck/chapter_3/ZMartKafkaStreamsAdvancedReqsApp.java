package bbejeck.chapter_3;

import bbejeck.chapter_3.service.SecurityDBService;
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
import org.apache.kafka.streams.kstream.ForeachAction;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Predicate;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@SuppressWarnings("unchecked")
public class ZMartKafkaStreamsAdvancedReqsApp {

    private static final Logger LOG = LoggerFactory.getLogger(ZMartKafkaStreamsAdvancedReqsApp.class);

    public static void main(String[] args) throws Exception {

        StreamsConfig streamsConfig = new StreamsConfig(getProperties());

        //购物序列化
        Serde<Purchase> purchaseSerde = StreamsSerdes.PurchaseSerde();

        //购物分析
        Serde<PurchasePattern> purchasePatternSerde = StreamsSerdes.PurchasePatternSerde();

        //积分
        Serde<RewardAccumulator> rewardAccumulatorSerde = StreamsSerdes.RewardAccumulatorSerde();

        //String
        Serde<String> stringSerde = Serdes.String();

        StreamsBuilder builder = new StreamsBuilder();


        // previous requirements
        // 消费主题为交易，key-value为string-购物
        // 把消费数据，按照购物票映射，业务目标：脱敏
        KStream<String,Purchase> purchaseKStream = builder.stream( "transactions",
                Consumed.with(stringSerde, purchaseSerde))
                .mapValues(p -> Purchase.builder(p).maskCreditCard().build());

        // 上面的purchaseKStream，即继续脱敏后的数据
        // 把脱敏后数据映射为购物分析模型，即PurchasePattern
        KStream<String, PurchasePattern> patternKStream = purchaseKStream.mapValues(purchase ->
                PurchasePattern.builder(purchase).build());

        // 打印数据，标示为patterns
        patternKStream.print( Printed.<String, PurchasePattern>toSysOut().withLabel("patterns"));

        //发送到Kafka为rewards
        patternKStream.to("patterns", Produced.with(stringSerde,purchasePatternSerde));

        // 把脱敏后的数据映射为积分
        KStream<String, RewardAccumulator> rewardsKStream = purchaseKStream.mapValues(purchase ->
                RewardAccumulator.builder(purchase).build());

        //积分显示
        rewardsKStream.print(Printed.<String, RewardAccumulator>toSysOut().withLabel("rewards"));

        //发送到Kafka为rewards主题
        rewardsKStream.to("rewards", Produced.with(stringSerde,rewardAccumulatorSerde));

        // selecting a key for storage and filtering out low dollar purchases

        //函数，按照Key-Value，返回值为R,即String，Purchase返回值为Long
        KeyValueMapper<String, Purchase, Long> purchaseDateAsKey = (key, purchase) ->
                purchase.getPurchaseDate().getTime();

        //脱敏后数据，按照purchase过滤，而后Key分区
        KStream<Long, Purchase> filteredKStream = purchaseKStream.filter((key, purchase) ->
                purchase.getPrice() > 5.00).selectKey(purchaseDateAsKey);

        filteredKStream.print(Printed.<Long, Purchase>toSysOut().withLabel("purchases"));

        //发送到Kafka为purchases主题
        filteredKStream.to("purchases", Produced.with(Serdes.Long(),purchaseSerde));



        // branching stream for separating out purchases in new departments to their own topics
        // 脱敏后数据分裂条件
        Predicate<String, Purchase> isCoffee = (key, purchase) ->
                purchase.getDepartment().equalsIgnoreCase("coffee");
        Predicate<String, Purchase> isElectronics = (key, purchase) ->
                purchase.getDepartment().equalsIgnoreCase("electronics");

        int coffee = 0;
        int electronics = 1;

        //分为两种购物票据
        KStream<String, Purchase>[] kstreamByDept = purchaseKStream.branch(isCoffee, isElectronics);

        //咖啡票据
        kstreamByDept[coffee].to( "coffee", Produced.with(stringSerde, purchaseSerde));
        kstreamByDept[coffee].print(Printed.<String, Purchase>toSysOut().withLabel( "coffee"));

        //3c数据
        kstreamByDept[electronics].to("electronics", Produced.with(stringSerde, purchaseSerde));
        kstreamByDept[electronics].print(Printed.<String, Purchase>toSysOut().withLabel("electronics"));

        // security Requirements to record transactions for certain employee
        // 保存数据，以备审计，按照员工ID分类，以防止内部销售问题
        ForeachAction<String, Purchase> purchaseForeachAction = (key, purchase) ->
                SecurityDBService.saveRecord(purchase.getPurchaseDate(),
                        purchase.getEmployeeId(), purchase.getItemPurchased());

        //防欺诈操作
        purchaseKStream.filter((key, purchase) ->
                purchase.getEmployeeId().equals("000000")).foreach(purchaseForeachAction);


        // used only to produce data for this application, not typical usage
        // 模拟数据
        MockDataProducer.producePurchaseData();
        
        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(),streamsConfig);
        LOG.info("ZMart Advanced Requirements Kafka Streams Application Started");
        kafkaStreams.start();
        Thread.sleep(65000);
        LOG.info("Shutting down the Kafka Streams Application now");
        kafkaStreams.close();
        MockDataProducer.shutdown();
    }




    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "Example-Kafka-Streams-Job");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "streams-purchases");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "testing-streams-api");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG,
                WallclockTimestampExtractor.class);
        return props;
    }

}
