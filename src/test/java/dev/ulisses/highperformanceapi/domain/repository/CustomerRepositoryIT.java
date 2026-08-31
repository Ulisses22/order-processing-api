package dev.ulisses.highperformanceapi.domain.repository;

import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import dev.ulisses.highperformanceapi.domain.specification.CustomerSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(CustomerRepositoryIT.TestCacheConfiguration.class)
class CustomerRepositoryIT {

    @Autowired
    private CustomerRepository customerRepository;

    @TestConfiguration
    static class TestCacheConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }
    }

    @Test
    void shouldFindCustomerByEmail() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john.doe@example.com"
        );

        customerRepository.saveAndFlush(customer);

        Optional<Customer> result =
                customerRepository.findByEmail("john.doe@example.com");

        assertThat(result)
                .isPresent()
                .get()
                .extracting(Customer::getEmail)
                .isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john.doe@example.com"
        );

        customerRepository.saveAndFlush(customer);

        assertThat(
                customerRepository.existsByEmail("john.doe@example.com")
        ).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        assertThat(
                customerRepository.existsByEmail("unknown@example.com")
        ).isFalse();
    }

    @Test
    void shouldReturnFalseWhenEmailBelongsToSameCustomer() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john.doe@example.com"
        );

        customerRepository.saveAndFlush(customer);

        assertThat(
                customerRepository.existsByEmailAndIdNot(
                        "john.doe@example.com",
                        customer.getId()
                )
        ).isFalse();
    }

    @Test
    void shouldReturnTrueWhenEmailBelongsToAnotherCustomer() {
        Customer firstCustomer = createCustomer(
                "John",
                "Doe",
                "john.doe@example.com"
        );

        Customer secondCustomer = createCustomer(
                "Jane",
                "Doe",
                "jane.doe@example.com"
        );

        customerRepository.save(firstCustomer);
        customerRepository.saveAndFlush(secondCustomer);

        assertThat(
                customerRepository.existsByEmailAndIdNot(
                        "john.doe@example.com",
                        secondCustomer.getId()
                )
        ).isTrue();
    }

    @Test
    void shouldFindCustomersByNameSpecification() {
        Customer john = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        Customer jane = createCustomer(
                "Jane",
                "Smith",
                "jane@example.com"
        );

        customerRepository.save(john);
        customerRepository.saveAndFlush(jane);

        var result = customerRepository.findAll(
                CustomerSpecification.hasName("john")
        );

        assertThat(result)
                .hasSize(1)
                .extracting(Customer::getFirstName)
                .containsExactly("John");
    }

    @Test
    void shouldFindCustomersByEmailSpecification() {
        Customer john = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        Customer jane = createCustomer(
                "Jane",
                "Smith",
                "jane@example.com"
        );

        customerRepository.save(john);
        customerRepository.saveAndFlush(jane);

        var result = customerRepository.findAll(
                CustomerSpecification.hasEmail("JOHN@EXAMPLE")
        );

        assertThat(result)
                .hasSize(1)
                .extracting(Customer::getEmail)
                .containsExactly("john@example.com");
    }

    @Test
    void shouldIgnoreBlankNameSpecification() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        customerRepository.saveAndFlush(customer);

        var result = customerRepository.findAll(
                CustomerSpecification.hasName(" ")
        );

        assertThat(result)
                .extracting(Customer::getEmail)
                .contains("john@example.com");
    }

    @Test
    void shouldIgnoreBlankEmailSpecification() {
        Customer customer = createCustomer(
                "John",
                "Doe",
                "john@example.com"
        );

        customerRepository.saveAndFlush(customer);

        var result = customerRepository.findAll(
                CustomerSpecification.hasEmail(" ")
        );

        assertThat(result)
                .extracting(Customer::getEmail)
                .contains("john@example.com");
    }

    private Customer createCustomer(
            String firstName,
            String lastName,
            String email
    ) {
        Customer customer = new Customer();

        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setStatus(CustomerStatus.ACTIVE);

        return customer;
    }
}
