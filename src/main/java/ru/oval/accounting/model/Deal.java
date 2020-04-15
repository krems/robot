package ru.oval.accounting.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Deal {
   public final Side side;
   public final Ticker ticker;
   public final BigDecimal notional;
   public final BigDecimal price;
   public final LocalDate dealDate;
   public final Currency currency;
   public final BigDecimal accNotional;
}
