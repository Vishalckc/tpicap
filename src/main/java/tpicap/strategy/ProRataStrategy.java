package tpicap.strategy;

import tpicap.domain.Direction;
import tpicap.domain.Order;
import tpicap.domain.OrderResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User Story Two: Pro-Rata
 */
public class ProRataStrategy implements MatchingStrategy {
    @Override
    public Map<String, OrderResult> match(List<Order> orders) {
        Map<String, OrderResult> results = orders.stream()
                .collect(Collectors.toMap(o -> o.orderId, OrderResult::new, (a, b) -> a, LinkedHashMap::new));

        // Simplified for the scenario: Group by price and match
        List<Order> buys = orders.stream().filter(o -> o.direction == Direction.BUY).collect(Collectors.toList());
        List<Order> sells = orders.stream().filter(o -> o.direction == Direction.SELL).collect(Collectors.toList());

        for (Order sell : sells) {
            List<Order> eligibleBuys = buys.stream()
                    .filter(b -> b.price == sell.price && b.remainingVolume > 0)
                    .collect(Collectors.toList());

            int totalBuyVol = eligibleBuys.stream().mapToInt(b -> b.totalVolume).sum();
            int sellVolToDistribute = sell.remainingVolume;

            for (Order buy : eligibleBuys) {
                // Calculation: (Order Vol / Total Vol) * Sell Vol
                int matchVol = (int) Math.floor(((double) buy.totalVolume / totalBuyVol) * sellVolToDistribute);

                buy.remainingVolume -= matchVol;
                sell.remainingVolume -= matchVol;

                results.get(buy.orderId).addMatch(sell.orderId, matchVol, sell.price);
                results.get(sell.orderId).addMatch(buy.orderId, matchVol, sell.price);
            }
        }
        return results;
    }
}
