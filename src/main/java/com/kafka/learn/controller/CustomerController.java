package com.kafka.learn.controller;

import com.kafka.learn.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @PostMapping(value = "/customer/create")
    public void createCustomer(@RequestParam UUID userId) {
        customerService.createUser(userId);
    }
}
