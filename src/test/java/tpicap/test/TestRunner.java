package tpicap.test;

import tpicap.domain.Direction;
import tpicap.domain.MatchState;
import tpicap.domain.Order;
import tpicap.domain.OrderResult;
import tpicap.strategy.PriceTimeStrategy;
import tpicap.strategy.ProRataStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Simple test runner for tpicap matching engine tests.
 * Runs all test cases and reports results without external dependencies.
 */
public class TestRunner {

    private int testsRun = 0;
    private int testsPassed = 0;
    private int testsFailed = 0;

    public static void main(String[] args) {
        TestRunner runner = new TestRunner();
        runner.runAllTests();
    }

    public void runAllTests() {
        System.out.println("======================================");
        System.out.println("TP ICAP Matching Engine Tests");
        System.out.println("======================================\n");

        // Price-Time-Priority Tests
        System.out.println("--- Price-Time-Priority Strategy Tests ---");
        testPriceTimeUserStoryOne();
        testPriceTimePriorityOrdering();
        testHigherPriceTakesPriority();
        testNoMatchWhenPriceDoesNotMeet();
        testPriceTimePartialFill();
        testPriceTimeEmptyBook();
        testPriceTimeOnlyBuys();
        testPriceTimeOnlySells();

        // Pro-Rata Tests
        System.out.println("\n--- Pro-Rata Strategy Tests ---");
        testProRataUserStoryTwo();
        testProRataDistribution();
        testProRataDifferentPrices();
        testProRataNoEligibleBuys();
        testProRataMultipleSells();
        testProRataEmptyBook();
        testProRataOnlyBuys();
        testProRataOnlySells();
        testProRataVolumeConservation();

        // Summary
        System.out.println("\n======================================");
        System.out.println("Test Summary:");
        System.out.println("  Total:  " + testsRun);
        System.out.println("  Passed: " + testsPassed);
        System.out.println("  Failed: " + testsFailed);
        System.out.println("======================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private void assertTrue(boolean condition, String message) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            testsFailed++;
            System.out.println("  [FAIL] " + message);
        }
    }

    private void assertEquals(Object expected, Object actual, String message) {
        boolean pass = (expected == null && actual == null) ||
                        (expected != null && expected.equals(actual));
        assertTrue(pass, message + " (expected: " + expected + ", actual: " + actual + ")");
    }

    private void assertEquals(int expected, int actual, String message) {
        assertTrue(expected == actual, message + " (expected: " + expected + ", actual: " + actual + ")");
    }

    // ==================== Price-Time-Priority Tests ====================

