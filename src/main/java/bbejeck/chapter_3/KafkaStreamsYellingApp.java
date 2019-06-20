package bbejeck.chapter_3;

import bbejeck.clients.producer.MockDataProducer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaStreamsYellingApp {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaStreamsYellingApp.class);

    public static void main(String[] args) throws Exception {


        //Used only to produce data for this application, not typical usage
        MockDataProducer.produceRandomTextData();

        Properties props = new Properties();
        //应用ID
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "yelling_app_id");
        //卡夫卡服务器
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        StreamsConfig streamsConfig = new StreamsConfig(props);

        Serde<String> stringSerde = Serdes.String();

        StreamsBuilder builder = new StreamsBuilder();

        //消费队列，K-V的类型为String-String
        KStream<String, String> simpleFirstStream = builder.stream("src-topic",
                Consumed.with(stringSerde, stringSerde));

        //把值转大写
        KStream<String, String> upperCasedStream = simpleFirstStream.mapValues(String::toUpperCase);

        //输出消息
        upperCasedStream.to( "out-topic", Produced.with(stringSerde, stringSerde));

        //打印结果
        upperCasedStream.print(Printed.<String, String>toSysOut().withLabel("Yelling App"));


        KafkaStreams kafkaStreams = new KafkaStreams(builder.build(),streamsConfig);
        LOG.info("Hello World Yelling App Started");
        kafkaStreams.start();
        Thread.sleep(35000);
        LOG.info("Shutting down the Yelling APP now");
        kafkaStreams.close();
        MockDataProducer.shutdown();

    }
}