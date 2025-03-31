package com.example.kafka;


public class Order {

    private String name;
    private Integer weight;

    public Order() {
    }

    public Order(String name, Integer age) {
        this.name = name;
        this.weight = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Order{" +
                "message='" + name + '\'' +
                ", weight=" + weight +
                '}';
    }
}
