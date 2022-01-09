package com.price.processor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class PriceThrottlerTest {

    PriceThrottler priceThrottler;
    AtomicInteger count;

    @Before
    public void init() {
        priceThrottler = new PriceThrottler();
        count = new AtomicInteger();
        for (int i = 0; i < 200; i++) {
            int j = i;
            priceThrottler.subscribe(getPriceProcessor((s, d) -> {
                System.out.printf("Processor%s: %s - %s%n", j, s, d);
                count.incrementAndGet();
                try {
                    Thread.sleep(j);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        }
    }

    @Test
    public void onPriceTest100PricesChanges() {
        Random r = new Random();

        for (int i = 0; i < 100; i++) {
            priceThrottler.onPrice("pair-" + r.nextInt(3), r.nextDouble());
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(count.get());
        Assert.assertTrue(100 * 200 > count.get());

    }

    private PriceProcessor getPriceProcessor(BiConsumer<String, Double> onPrice) {
        return new PriceProcessor() {
            @Override
            public void onPrice(String ccyPair, double rate) {
                onPrice.accept(ccyPair, rate);
            }

            @Override
            public void subscribe(PriceProcessor priceProcessor) {

            }

            @Override
            public void unsubscribe(PriceProcessor priceProcessor) {

            }
        };
    }
}