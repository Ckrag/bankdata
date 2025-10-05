package dk.ckrag.account.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.time.OffsetDateTime;

@Serdeable
@Introspected
public class TransactionResponse {
    private final Long id;
    private final Long fromAccountId;
    private final Long toAccountId;
    private final Integer amount;
    private final OffsetDateTime createdAt;

    public TransactionResponse(Long id, Long fromAccountId, Long toAccountId, Integer amount, OffsetDateTime createdAt) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Integer getAmount() {
        return amount;
    }

    public static TransactionResponse fromDto(dk.ckrag.account.dto.TransactionDto dto) {
        return new TransactionResponse(dto.getId(), dto.getFromAccountId(), dto.getToAccountId(), dto.getAmount(), dto.getCreatedAt());
    }
}
