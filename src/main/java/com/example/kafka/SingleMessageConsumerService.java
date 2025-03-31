package com.example.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class SingleMessageConsumerService {
    private static final Logger log = LoggerFactory.getLogger(SingleMessageConsumerService.class);

    @KafkaListener(topics = "orders", groupId = "orders-single",containerFactory = "kafkaListenerContainerFactorySingleMessageConsumer")
    public void listenStringMessage(@Payload Order order) {
        log.debug("Получено сообщение из топика orders. Message: " + order);
    }
}

