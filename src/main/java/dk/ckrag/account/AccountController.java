package dk.ckrag.account;

import dk.ckrag.account.dto.UserDto;
import dk.ckrag.account.request.CreateAccountRequest;
import dk.ckrag.account.response.AccountListResponse;
import dk.ckrag.account.response.AccountResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@Controller("/banking/account")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class AccountController {

    @Inject
    private AccountService accountService;

    @Inject
    private UserService userService;

    @Post
    public AccountResponse createAccount(Authentication authentication, @Body CreateAccountRequest request) {
        UserDto user = userService.getAuthorizedUser(authentication);
        return AccountResponse.fromDto(accountService.createAccount(user, request.getInitialAmount()));
    }

    @Get
    public AccountListResponse listAccounts(Authentication authentication) {
        UserDto user = userService.getAuthorizedUser(authentication);
        List<AccountResponse> accounts = accountService.listAccounts(user)
                .stream()
                .map(AccountResponse::fromDto)
                .collect(Collectors.toList());
        return new AccountListResponse(accounts);
    }

}
