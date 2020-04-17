package ru.oval.accounting.model.repository;

import ru.oval.accounting.model.model.Account;

import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;

public class AccountRepository {

    private static final Set<Account> accounts = Set.of(new Account("alfa-cu"),
            new Account("yango"),
            new Account("alfa-forts"),
            new Account("alfa"),
            new Account("tinkoff"),
            new Account("external"));

    public static Set<Account> findAll(final int userId) {
        return accounts;
    }

    public static Optional<Account> findByName(final int userId, final String accName) {
        final Account acc = new Account(accName);
        if (accounts.contains(acc)) {
            return Optional.of(acc);
        }
        return empty();
    }
}
