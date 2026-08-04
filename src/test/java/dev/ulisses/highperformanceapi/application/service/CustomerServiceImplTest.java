package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.mapper.CustomerMapper;
import dev.ulisses.highperformanceapi.application.service.impl.CustomerServiceImpl;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

}
