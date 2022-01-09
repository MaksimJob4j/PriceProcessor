package com.price.processor;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriceThrottler implements PriceProcessor {

    private final ConcurrentHashMap<PriceProcessor, PriceProcessorHandler> updateMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, ImmutablePair<Double, Date>> lastPrice = new ConcurrentHashMap<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void onPrice(final String ccyPair, final double rate) {
        lastPrice.put(ccyPair, new ImmutablePair<>(rate, new Date()));
        updateMap.forEach((k, v) -> executorService.execute(() -> v.handle(ccyPair)));
    }

    public void subscribe(PriceProcessor priceProcessor) {
        updateMap.putIfAbsent(priceProcessor, new PriceProcessorHandler(priceProcessor, lastPrice));
    }

    public void unsubscribe(PriceProcessor priceProcessor) {
        updateMap.remove(priceProcessor);
    }
}
