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

@Service
public class ProductService {

    private KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public ProductService(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String createProduct(CreateProductDto createProductDto) {
        var productId = UUID.randomUUID().toString();
        var productCreatedEvent = new ProductCreatedEvent(
                productId,
                createProductDto.title(),
                createProductDto.price(),
                createProductDto.quantity());

        CompletableFuture<SendResult<String, ProductCreatedEvent>> future = kafkaTemplate
                .send(
                        "product-created-events-topic",
                        productId,
                        productCreatedEvent
                );

        future.whenComplete((result, exception) -> {
           if (exception != null) {
               LOGGER.error("Failed to send message: {}", exception.getMessage());
           } else {
               LOGGER.info("Message sent successfully: {}", result.getRecordMetadata());
           }
        });


        LOGGER.info("Return: {}", productId);

        return productId;
    }

}
