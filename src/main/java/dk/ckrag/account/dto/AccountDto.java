package dk.ckrag.account.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class AccountDto {
    private final Long id;
    private final Integer amount;
    private final Long userId;

    public AccountDto(Long id, Integer amount, Long userId) {
        this.id = id;
        this.amount = amount;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public Integer getAmount() {
        return amount;
    }

    public Long getUserId() {
        return userId;
    }
}