package ru.oval.accounting.model.model;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

import static java.util.Optional.empty;

@RequiredArgsConstructor
@ToString
public enum Currency {
    RUB("RUB"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    // crypto
    BTC("BTC"),
    ETH("ETH"),
    XRP("XRP"),

    ;
    public final String name;

    private static final Currency[] VALUES = values();

    public static Optional<Currency> find(final String name) {
        for (final Currency value : VALUES) {
            if (value.name.equals(name)) {
                return Optional.of(value);
            }
        }
        return empty();
    }
}
