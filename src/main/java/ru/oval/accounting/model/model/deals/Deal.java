package ru.oval.accounting.model.model.deals;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.oval.accounting.model.model.Account;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class Deal {
   public int id = 1;//fixme
   public final Account accountFrom;
   public final Account accountTo;
   public final BigDecimal notional;
   public final LocalDate dealDate;
}
