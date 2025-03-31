package com.example.kafka;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderRestController {

    private final ProducerService producerService;

    public OrderRestController(ProducerService producerService) {
        this.producerService = producerService;
    }

    @GetMapping("create-orders/{name}")
    public String create(@PathVariable(value = "name") String name) {
        producerService.sendMessage(name);
        return "Ok!";
    }
}
