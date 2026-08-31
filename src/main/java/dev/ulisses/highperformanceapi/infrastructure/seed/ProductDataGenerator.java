package dev.ulisses.highperformanceapi.infrastructure.seed;

import dev.ulisses.highperformanceapi.domain.entity.Product;
import dev.ulisses.highperformanceapi.domain.enums.ProductStatus;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
public class ProductDataGenerator {

    private final Faker faker;
    private final Random random;

    private static final String[] SEARCH_TERMS = {
            "Phone",
            "Laptop",
            "Monitor",
            "Keyboard",
            "Mouse",
            "Headphones",
            "Tablet",
            "Camera",
            "Watch",
            "Speaker"
    };

    public ProductDataGenerator() {
        this.random = new Random(42);
        this.faker = new Faker(random);
    }

    public Product generate(long sequence) {
        Product product = new Product();

        product.setSku(String.format("PERF-%08d", sequence));

        product.setName(generateProductName());

        product.setDescription(
                faker.lorem().sentence(12)
        );

        product.setPrice(generatePrice());

        product.setStatus(ProductStatus.ACTIVE);

        return product;
    }

    private String generateProductName() {
        if (random.nextInt(100) < 30) {
            String term = SEARCH_TERMS[random.nextInt(SEARCH_TERMS.length)];
            return faker.commerce().productName() + " " + term;
        }

        return faker.commerce().productName();
    }

    private BigDecimal generatePrice() {
        double price = 10 + (random.nextDouble() * 1990);

        return BigDecimal
                .valueOf(price)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
