package tpicap.strategy;

import tpicap.domain.Direction;
import tpicap.domain.Order;
import tpicap.domain.OrderResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * User Story One: Price-Time-Priority
 */
public class PriceTimeStrategy implements MatchingStrategy {
    @Override
    public Map<String, OrderResult> match(List<Order> orders) {
        Map<String, OrderResult> results = orders.stream()
                .collect(Collectors.toMap(o -> o.orderId, OrderResult::new, (a, b) -> a, LinkedHashMap::new));

        List<Order> buyBook = new ArrayList<>();
        List<Order> sellBook = new ArrayList<>();

        for (Order order : orders) {
            if (order.direction == Direction.BUY) {
                matchOrder(order, sellBook, results, true);
                if (order.remainingVolume > 0) buyBook.add(order);
            } else {
                matchOrder(order, buyBook, results, false);
                if (order.remainingVolume > 0) sellBook.add(order);
            }
        }
        return results;
    }

    private void matchOrder(Order incoming, List<Order> book, Map<String, OrderResult> results, boolean isIncomingBuy) {
        // Sort book: For Buy, highest price first. For Sell, lowest price first. Then by time.
        book.sort((o1, o2) -> {
            if (o1.price != o2.price) {
                return isIncomingBuy ? Double.compare(o1.price, o2.price) : Double.compare(o2.price, o1.price);
            }
            return o1.timestamp.compareTo(o2.timestamp);
        });

        Iterator<Order> it = book.iterator();
        while (it.hasNext() && incoming.remainingVolume > 0) {
            Order resting = it.next();
            // Check if price matches (Buy price >= Sell price)
            if ((isIncomingBuy && incoming.price >= resting.price) || (!isIncomingBuy && incoming.price <= resting.price)) {
                int matchVol = Math.min(incoming.remainingVolume, resting.remainingVolume);

                incoming.remainingVolume -= matchVol;
                resting.remainingVolume -= matchVol;

                results.get(incoming.orderId).addMatch(resting.orderId, matchVol, resting.price);
                results.get(resting.orderId).addMatch(incoming.orderId, matchVol, resting.price);

                if (resting.remainingVolume == 0) it.remove();
            }
        }
    }
}
