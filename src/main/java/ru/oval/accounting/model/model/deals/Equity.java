package ru.oval.accounting.model.model.deals;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.oval.accounting.model.model.Account;
import ru.oval.accounting.model.model.Side;
import ru.oval.accounting.model.model.Ticker;

import java.math.BigDecimal;
import java.time.LocalDate;

@ToString
@EqualsAndHashCode(callSuper = true)
public class Equity extends Deal {
    public final Side side;
    public final Ticker ticker;
    public final BigDecimal price;

    @Builder
    private Equity(final Account accountFrom,
                   final Account accountTo,
                   final BigDecimal notional,
                   final LocalDate dealDate,
                   final Side side,
                   final Ticker ticker,
                   final BigDecimal price) {
        super(accountFrom, accountTo, notional, dealDate);
        this.side = side;
        this.ticker = ticker;
        this.price = price;
    }
}
