package ru.oval.accounting.model;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Ticker {
    public final String ticker;
    public final String fullName;
    public final Currency accountingCurrency;
}
