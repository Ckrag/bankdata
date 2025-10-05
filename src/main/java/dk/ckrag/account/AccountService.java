package dk.ckrag.account;

import dk.ckrag.account.dto.AccountDto;
import dk.ckrag.account.dto.TransactionDto;
import dk.ckrag.account.dto.UserDto;
import dk.ckrag.account.request.TransferRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountDto createAccount(UserDto user, Integer initialAmount) {
        return accountRepository.createAccount(user.getId(), initialAmount);
    }

    public List<AccountDto> listAccounts(UserDto user) {
        return accountRepository.findAllByUserId(user.getId());
    }

    public TransactionDto transfer(UserDto authenticatedUser, TransferRequest request) {
        // Basic validation: ensure accounts exist and amount is positive
        if (request.getAmount() <= 0) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Transfer amount must be positive");
        }

        AccountDto fromAccount = accountRepository.findByAccountId(request.getFromAccountId());
        AccountDto toAccount = accountRepository.findByAccountId(request.getToAccountId());

        if (fromAccount == null) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Source account not found: " + request.getFromAccountId());
        }
        if (toAccount == null) {
            throw new HttpStatusException(HttpStatus.NOT_FOUND, "Destination account not found: " + request.getToAccountId());
        }

        // Security check: ensure authenticated user owns the source account
        if (!fromAccount.getUserId().equals(authenticatedUser.getId())) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "You are not authorized to transfer from account: " + request.getFromAccountId());
        }

        if (fromAccount.getAmount() < request.getAmount()) {
            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds in source account: " + request.getFromAccountId());
        }

        return accountRepository.transferBetween(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
    }
}
