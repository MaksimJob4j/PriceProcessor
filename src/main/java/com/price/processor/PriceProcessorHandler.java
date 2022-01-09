package com.price.processor;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class PriceProcessorHandler {

    private final PriceProcessor priceProcessor;
    private final ConcurrentHashMap<String, ImmutablePair<Double, Date>> lastPrice;

    private final ConcurrentHashMap<String, Object> blocker = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Date> lastUpdateDate = new ConcurrentHashMap<>();

    public PriceProcessorHandler(PriceProcessor priceProcessor, ConcurrentHashMap<String, ImmutablePair<Double, Date>> lastPrice) {
        this.priceProcessor = priceProcessor;
        this.lastPrice = lastPrice;
    }

    public void handle(String ccyPair) {
        blocker.putIfAbsent(ccyPair, new Object());
        synchronized (blocker.get(ccyPair)) {
            ImmutablePair<Double, Date> price = lastPrice.get(ccyPair);
            if (price != null) {
                if (lastUpdateDate.get(ccyPair) == null || price.getRight().after(lastUpdateDate.get(ccyPair))) {
                    priceProcessor.onPrice(ccyPair, price.left);
                    lastUpdateDate.put(ccyPair, price.right);
                }
            }
        }
    }
}
