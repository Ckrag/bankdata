package dk.ckrag.account.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class AccountDto {
    private Long id;
    private Integer amount;
    private Long userId;

    public AccountDto(Long id, Integer amount, Long userId) {
        this.id = id;
        this.amount = amount;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}