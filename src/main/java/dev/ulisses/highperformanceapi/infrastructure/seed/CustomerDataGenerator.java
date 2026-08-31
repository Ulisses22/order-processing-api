package dev.ulisses.highperformanceapi.infrastructure.seed;

import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.enums.CustomerStatus;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class CustomerDataGenerator {

    private final Faker faker;

    public CustomerDataGenerator() {
        this.faker = new Faker(new Random(42));
    }

    public Customer generate(long sequence) {
        Customer customer = new Customer();

        customer.setFirstName(faker.name().firstName());
        customer.setLastName(faker.name().lastName());
        customer.setEmail("customer-" + sequence + "@email.test");
        customer.setPhone(faker.phoneNumber().phoneNumber());
        customer.setStatus(CustomerStatus.ACTIVE);

        return customer;
    }
}
