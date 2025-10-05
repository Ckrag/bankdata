package dk.ckrag.account.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
@Introspected
public class TransferRequest {
    private final Long fromAccountId;
    private final Long toAccountId;
    private final Integer amount;

    public TransferRequest(Long fromAccountId, Long toAccountId, Integer amount) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
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

}
