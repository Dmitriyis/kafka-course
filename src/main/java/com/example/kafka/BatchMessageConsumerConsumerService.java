package com.example.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class BatchMessageConsumerConsumerService {

    private static final Logger log = LoggerFactory.getLogger(BatchMessageConsumerConsumerService.class);

    @KafkaListener(topics = "orders", groupId = "orders-batch", containerFactory = "kafkaListenerContainerFactoryBatchMessageConsumer")
    public void listenStringMessage(@Payload List<Order> orders, Acknowledgment ack) {
        boolean hasErrors = orders.stream()
                .anyMatch(Objects::isNull);
        if (hasErrors) {
            throw new DeserializationException("failed to deserialize", null, true, null);
        }

        log.debug("Получено сообщения из топика orders. Размер сообщения: " + orders.size());

        orders.forEach(order -> {
            log.debug(order.toString());
        });
        ack.acknowledge();
    }
}
