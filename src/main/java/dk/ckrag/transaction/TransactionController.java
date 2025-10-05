package dk.ckrag.transaction;

import dk.ckrag.account.UserService;
import dk.ckrag.account.dto.UserDto;
import dk.ckrag.account.request.TransferRequest;
import dk.ckrag.account.response.TransactionResponse;
import dk.ckrag.transaction.response.TransactionAuditResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@Controller("/banking/transaction")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class TransactionController {

    @Inject
    private TransactionService transactionService;

    @Inject
    private UserService userService;

    @Post("/transfer")
    public TransactionResponse transfer(Authentication authentication, @Body TransferRequest request) {
        UserDto user = userService.getAuthorizedUser(authentication);
        return TransactionResponse.fromDto(transactionService.transfer(user, request));
    }

    @Get("/audit/{userId}")
    public TransactionAuditResponse audit(Authentication authentication, @PathVariable Long userId) {
        UserDto user = userService.getAuthorizedUser(authentication);
        List<TransactionResponse> transactions = transactionService.audit(user, userId)
                .stream()
                .map(TransactionResponse::fromDto)
                .collect(Collectors.toList());
        return new TransactionAuditResponse(transactions);
    }
}
