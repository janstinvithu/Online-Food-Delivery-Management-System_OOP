package com.fooddelivery.persistence;

import com.fooddelivery.config.SampleDataSeeder;
import com.fooddelivery.model.customer.Customer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class FilePersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void missingFileIsCreatedAndSeedIsNotDuplicatedAfterRestart() {
        TestStore first = TestStore.open(tempDir);
        assertTrue(SampleDataSeeder.seedIfEmpty(first.foods, first.customers, first.couriers, first.orders, first.payments));
        assertEquals(7, first.foods.count());
        assertEquals(4, first.customers.count());
        assertTrue(Files.exists(tempDir.resolve("food-delivery-data.json")));

        TestStore restarted = TestStore.open(tempDir);
        assertFalse(SampleDataSeeder.seedIfEmpty(restarted.foods, restarted.customers, restarted.couriers, restarted.orders, restarted.payments));
        assertEquals(4, restarted.customers.count());
        assertEquals("Eleanor Fernando", restarted.customers.findByEmailIgnoreCase("eleanor.fernando@vanguard.lk").orElseThrow().getFullName());
        assertEquals(4, restarted.orders.findAllWithDetails().size());
    }

    @Test
    void corruptedPrimaryFileIsRestoredFromBackup() throws Exception {
        TestStore store = TestStore.open(tempDir);
        SampleDataSeeder.seedIfEmpty(store.foods, store.customers, store.couriers, store.orders, store.payments);
        store.customers.save(new Customer("Backup Touch", "backup.touch@test.lk", "+94-77-0001111", "Colombo"));
        store.customers.save(new Customer("Later User", "later.user@test.lk", "+94-77-0002222", "Kandy"));
        Path primary = tempDir.resolve("food-delivery-data.json");
        assertTrue(Files.exists(tempDir.resolve("food-delivery-data.json.bak")));

        Files.writeString(primary, "{ this is not json", StandardCharsets.UTF_8);
        TestStore recovered = TestStore.open(tempDir);
        assertTrue(recovered.customers.findByEmailIgnoreCase("backup.touch@test.lk").isPresent());
        assertEquals(7, recovered.foods.count());
    }

    @Test
    void corruptFileWithoutBackupRefusesToReplaceData() throws Exception {
        Path directory = tempDir.resolve("broken");
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("food-delivery-data.json"), "<<<broken>>>", StandardCharsets.UTF_8);
        DataStoreException error = assertThrows(DataStoreException.class, () -> TestStore.open(directory));
        assertTrue(error.getMessage().contains("corrupted"));
        assertEquals("<<<broken>>>", Files.readString(directory.resolve("food-delivery-data.json")));
    }

    @Test
    void simultaneousUpdatesKeepEveryRecord() throws Exception {
        TestStore store = TestStore.open(tempDir);
        int writers = 8;
        ExecutorService pool = Executors.newFixedThreadPool(writers);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> tasks = new ArrayList<>();
        for (int i = 0; i < writers; i++) {
            int index = i;
            tasks.add(pool.submit(() -> {
                start.await();
                store.customers.save(new Customer(
                        "Concurrent User " + index,
                        "user" + index + "@test.lk",
                        "+94-77-10000" + index,
                        "Colombo"));
                return null;
            }));
        }
        start.countDown();
        for (Future<?> task : tasks) {
            task.get();
        }
        pool.shutdown();
        assertEquals(writers, store.customers.count());
        TestStore reopened = TestStore.open(tempDir);
        assertEquals(writers, reopened.customers.count());
    }

    @Test
    void projectDummyDataFileIsCreatedWhenMissing() throws Exception {
        Path file = Path.of("data", "food-delivery-data.json");
        if (!Files.exists(file) || Files.size(file) == 0) {
            TestStore store = TestStore.open(Path.of("data"));
            assertTrue(SampleDataSeeder.seedIfEmpty(store.foods, store.customers, store.couriers, store.orders, store.payments));
        }
        assertTrue(Files.size(file) > 0);
        String json = Files.readString(file);
        assertTrue(json.contains("\"schemaVersion\""));
        assertTrue(json.contains("Eleanor Fernando"));
        assertTrue(json.contains("Chicken Dum Biryani Special"));
    }
}
