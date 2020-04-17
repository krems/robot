package ru.oval.accounting.model.repository;

import ru.oval.accounting.model.model.Account;

import java.util.Optional;
import java.util.Set;

import static java.util.Optional.empty;

public class AccountRepository {

    private static Set<Account> accounts;

    public static Set<Account> findAll(final int userId) {
        accounts = Set.of(new Account("Alfa-CU"),
                new Account("Yango"),
                new Account("Alfa-FORTS"),
                new Account("Alfa-SE"),
                new Account("Tinkoff"),
                new Account("External"));
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
