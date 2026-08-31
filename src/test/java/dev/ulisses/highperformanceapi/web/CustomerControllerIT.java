package dev.ulisses.highperformanceapi.web;

import dev.ulisses.highperformanceapi.application.dto.request.UpdateCustomerRequest;
import dev.ulisses.highperformanceapi.support.IntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import dev.ulisses.highperformanceapi.application.dto.request.CreateCustomerRequest;
import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomerControllerIT extends IntegrationTest {

    private String accessToken;

    @BeforeAll
    void authenticateOnce() throws Exception {
        accessToken = authenticate();
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomerSuccessfully() throws Exception {

        CreateCustomerRequest request =
                new CreateCustomerRequest(
                        "Willian",
                        "Clark",
                        "user@email.com"
                );

        mockMvc.perform(
                        post("/api/v1/customers")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Willian"))
                .andExpect(jsonPath("$.lastName").value("Clark"))
                .andExpect(jsonPath("$.email").value("user@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        Customer customer = customerRepository
                .findByEmail("user@email.com")
                .orElseThrow();

        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    @DisplayName("Should find customer by id")
    void shouldFindCustomerById() throws Exception {

        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@email.com");
        customer.setStatus(CustomerStatus.ACTIVE);

        customer = customerRepository.save(customer);

        mockMvc.perform(
                        get("/api/v1/customers/{id}", customer.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customer.getId().toString()))
                .andExpect(jsonPath("$.email").value(customer.getEmail()));
    }

    @Test
    @DisplayName("Should return 404 when customer does not exist")
    void shouldReturn404WhenCustomerNotFound() throws Exception {

        mockMvc.perform(
                        get("/api/v1/customers/{id}", UUID.randomUUID())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return paged customers")
    void shouldReturnPagedCustomers() throws Exception {

        mockMvc.perform(
                        get("/api/v1/customers")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(20)))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.numberOfElements").value(20));
    }

    @Test
    @DisplayName("Should update customer successfully")
    void shouldUpdateCustomerSuccessfully() throws Exception {

        Customer customer = new Customer();
        customer.setFirstName("Old");
        customer.setLastName("Name");
        customer.setEmail("old@email.com");
        customer.setStatus(CustomerStatus.ACTIVE);

        customer = customerRepository.save(customer);

        UpdateCustomerRequest request =
                new UpdateCustomerRequest(
                        "New",
                        "Customer"
                );

        mockMvc.perform(
                        put("/api/v1/customers/{id}", customer.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("Customer"));
    }

    @Test
    @DisplayName("Should delete customer successfully")
    void shouldDeleteCustomerSuccessfully() throws Exception {

        Customer customer = new Customer();
        customer.setFirstName("Delete");
        customer.setLastName("Me");
        customer.setEmail("delete@email.com");
        customer.setStatus(CustomerStatus.ACTIVE);

        customer = customerRepository.save(customer);

        mockMvc.perform(
                        delete("/api/v1/customers/{id}", customer.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                )
                .andExpect(status().isNoContent());

        assertFalse(customerRepository.existsById(customer.getId()));
    }

    @Test
    @DisplayName("Should return 404 when deleting unknown customer")
    void shouldReturn404WhenDeletingUnknownCustomer() throws Exception {

        mockMvc.perform(
                        delete("/api/v1/customers/{id}", UUID.randomUUID())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 when request is invalid")
    void shouldReturn400WhenRequestIsInvalid() throws Exception {

        CreateCustomerRequest request =
                new CreateCustomerRequest(
                        "",
                        "",
                        "invalid-email"
                );

        mockMvc.perform(
                        post("/api/v1/customers")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 when email already exists")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {

        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("duplicate@email.com");
        customer.setStatus(CustomerStatus.ACTIVE);

        customerRepository.save(customer);

        CreateCustomerRequest request =
                new CreateCustomerRequest(
                        "Jane",
                        "Doe",
                        "duplicate@email.com"
                );

        mockMvc.perform(
                        post("/api/v1/customers")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is(HttpStatus.CONFLICT.value()));
    }

    @Test
    @DisplayName("Should search customers by name")
    void shouldSearchCustomersByName() throws Exception {

        Customer john = new Customer();
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setEmail("john.search@email.com");
        john.setStatus(CustomerStatus.ACTIVE);

        Customer jane = new Customer();
        jane.setFirstName("Jane");
        jane.setLastName("Smith");
        jane.setEmail("jane.search@email.com");
        jane.setStatus(CustomerStatus.ACTIVE);

        customerRepository.save(john);
        customerRepository.saveAndFlush(jane);

        mockMvc.perform(
                        get("/api/v1/customers")
                                .param("name", "john")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.content[0].email")
                        .value("john.search@email.com"));
    }

    @Test
    @DisplayName("Should search customers by email")
    void shouldSearchCustomersByEmail() throws Exception {

        Customer john = new Customer();
        john.setFirstName("John");
        john.setLastName("Doe");
        john.setEmail("john.search@email.com");
        john.setStatus(CustomerStatus.ACTIVE);

        Customer jane = new Customer();
        jane.setFirstName("Jane");
        jane.setLastName("Smith");
        jane.setEmail("jane.search@email.com");
        jane.setStatus(CustomerStatus.ACTIVE);

        customerRepository.save(john);
        customerRepository.saveAndFlush(jane);

        mockMvc.perform(
                        get("/api/v1/customers")
                                .param("email", "john.search")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].email")
                        .value("john.search@email.com"));
    }

    @Test
    @DisplayName("Should apply requested pagination")
    void shouldApplyRequestedPagination() throws Exception {

        Customer customer1 = new Customer();
        customer1.setFirstName("John");
        customer1.setLastName("One");
        customer1.setEmail("pagination1@email.com");
        customer1.setStatus(CustomerStatus.ACTIVE);

        Customer customer2 = new Customer();
        customer2.setFirstName("Jane");
        customer2.setLastName("Two");
        customer2.setEmail("pagination2@email.com");
        customer2.setStatus(CustomerStatus.ACTIVE);

        Customer customer3 = new Customer();
        customer3.setFirstName("Mark");
        customer3.setLastName("Three");
        customer3.setEmail("pagination3@email.com");
        customer3.setStatus(CustomerStatus.ACTIVE);

        customerRepository.save(customer1);
        customerRepository.save(customer2);
        customerRepository.saveAndFlush(customer3);

        mockMvc.perform(
                        get("/api/v1/customers")
                                .param("page", "0")
                                .param("size", "2")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + accessToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.numberOfElements").value(2));
    }
}
