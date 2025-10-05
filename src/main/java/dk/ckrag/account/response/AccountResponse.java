package dk.ckrag.account.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public class AccountResponse {
    private Long id;
    private Integer amount;
    private Long userId;

    public AccountResponse(Long id, Integer amount, Long userId) {
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

    public static AccountResponse fromDto(dk.ckrag.account.dto.AccountDto dto) {
        return new AccountResponse(dto.getId(), dto.getAmount(), dto.getUserId());
    }
}
