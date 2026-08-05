package dev.ulisses.highperformanceapi.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerIT {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    

}
