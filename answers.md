# Technical Questions - TP ICAP Assessment

## 1. How much time did you spend on the engineering task?
I spent approximately ** 3.5 hours** on this task. This included:
* Initial design and domain modeling (30 mins)
* Implementing the matching strategies and logic (1 hour)
* Writing the console application and unit tests (45 mins)
* Refactoring and documentation (15 mins)

---

## 2. What would you add to your solution if you’d had more time?
If this were a real-world production system, I would implement the following:

* **Financial Precision:** Replace `double` with `BigDecimal` for price calculations to prevent floating-point arithmetic errors common in financial applications.
* **Rounding Logic:** Implement a specific rounding strategy (e.g., Banker's Rounding) for the Pro-Rata algorithm to handle fractional shares/volume when remainders occur.
* **Performance Optimization:** Use specialized collections like `PriorityQueue` for the order books to ensure $O(\log n)$ insertion and $O(1)$ access to the best bid/offer.
* **Validation Layer:** Add a robust validation service to flag `InvalidOrder` states (e.g., non-positive volumes, prices, or missing IDs).
* **Logging & Auditing:** Integrate a logging framework (like SLF4J/Logback) to create an audit trail of every match and order modification.

---

## 3. What do you think is the most useful feature added to the latest version of the language?
The most impactful recent addition to Java (specifically Java 21) is **Virtual Threads (Project Loom)**.

In high-throughput financial systems, traditional platform threads are expensive and limited in number. Virtual threads allow the application to handle millions of concurrent tasks (like thousands of simultaneous order streams) without the memory overhead of OS threads.

### Code Snippet:
Using a Virtual Thread executor to handle incoming order requests asynchronously:

```java
// Java 21+ syntax
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    incomingOrders.forEach(order -> {
        executor.submit(() -> {
            matchingEngine.process(order);
            System.out.println("Processed order " + order.getId() + " on " + Thread.currentThread());
        });
    });
}