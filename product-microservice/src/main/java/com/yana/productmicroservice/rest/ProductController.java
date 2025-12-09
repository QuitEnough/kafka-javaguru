package com.yana.productmicroservice.rest;

import com.yana.productmicroservice.dto.CreateProductDto;
import com.yana.productmicroservice.dto.ErrorMessage;
import com.yana.productmicroservice.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/product")
public class ProductController {

    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Object> createProduct(@RequestBody CreateProductDto createProductDto) throws ExecutionException, InterruptedException {
        String productId = null;
        try {
            productId = productService.createProduct(createProductDto);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorMessage(
                            new Date(),
                            e.getMessage()
                    ));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(productId);
    }

}
