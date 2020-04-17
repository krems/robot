package ru.oval.accounting.model.model;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@RequiredArgsConstructor
@ToString
public class Account {
    public final String name;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Account account = (Account) o;
        return name.equalsIgnoreCase(account.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
