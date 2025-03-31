package com.example.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ProducerService {

    private static final Logger log = LoggerFactory.getLogger(ProducerService.class);

    public final KafkaTemplate<String, Order> kafkaTemplate;

    public ProducerService(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        Order order = new Order();
        order.setName(message);
        order.setWeight(new Random().nextInt(1, 50));

        try {
            kafkaTemplate.send("orders", order).get();
        } catch (Exception ex) {
            log.debug("Ошибка отправки сообщения в топик [orders]. " + ex.getMessage());
            throw new RuntimeException("Ошибка отправки сообщения в топик [orders]. " + ex.getMessage());
        }

        log.debug("Сообщение отправлено в топик [orders]. Message: " + order);
    }
}
