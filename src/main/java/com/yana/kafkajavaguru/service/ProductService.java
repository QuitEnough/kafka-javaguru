package com.yana.kafkajavaguru.service;

import com.yana.kafkajavaguru.dto.CreateProductDto;
import com.yana.kafkajavaguru.event.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ProductService {

    private KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductService(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String createProduct(CreateProductDto createProductDto) throws ExecutionException, InterruptedException {
        var productId = UUID.randomUUID().toString();
        var productCreatedEvent = new ProductCreatedEvent(
                productId,
                createProductDto.title(),
                createProductDto.price(),
                createProductDto.quantity());

        SendResult<String, ProductCreatedEvent> result = kafkaTemplate
                .send(
                        "product-created-events-topic",
                        productId,
                        productCreatedEvent
                ).get();

        LOGGER.info("Topic: {}", result.getRecordMetadata().topic());
        LOGGER.info("Partition: {}", result.getRecordMetadata().partition());
        LOGGER.info("Offset: {}", result.getRecordMetadata().offset());


        LOGGER.info("Return: {}", productId);

        return productId;
    }

}
