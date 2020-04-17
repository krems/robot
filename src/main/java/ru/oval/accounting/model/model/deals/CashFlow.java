package ru.oval.accounting.model.model.deals;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.oval.accounting.model.model.Account;
import ru.oval.accounting.model.model.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;

@ToString
@EqualsAndHashCode(callSuper = true)
public class CashFlow extends Deal {
    public final Currency currency;

    @Builder
    private CashFlow(final Account accountFrom,
                     final Account accountTo,
                     final BigDecimal notional,
                     final LocalDate dealDate,
                     final Currency currency) {
        super(accountFrom, accountTo, notional, dealDate);
        this.currency = currency;
    }
}

