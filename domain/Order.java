package tpicap.domain;

import java.time.LocalTime;

public class Order {
    public String companyId;
    public String orderId;
    public Direction direction;
    public int totalVolume;
    public int remainingVolume;
    public double price;
    public LocalTime timestamp;

    public Order(String companyId, String orderId, Direction direction, int volume, double price, String time) {
        this.companyId = companyId;
        this.orderId = orderId;
        this.direction = direction;
        this.totalVolume = volume;
        this.remainingVolume = volume;
        this.price = price;
        this.timestamp = LocalTime.parse(time);
    }

    public MatchState getMatchState() {
        if (remainingVolume == totalVolume) return MatchState.NO_MATCH;
        if (remainingVolume == 0) return MatchState.FULL_MATCH;
        return MatchState.PARTIAL_MATCH;
    }
}
