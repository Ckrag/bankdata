package dk.ckrag.account.response;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
@Introspected
public class AccountListResponse {
    private final List<AccountResponse> accounts;

    public AccountListResponse(List<AccountResponse> accounts) {
        this.accounts = accounts;
    }

    public List<AccountResponse> getAccounts() {
        return accounts;
    }
}
