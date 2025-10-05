package dk.ckrag.account.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public class AccountResponse {
    private final Long id;
    private final Integer amount;
    private final Long userId;

    public AccountResponse(Long id, Integer amount, Long userId) {
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

    public static AccountResponse fromDto(dk.ckrag.account.dto.AccountDto dto) {
        return new AccountResponse(dto.getId(), dto.getAmount(), dto.getUserId());
    }
}
