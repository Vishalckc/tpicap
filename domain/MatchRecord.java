package tpicap.domain;

public class MatchRecord {
    public String orderId;
    public int volume;
    public double price;

    public MatchRecord(String orderId, int volume, double price) {
        this.orderId = orderId;
        this.volume = volume;
        this.price = price;
    }
}
