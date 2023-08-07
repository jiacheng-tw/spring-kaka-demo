package com.example.demo.producer;

import com.example.demo.config.DemoKafkaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoProducer {

    private final KafkaTemplate<String, String> stringKafkaTemplate;

    private final DemoKafkaProperties demoKafkaProperties;

    public void syncSendToTopic1(String value) {
        syncSendKafka(demoKafkaProperties.getTopic1().name(), Long.toString(System.currentTimeMillis()), value);
    }

    public void asyncSendToTopic2(String value) {
        asyncSendKafka(demoKafkaProperties.getTopic2().name(), Long.toString(System.currentTimeMillis()), value);
    }

    private void syncSendKafka(String topic, String key, String value) {
        try {
            var sendResult = stringKafkaTemplate
                    .send(new ProducerRecord<>(topic, key, value))
                    .get(10, TimeUnit.SECONDS);
            log.info("Kafka sync send to {} successfully with offset {}", topic, sendResult.getRecordMetadata().offset());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Kafka sync send failed", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void asyncSendKafka(String topic, String key, String value) {
        var sendResultFuture = stringKafkaTemplate.send(new ProducerRecord<>(topic, key, value));
        sendResultFuture.whenComplete((result, e) -> {
            if (Objects.isNull(e)) {
                log.info("Kafka async send to {} successfully with offset {}", topic, result.getRecordMetadata().offset());
            }
            else {
                log.error("Kafka async send failed", e);
            }
        });
    }
}
