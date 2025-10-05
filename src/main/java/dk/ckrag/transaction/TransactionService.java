package dk.ckrag.transaction;

import dk.ckrag.account.AccountRepository;
import dk.ckrag.account.AccountService;
import dk.ckrag.account.dto.TransactionDto;
import dk.ckrag.account.dto.UserDto;
import dk.ckrag.account.request.TransferRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class TransactionService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;

    public TransactionService(AccountService accountService, AccountRepository accountRepository) {
        this.accountService = accountService;
        this.accountRepository = accountRepository;
    }

    public TransactionDto transfer(UserDto authenticatedUser, TransferRequest request) {
        return accountService.transfer(authenticatedUser, request);
    }

    public List<TransactionDto> audit(UserDto authenticatedUser, Long userId) {
        // Authorization: user can only audit their own transactions
        if (!authenticatedUser.getId().equals(userId)) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "You are not authorized to audit transactions for user: " + userId);
        }

        return accountRepository.findAllTransactionsByUserId(userId);
    }
}
