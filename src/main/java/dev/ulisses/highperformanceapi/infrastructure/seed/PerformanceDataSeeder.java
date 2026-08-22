package dev.ulisses.highperformanceapi.infrastructure.seed;

import dev.ulisses.highperformanceapi.domain.entity.Customer;
import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.repository.CustomerRepository;
import dev.ulisses.highperformanceapi.domain.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class PerformanceDataSeeder implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(PerformanceDataSeeder.class);

    private final SeedProperties seedProperties;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CustomerDataGenerator customerDataGenerator;
    private final ProductDataGenerator productDataGenerator;

    @PersistenceContext
    private EntityManager entityManager;

    public PerformanceDataSeeder(
            SeedProperties seedProperties,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            CustomerDataGenerator customerDataGenerator,
            ProductDataGenerator productDataGenerator
    ) {
        this.seedProperties = seedProperties;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.customerDataGenerator = customerDataGenerator;
        this.productDataGenerator = productDataGenerator;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedProperties.enabled()) {
            return;
        }

        Instant start = Instant.now();

        log.info("Starting performance data seeding...");

        seedCustomers();
        seedProducts();

        Duration duration = Duration.between(start, Instant.now());

        log.info(
                "Performance data seeding completed in {} seconds.",
                duration.toMillis() / 1000.0
        );
    }

    private void seedCustomers() {
        int total = seedProperties.customers();

        if (total <= 0) {
            log.info("Customer seed skipped: configured amount is {}", total);
            return;
        }

        if (customerRepository.count() > 0) {
            log.info("Customers already exist. Skipping customer seed.");
            return;
        }

        log.info("Seeding {} customers...", total);

        int batchSize = seedProperties.batchSize();

        for (int start = 0; start < total; start += batchSize) {

            int end = Math.min(start + batchSize, total);

            List<Customer> customers = new ArrayList<>(end - start);

            for (int i = start; i < end; i++) {
                customers.add(
                        customerDataGenerator.generate(i + 1L)
                );
            }

            customerRepository.saveAll(customers);

            entityManager.flush();
            entityManager.clear();

            log.info(
                    "Customers seeded: {}/{}",
                    end,
                    total
            );
        }
    }

    private void seedProducts() {
        int total = seedProperties.products();

        if (total <= 0) {
            log.info("Product seed skipped: configured amount is {}", total);
            return;
        }

        if (productRepository.count() > 0) {
            log.info("Products already exist. Skipping product seed.");
            return;
        }

        log.info("Seeding {} products...", total);

        int batchSize = seedProperties.batchSize();

        for (int start = 0; start < total; start += batchSize) {

            int end = Math.min(start + batchSize, total);

            List<Product> products = new ArrayList<>(end - start);

            for (int i = start; i < end; i++) {
                products.add(
                        productDataGenerator.generate(i + 1L)
                );
            }

            productRepository.saveAll(products);

            entityManager.flush();
            entityManager.clear();

            log.info(
                    "Products seeded: {}/{}",
                    end,
                    total
            );
        }
    }
}
