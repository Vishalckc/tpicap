package tpicap.domain;

import java.util.ArrayList;
import java.util.List;

public class OrderResult {
    public Order order;
    public List<MatchRecord> matches = new ArrayList<>();

    public OrderResult(Order order) {
        this.order = order;
    }

    public void addMatch(String otherId, int volume, double price) {
        matches.add(new MatchRecord(otherId, volume, price));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s - %s\n", order.orderId, order.getMatchState()));
        for (MatchRecord m : matches) {
            sb.append(String.format("  -> %s - %d @ %.2f\n", m.orderId, m.volume, m.price));
        }
        return sb.toString();
    }
}
