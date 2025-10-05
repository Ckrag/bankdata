package dk.ckrag.account.request;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.validation.Validated;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;

@Introspected
@Validated
@Serdeable
public class CreateAccountRequest {
    @Min(0)
    private final Integer initialAmount;

    public CreateAccountRequest(Integer initialAmount) {
        this.initialAmount = initialAmount;
    }

    public Integer getInitialAmount() {
        return initialAmount;
    }
}
