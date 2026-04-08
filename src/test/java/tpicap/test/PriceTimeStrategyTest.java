package tpicap.test;

import org.junit.jupiter.api.Test;
import tpicap.domain.Direction;
import tpicap.domain.MatchState;
import tpicap.domain.Order;
import tpicap.domain.OrderResult;
import tpicap.strategy.PriceTimeStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for User Story One: Price-Time-Priority matching algorithm
 */
public class PriceTimeStrategyTest {

    @Test
    void testUserStoryOneAcceptanceCriteria() {
        // Given: Order Book from User Story One
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 4.99, "09:27:43"),
            new Order("B", "B1", Direction.BUY, 200, 5.00, "10:21:46"),
            new Order("C", "C1", Direction.BUY, 150, 5.00, "10:26:18"),
            new Order("D", "D1", Direction.SELL, 150, 5.00, "10:32:41"),
            new Order("E", "E1", Direction.SELL, 100, 5.00, "10:33:07")
        );

        // When: Matching with Price-Time-Priority strategy
        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        // Then: Verify all orders have results
        assertEquals(5, results.size(), "All orders should have results");
        assertNotNull(results.get("A1"), "A1 should have a result");
        assertNotNull(results.get("B1"), "B1 should have a result");
        assertNotNull(results.get("C1"), "C1 should have a result");
        assertNotNull(results.get("D1"), "D1 should have a result");
        assertNotNull(results.get("E1"), "E1 should have a result");

        // A1 - NoMatch: the higher priced orders took priority
        OrderResult a1Result = results.get("A1");
        assertEquals(MatchState.NO_MATCH, a1Result.order.getMatchState(),
            "A1 should have NO_MATCH - lower price orders don't get priority");
        assertTrue(a1Result.matches.isEmpty(), "A1 should have no matches");
        assertEquals(100, a1Result.order.remainingVolume, "A1 should have full remaining volume");

        // B1 - FullMatch
        OrderResult b1Result = results.get("B1");
        assertEquals(MatchState.FULL_MATCH, b1Result.order.getMatchState(),
            "B1 should have FULL_MATCH");
        assertEquals(0, b1Result.order.remainingVolume, "B1 should have 0 remaining volume");
        assertEquals(2, b1Result.matches.size(), "B1 should match with D1 and E1");

        // Verify B1 matches: D1 - 150, E1 - 50
        assertTrue(b1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("D1") && m.volume == 150), "B1 should match 150 with D1");
        assertTrue(b1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("E1") && m.volume == 50), "B1 should match 50 with E1");

        // C1 - PartialMatch
        OrderResult c1Result = results.get("C1");
        assertEquals(MatchState.PARTIAL_MATCH, c1Result.order.getMatchState(),
            "C1 should have PARTIAL_MATCH");
        assertEquals(100, c1Result.order.remainingVolume, "C1 should have 100 remaining volume (150-50)");
        assertEquals(1, c1Result.matches.size(), "C1 should have one match");

        // Verify C1 matches: E1 - 50
        assertTrue(c1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("E1") && m.volume == 50), "C1 should match 50 with E1");

        // D1 - FullMatch
        OrderResult d1Result = results.get("D1");
        assertEquals(MatchState.FULL_MATCH, d1Result.order.getMatchState(),
            "D1 should have FULL_MATCH");
        assertEquals(0, d1Result.order.remainingVolume, "D1 should have 0 remaining volume");
        assertEquals(1, d1Result.matches.size(), "D1 should have one match");

        // Verify D1 matches: B1 - 150
        assertTrue(d1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("B1") && m.volume == 150), "D1 should match 150 with B1");

        // E1 - FullMatch
        OrderResult e1Result = results.get("E1");
        assertEquals(MatchState.FULL_MATCH, e1Result.order.getMatchState(),
            "E1 should have FULL_MATCH");
        assertEquals(0, e1Result.order.remainingVolume, "E1 should have 0 remaining volume");
        assertEquals(2, e1Result.matches.size(), "E1 should match with B1 and C1");

        // Verify E1 matches: B1 - 50, C1 - 50
        assertTrue(e1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("B1") && m.volume == 50), "E1 should match 50 with B1");
        assertTrue(e1Result.matches.stream().anyMatch(m ->
            m.orderId.equals("C1") && m.volume == 50), "E1 should match 50 with C1");
    }

    @Test
    void testPriceTimePriorityOrdering() {
        // Test that earlier orders at same price get priority
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.00, "08:59:00"), // Earlier
            new Order("C", "C1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        // B1 should match first (earlier timestamp)
        OrderResult c1Result = results.get("C1");
        assertEquals(1, c1Result.matches.size(), "C1 should have one match");
        assertEquals("B1", c1Result.matches.get(0).orderId,
            "Earlier order B1 should take priority over A1");
    }

    @Test
    void testHigherPriceTakesPriority() {
        // Test that higher buy price matches first
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.50, "09:01:00"), // Higher price
            new Order("C", "C1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        // B1 should match first (higher price)
        OrderResult c1Result = results.get("C1");
        assertEquals(1, c1Result.matches.size(), "C1 should have one match");
        assertEquals("B1", c1Result.matches.get(0).orderId,
            "Higher priced order B1 should take priority");
    }

    @Test
    void testNoMatchWhenPriceDoesNotMeet() {
        // Buy price lower than sell price - no match
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 4.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 100, 5.00, "10:00:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState(),
            "A1 should not match - buy price too low");
        assertEquals(MatchState.NO_MATCH, results.get("B1").order.getMatchState(),
            "B1 should not match - sell price too high");
    }

    @Test
    void testPartialFillScenario() {
        // Large buy order matches multiple smaller sells
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 500, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.SELL, 200, 5.00, "10:00:00"),
            new Order("C", "C1", Direction.SELL, 200, 5.00, "10:01:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        // A1 should be partially filled (matched 400, remaining 100)
        OrderResult a1Result = results.get("A1");
        assertEquals(MatchState.PARTIAL_MATCH, a1Result.order.getMatchState(),
            "A1 should be partially matched");
        assertEquals(100, a1Result.order.remainingVolume,
            "A1 should have 100 remaining after matching 400");
        assertEquals(2, a1Result.matches.size(), "A1 should match with both sells");
    }

    @Test
    void testEmptyOrderBook() {
        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(Arrays.asList());

        assertTrue(results.isEmpty(), "Empty order book should produce empty results");
    }

    @Test
    void testOnlyBuyOrders() {
        List<Order> orders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 5.00, "09:00:00"),
            new Order("B", "B1", Direction.BUY, 100, 5.50, "09:01:00")
        );

        PriceTimeStrategy strategy = new PriceTimeStrategy();
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

        PriceTimeStrategy strategy = new PriceTimeStrategy();
        Map<String, OrderResult> results = strategy.match(orders);

        assertEquals(2, results.size());
        assertEquals(MatchState.NO_MATCH, results.get("A1").order.getMatchState());
        assertEquals(MatchState.NO_MATCH, results.get("B1").order.getMatchState());
    }
}
