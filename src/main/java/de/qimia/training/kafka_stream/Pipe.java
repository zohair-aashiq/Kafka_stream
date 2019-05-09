package de.qimia.training.kafka_stream;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import scala.Int;

import javax.sound.midi.Soundbank;
import java.sql.SQLOutput;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;

public class Pipe {

    static String Record(String record, MultiMap multiMap){
        List<String> eventIDs = Arrays.asList(record.split("packageId")[0].split(":")[1].split(",")[0]);
        List<String> packageIds = Arrays.asList(record.split("packageId")[1].split(",")[0].split(":")[1]);
        for(int i=0;i<packageIds.size();i++)
        {
            for(int j=0;j<eventIDs.size();j++) {
                multiMap.put(packageIds.get(i),eventIDs.get(j));
            }
        }
        return multiMap.toString();
    }
    public static void main(String[] args) throws Exception {
        String topicName1 = args[0];
        String topicName2 = args[1];
        String localHost =  args[2];
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, localHost);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        MultiMap multiMap = new MultiValueMap();
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> stream1 = builder.stream(topicName1);
        final KStream<String, String> returnStream = stream1.map((k, v) -> new KeyValue<>(k, Record(v,multiMap)));

        returnStream.to(topicName2);

        final Topology topology = builder.build();

        final KafkaStreams streams = new KafkaStreams(topology, props);

        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            System.out.println(streams.toString());
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

}
