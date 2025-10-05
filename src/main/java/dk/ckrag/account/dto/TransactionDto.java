package dk.ckrag.account.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import java.time.OffsetDateTime;

@Serdeable
@Introspected
public class TransactionDto {
    private final Long id;
    private final Long fromAccountId;
    private final Long toAccountId;
    private final Integer amount;
    private final OffsetDateTime createdAt;

    public TransactionDto(Long id, Long fromAccountId, Long toAccountId, Integer amount, OffsetDateTime createdAt) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public Integer getAmount() {
        return amount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

}