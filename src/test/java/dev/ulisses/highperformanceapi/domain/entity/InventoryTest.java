package dev.ulisses.highperformanceapi.domain.entity;

import dev.ulisses.highperformanceapi.application.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(0);
    }

    @Test
    void shouldReserveInventory() {

        inventory.reserve(20);

        assertEquals(80, inventory.getAvailableQuantity());
        assertEquals(20, inventory.getReservedQuantity());
        assertEquals(100, inventory.getTotalQuantity());
    }

    @Test
    void shouldThrowExceptionWhenReservingMoreThanAvailable() {

        assertThrows(
                InsufficientStockException.class,
                () -> inventory.reserve(101)
        );

        assertEquals(100, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    void shouldReleaseReservedInventory() {

        inventory.reserve(30);

        inventory.release(10);

        assertEquals(80, inventory.getAvailableQuantity());
        assertEquals(20, inventory.getReservedQuantity());
        assertEquals(100, inventory.getTotalQuantity());
    }

    @Test
    void shouldDeductReservedInventory() {

        inventory.reserve(40);

        inventory.deduct(15);

        assertEquals(60, inventory.getAvailableQuantity());
        assertEquals(25, inventory.getReservedQuantity());
        assertEquals(85, inventory.getTotalQuantity());
    }

    @Test
    void shouldReserveAllAvailableInventory() {

        inventory.reserve(100);

        assertEquals(0, inventory.getAvailableQuantity());
        assertEquals(100, inventory.getReservedQuantity());
    }

    @Test
    void shouldReleaseAllReservedInventory() {

        inventory.reserve(100);

        inventory.release(100);

        assertEquals(100, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    void shouldDeductAllReservedInventory() {

        inventory.reserve(100);

        inventory.deduct(100);

        assertEquals(0, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    // Utility Tests
    @Test
    void shouldReturnCorrectTotalQuantity() {

        inventory.setAvailableQuantity(70);
        inventory.setReservedQuantity(30);

        assertEquals(100, inventory.getTotalQuantity());
    }

    // Reserve Tests
    @Test
    void shouldThrowExceptionWhenReservingZeroItems() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.reserve(0)
        );
    }

    @Test
    void shouldThrowExceptionWhenReservingNegativeItems() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.reserve(-1)
        );
    }

    // Release Tests
    @Test
    void shouldThrowExceptionWhenReleasingMoreThanReserved() {

        inventory.reserve(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.release(20)
        );
    }

    @Test
    void shouldThrowExceptionWhenReleasingZeroItems() {

        inventory.reserve(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.release(0)
        );
    }

    @Test
    void shouldThrowExceptionWhenReleasingNegativeItems() {

        inventory.reserve(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.release(-5)
        );
    }

    // Deduct Tests
    @Test
    void shouldThrowExceptionWhenDeductingMoreThanReserved() {

        inventory.reserve(15);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.deduct(20)
        );
    }

    @Test
    void shouldThrowExceptionWhenDeductingZeroItems() {

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.deduct(0)
        );
    }

    @Test
    void shouldThrowExceptionWhenDeductingNegativeItems() {

        inventory.reserve(10);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.deduct(-1)
        );
    }
}
