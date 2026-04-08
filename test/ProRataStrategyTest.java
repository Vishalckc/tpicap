package tpicap.test;

import org.junit.jupiter.api.Test;
import tpicap.domain.Direction;
import tpicap.domain.MatchState;
import tpicap.domain.Order;
import tpicap.domain.OrderResult;
import tpicap.strategy.ProRataStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for User Story Two: Pro-Rata matching algorithm
 */
public class ProRataStrategyTest {

    @Test
    void testUserStoryTwoAcceptanceCriteria() {
        // Given: Order Book from User Story Two
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 50, 5.00, "09:27:43"),
            new Order("B", "B1", Direction.BUY, 200, 5.00, "10:21:46"),
            new Order("C", "C1", Direction.SELL, 200, 5.00, "10:26:18")
        );

        // When: Matching with Pro-Rata strategy
        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        // Then: Verify all orders have results
        assertEquals(3, results.size(), "All orders should have results");

        // A1 - PartialMatch (40 matched, 10 remaining)
        OrderResult a1Result = results.get("A1");
        assertNotNull(a1Result, "A1 should have a result");
        assertEquals(MatchState.PARTIAL_MATCH, a1Result.order.getMatchState(),
            "A1 should be PARTIAL_MATCH");
        assertEquals(10, a1Result.order.remainingVolume, "A1 should have 10 remaining");
        assertEquals(1, a1Result.matches.size(), "A1 should have one match");
        assertTrue(a1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("C1") && m.volume == 40), "A1 should match 40 with C1");

        // B1 - PartialMatch (160 matched, 40 remaining)
        OrderResult b1Result = results.get("B1");
        assertNotNull(b1Result, "B1 should have a result");
        assertEquals(MatchState.PARTIAL_MATCH, b1Result.order.getMatchState(),
            "B1 should be PARTIAL_MATCH");
        assertEquals(40, b1Result.order.remainingVolume, "B1 should have 40 remaining");
        assertEquals(1, b1Result.matches.size(), "B1 should have one match");
        assertTrue(b1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("C1") && m.volume == 160), "B1 should match 160 with C1");

        // C1 - FullMatch (200 matched: 40 to A1, 160 to B1)
        OrderResult c1Result = results.get("C1");
        assertNotNull(c1Result, "C1 should have a result");
        assertEquals(MatchState.FULL_MATCH, c1Result.order.getMatchState(),
            "C1 should be FULL_MATCH");
        assertEquals(0, c1Result.order.remainingVolume, "C1 should have 0 remaining");
        assertEquals(2, c1Result.matches.size(), "C1 should have two matches");
        assertTrue(c1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("A1") && m.volume == 40), "C1 should match 40 with A1");
        assertTrue(c1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("B1") && m.volume == 160), "C1 should match 160 with B1");
    }

    @Test
    void testProRataDistributionCalculation() {
        // Test pro-rata distribution: orders matched proportionally
        // Total buy volume = 300, Sell volume = 150
        // A1 (100/300) * 150 = 50
        // B1 (200/300) * 150 = 100
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 200, 5.00, "09:01:00"),
            new Order("C", "C1", Direction.SELL, 150, 5.00, "10:00:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        OrderResult a1Result = results.get("A1");
        OrderResult b1Result = results.get("B1");

        // Verify pro-rata allocation
        assertEquals(50, a1Result.matches.get(0).volume,
            "A1 should get 50 (1/3 of 150) based on pro-rata");
        assertEquals(100, b1Result.matches.get(0).volume,
            "B1 should get 100 (2/3 of 150) based on pro-rata");
    }

    @Test
    void testProRataWithDifferentPrices() {
        // Only orders at the same price should match
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.50, "09:01:00"), // Different price
            new Order("C", "C1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        OrderResult c1Result = results.get("C1");
        assertEquals(1, c1Result.matches.size(),
            "C1 should only match with A1 at same price");
        assertEquals("A1", c1Result.matches.get(0).orderId);
        assertEquals(100, c1Result.matches.get(0).volume);
    }

    @Test
    void testNoEligibleBuys() {
        // Sell order with no matching buys at same price
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 4.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.NO_MATCH, results.get("B1").order.getMatchState(),
            "B1 should not match - no eligible buys at price 5.00");
        assertEquals(100, results.get("B1").order.remainingVolume,
            "B1 should retain full volume");
    }

    @Test
    void testMultipleSellsAtSamePrice() {
        // Multiple sell orders - each matched independently
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 300, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 100, 5.00, "10:00:00"),
            new Order("C", "C1", Direction.SELL, 100, 5.00, "10:01:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        // Each sell should fully match since there's enough buy volume
        assertEquals(MatchState.FULL_MATCH, results.get("B1").order.getMatchState());
        assertEquals(MatchState.FULL_MATCH, results.get("C1").order.getMatchState());
    }

    @Test
    void testEmptyOrderBook() {
        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(Arrays.asList());

        assertTrue(results.isEmpty(), "Empty order book should produce empty results");
    }

    @Test
    void testOnlyBuyOrders() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.50, "09:01:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(2, results.size());
        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState());
        assertEquals(MatchState.NO_MATCH, results.get("B1").order.getMatchState());
    }

    @Test
    void testOnlySellOrders() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.SELL, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 100, 5.50, "09:01:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(2, results.size());
        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState());
        assertEquals(MatchState.NO_MATCH, results.get("B1").order.getMatchState());
    }

    @Test
    void testVolumeConservation() {
        // Total matched volume should equal total sell volume (at same price)
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.00, "09:01:00"),
            new Order("C", "C1", Direction.SELL, 120, 5.00, "10:00:00")
        );

        ProRataStrategy strategy = new ProRataStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        int totalMatched = results.get("C1").matches.stream()
            .mapToInt(m -> m.volume).sum();

        assertEquals(120, totalMatched, "Total matched volume should equal sell volume");
    }
}
