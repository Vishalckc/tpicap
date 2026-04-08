package tpicap.main;

import tpicap.domain.Direction;
import tpicap.domain.Order;
import tpicap.strategy.PriceTimeStrategy;
import tpicap.strategy.ProRataStrategy;

import java.util.Arrays;
import java.util.List;

public class MatchingEngineApp {
    public static void main(String[] args) {
        System.out.println("--- User Story One: Price-Time-Priority ---");
        List<Order> storyOneOrders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 100, 4.99, "09:27:43"),
            new Order("B", "B1", Direction.BUY, 200, 5.00, "10:21:46"),
            new Order("C", "C1", Direction.BUY, 150, 5.00, "10:26:18"),
            new Order("D", "D1", Direction.SELL, 150, 5.00, "10:32:41"),
            new Order("E", "E1", Direction.SELL, 100, 5.00, "10:33:07")
        );
        new PriceTimeStrategy().match(storyOneOrders).values().forEach(System.out::println);

        System.out.println("\n--- User Story Two: Pro-Rata ---");
        List<Order> storyTwoOrders = Arrays.asList(
            new Order("A", "A1", Direction.BUY, 50, 5.00, "09:27:43"),
            new Order("B", "B1", Direction.BUY, 200, 5.00, "10:21:46"),
            new Order("C", "C1", Direction.SELL, 200, 5.00, "10:26:18")
        );
        new ProRataStrategy().match(storyTwoOrders).values().forEach(System.out::println);
    }
}