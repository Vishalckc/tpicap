package tpicap.strategy;

import tpicap.domain.Order;
import tpicap.domain.OrderResult;

import java.util.*;

public interface MatchingStrategy {
    Map<String, OrderResult> match(List<Order> orders);
}