    private void testPriceTimeUserStoryOne() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 4.99, "09:27:43"),
            new Order("B", "B1", Direction.BUY, 200, 5.00, "10:21:46"),
            new Order("C", "C1", Direction.BUY, 150, 5.00, "10:26:18"),
            new Order("D", "D1", Direction.SELL, 150, 5.00, "10:32:41"),
            new Order("E", "E1", Direction.SELL, 100, 5.00, "10:33:07")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(5, results.size(), "All orders should have results");
        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState(), "A1 - NO_MATCH");
        assertEquals(MatchState.FULL_MATCH, results.get("B1").order.getMatchState(), "B1 - FULL_MATCH");
        assertEquals(MatchState.PARTIAL_MATCH, results.get("C1").order.getMatchState(), "C1 - PARTIAL_MATCH");
        assertEquals(MatchState.FULL_MATCH, results.get("D1").order.getMatchState(), "D1 - FULL_MATCH");
        assertEquals(MatchState.FULL_MATCH, results.get("E1").order.getMatchState(), "E1 - FULL_MATCH");
        assertEquals(0, results.get("B1").order.remainingVolume, "B1 remaining volume");
        assertEquals(100, results.get("C1").order.remainingVolume, "C1 remaining volume");
    }

    private void testPriceTimePriorityOrdering() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.00, "08:59:00"),
            new Order("C", "C1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals("B1", results.get("C1").matches.get(0).orderId,
            "Earlier order B1 takes priority");
    }

    private void testHigherPriceTakesPriority() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.50, "09:01:00"),
            new Order("C", "C1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals("B1", results.get("C1").matches.get(0).orderId,
            "Higher price order B1 takes priority");
    }

    private void testNoMatchWhenPriceDoesNotMeet() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 4.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState(),
            "No match when buy price < sell price");
    }

    private void testPriceTimePartialFill() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 500, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 200, 5.00, "10:00:00"),
            new Order("C", "C1", Direction.SELL, 200, 5.00, "10:01:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.PARTIAL_MATCH, results.get("A1").order.getMatchState(),
            "A1 partial match");
        assertEquals(100, results.get("A1").order.remainingVolume, "A1 remaining 100");
    }

    private void testPriceTimeEmptyBook() {
        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(Arrays.asList());
        assertTrue(results.isEmpty(), "Empty book returns empty results");
    }

    private void testPriceTimeOnlyBuys() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.50, "09:01:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState(), "Only buys - no match");
    }

    private void testPriceTimeOnlySells() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.SELL, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 100, 5.50, "09:01:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState(), "Only sells - no match");
    }

    // ==================== Pro-Rata Tests ====================

    private void testProRataUserStoryTwo() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 50, 5.00, "09:27:43"),
            new Order("B", "B1", Direction.BUY, 200, 5.00, "10:21:46"),
            new Order("C", "C1", Direction.SELL, 200, 5.00, "10:26:18")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.PARTIAL_MATCH, results.get("A1").order.getMatchState(), "A1 partial");
        assertEquals(MatchState.PARTIAL_MATCH, results.get("B1").order.getMatchState(), "B1 partial");
        assertEquals(MatchState.FULL_MATCH, results.get("C1").order.getMatchState(), "C1 full");
        assertEquals(10, results.get("A1").order.remainingVolume, "A1 remaining 10");
        assertEquals(40, results.get("B1").order.remainingVolume, "B1 remaining 40");
    }

    private void testProRataDistribution() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 200, 5.00, "09:01:00"),
            new Order("C", "C1", Direction.SELL, 150, 5.00, "10:00:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(50, results.get("A1").matches.get(0).volume, "A1 gets 1/3 (50)");
        assertEquals(100, results.get("B1").matches.get(0).volume, "B1 gets 2/3 (100)");
    }

    private void testProRataDifferentPrices() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.50, "09:01:00"),
            new Order("C", "C1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(1, results.get("C1").matches.size(), "C1 only matches same price");
        assertEquals("A1", results.get("C1").matches.get(0).orderId, "C1 matches A1 only");
    }

    private void testProRataNoEligibleBuys() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 4.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.NO_MATCH, results.get("B1").order.getMatchState(), "No eligible buys");
    }

    private void testProRataMultipleSells() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 300, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 100, 5.00, "10:00:00"),
            new Order("C", "C1", Direction.SELL, 100, 5.00, "10:01:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.FULL_MATCH, results.get("B1").order.getMatchState(), "B1 full");
        assertEquals(MatchState.FULL_MATCH, results.get("C1").order.getMatchState(), "C1 full");
    }

    private void testProRataEmptyBook() {
        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(Arrays.asList());
        assertTrue(results.isEmpty(), "Empty book");
    }

    private void testProRataOnlyBuys() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.50, "09:01:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState(), "Pro-rata only buys");
    }

    private void testProRataOnlySells() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.SELL, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 100, 5.50, "09:01:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState(), "Pro-rata only sells");
    }

    private void testProRataVolumeConservation() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.00, "09:01:00"),
            new Order("C", "C1", Direction.SELL, 120, 5.00, "10:00:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        int totalMatched = results.get("C1").matches.stream()
            .mapToInt(m -> m.volume).sum();
        assertEquals(120, totalMatched, "Volume conservation");
    }
}
