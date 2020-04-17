package ru.oval.accounting.model.repository;

import ru.oval.accounting.model.model.deals.Deal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DealRepository {
    private static final AtomicLong seq = new AtomicLong();
    private static final Map<Long, Deal> deals = new ConcurrentHashMap<>();

    public static boolean save(final int userId, final Deal deal) {
        long id = seq.incrementAndGet();
        return deals.put(id, deal) != deal;
    }


}
