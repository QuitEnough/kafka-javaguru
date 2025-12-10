package com.yana.transferservice.service;

import com.yana.core.events.DepositRequestedEvent;
import com.yana.core.events.WithdrawalRequestedEvent;
import com.yana.transferservice.dto.TransferRestModel;
import com.yana.transferservice.exception.TransferServiceException;
import com.yana.transferservice.persistence.TransferEntity;
import com.yana.transferservice.persistence.TransferRepository;
import org.apache.kafka.common.Uuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class TransferService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private KafkaTemplate<String, Object> kafkaTemplate;
    private Environment environment;
    private RestTemplate restTemplate;
    private TransferRepository transferRepository;

    public TransferService(KafkaTemplate<String, Object> kafkaTemplate,
                           Environment environment,
                           RestTemplate restTemplate,
                           TransferRepository transferRepository) {

        this.kafkaTemplate = kafkaTemplate;
        this.environment = environment;
        this.restTemplate = restTemplate;
        this.transferRepository = transferRepository;
    }

    // вариант 1 - предпочтительнее, т.к. унверсальный
    @Transactional("transactionManager")
    public boolean transfer(TransferRestModel transferRestModel) {
        WithdrawalRequestedEvent withdrawalEvent = new WithdrawalRequestedEvent(
                transferRestModel.senderId(),
                transferRestModel.recepientId(),
                transferRestModel.amount()
        );
        DepositRequestedEvent depositEvent = new DepositRequestedEvent(
                transferRestModel.senderId(),
                transferRestModel.recepientId(),
                transferRestModel.amount()
        );

        try {
            TransferEntity transferEntity = new TransferEntity();
            BeanUtils.copyProperties(transferRestModel, transferEntity);
            transferEntity.setTransferId(Uuid.randomUuid().toString());
            transferRepository.save(transferEntity);

            kafkaTemplate.send(environment
                    .getProperty("withdraw-money-topic", "withdraw-money-topic"),
                    withdrawalEvent);
            LOGGER.info("Sent event to withdrawal topic.");

            // business logic that causes and error
            callRemoteService();

            kafkaTemplate.send(environment
                    .getProperty("deposit-money-topic", "deposit-money-topic"),
                    depositEvent);
            LOGGER.info("Sent event to deposit topic.");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new TransferServiceException(e);
        }

        return true;
    }

    // вариант 2 - обработки транзакции. подходит только для работы с кафкой
//    @Transactional
//    public boolean transfer(TransferRestModel transferRestModel) {
//        WithdrawalRequestedEvent withdrawalEvent = new WithdrawalRequestedEvent(
//                transferRestModel.senderId(),
//                transferRestModel.recepientId(),
//                transferRestModel.amount()
//        );
//        DepositRequestedEvent depositEvent = new DepositRequestedEvent(
//                transferRestModel.senderId(),
//                transferRestModel.recepientId(),
//                transferRestModel.amount()
//        );
//
//        try {
//            boolean result = kafkaTemplate.executeInTransaction(t -> {
//                t.send(environment.getProperty("withdraw-money-topic", "withdraw-money-topic"),
//                        withdrawalEvent);
//                LOGGER.info("Sent event to withdrawal topic.");
//
//                t.send(environment.getProperty("deposit-money-topic", "deposit-money-topic"),
//                        depositEvent);
//                LOGGER.info("Sent event to deposit topic.");
//
//                return true;
//            });
//
//            callRemoteService();
//
//        } catch (Exception e) {
//            LOGGER.error(e.getMessage(), e);
//            throw new TransferServiceException(e);
//        }
//
//        return true;
//    }

    private ResponseEntity<String> callRemoteService() throws Exception {
        var requestUrl = "http://localhost:50513/response/200";
        var response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                null,
                String.class
                );

        if (response.getStatusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            throw new Exception("Destination Microservice not available");
        }

        if (response.getStatusCode().value() == HttpStatus.OK.value()) {
            LOGGER.info("Received response from mock service: " + response.getBody());
        }
        return response;
    }

}
