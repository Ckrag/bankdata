package dk.ckrag.account;

import dk.ckrag.account.dto.AccountDto;
import dk.ckrag.account.dto.TransactionDto;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

import static dk.ckrag.jooq.tables.Account.ACCOUNT;
import static dk.ckrag.jooq.tables.Transaction.TRANSACTION;

@Singleton
public class AccountRepository {

    private final DSLContext dslContext;

    public AccountRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<AccountDto> findAllByUserId(Long userId) {
        return dslContext.select(ACCOUNT.ID, ACCOUNT.AMOUNT, ACCOUNT.USER_ID)
                .from(ACCOUNT)
                .where(ACCOUNT.USER_ID.eq(userId))
                .fetch()
                .map(record -> new AccountDto(record.get(ACCOUNT.ID), record.get(ACCOUNT.AMOUNT), record.get(ACCOUNT.USER_ID)));
    }

    public AccountDto findByAccountId(Long accountId) {
        return dslContext.select(ACCOUNT.ID, ACCOUNT.AMOUNT, ACCOUNT.USER_ID)
                .from(ACCOUNT)
                .where(ACCOUNT.ID.eq(accountId))
                .fetchOptional()
                .map(record -> new AccountDto(record.get(ACCOUNT.ID), record.get(ACCOUNT.AMOUNT), record.get(ACCOUNT.USER_ID)))
                .orElse(null);
    }

    public TransactionDto transferBetween(Long fromAccountId, Long toAccountId, Integer amount) {
        return dslContext.transactionResult(configuration -> {
            DSLContext trxContext = configuration.dsl();

            // Record transaction first
            var transactionId = trxContext.insertInto(TRANSACTION)
                    .columns(TRANSACTION.FROM_ACCOUNT_ID, TRANSACTION.TO_ACCOUNT_ID, TRANSACTION.AMOUNT)
                    .values(fromAccountId, toAccountId, amount)
                    .returning(TRANSACTION.ID)
                    .fetchOne()
                    .getId();

            // Decrement fromAccountId
            trxContext.update(ACCOUNT)
                    .set(ACCOUNT.AMOUNT, ACCOUNT.AMOUNT.minus(amount))
                    .where(ACCOUNT.ID.eq(fromAccountId))
                    .execute();

            // Increment toAccountId
            trxContext.update(ACCOUNT)
                    .set(ACCOUNT.AMOUNT, ACCOUNT.AMOUNT.plus(amount))
                    .where(ACCOUNT.ID.eq(toAccountId))
                    .execute();

            return trxContext.select(
                    TRANSACTION.ID,
                    TRANSACTION.FROM_ACCOUNT_ID,
                    TRANSACTION.TO_ACCOUNT_ID,
                    TRANSACTION.AMOUNT,
                    TRANSACTION.CREATED_AT
                )
                .from(TRANSACTION)
                .where(TRANSACTION.ID.eq(transactionId))
                .fetchOne()
                .map(record -> new TransactionDto(
                    record.get(TRANSACTION.ID),
                    record.get(TRANSACTION.FROM_ACCOUNT_ID),
                    record.get(TRANSACTION.TO_ACCOUNT_ID),
                    record.get(TRANSACTION.AMOUNT),
                    record.get(TRANSACTION.CREATED_AT)
                ));
        });
    }

    public AccountDto createAccount(Long userId, Integer initialAmount) {
        Long accountId = dslContext.insertInto(ACCOUNT)
                .columns(ACCOUNT.AMOUNT, ACCOUNT.USER_ID)
                .values(initialAmount, userId)
                .returning(ACCOUNT.ID)
                .fetchOne()
                .getId();
        return findByAccountId(accountId);
    }

    public TransactionDto getTransactionById(Long transactionId) {
        return dslContext.select(
                TRANSACTION.ID,
                TRANSACTION.FROM_ACCOUNT_ID,
                TRANSACTION.TO_ACCOUNT_ID,
                TRANSACTION.AMOUNT,
                TRANSACTION.CREATED_AT
            )
            .from(TRANSACTION)
            .where(TRANSACTION.ID.eq(transactionId))
            .fetchOptional()
            .map(record -> new TransactionDto(
                record.get(TRANSACTION.ID),
                record.get(TRANSACTION.FROM_ACCOUNT_ID),
                record.get(TRANSACTION.TO_ACCOUNT_ID),
                record.get(TRANSACTION.AMOUNT),
                record.get(TRANSACTION.CREATED_AT)
            ))
            .orElse(null);
    }

    public List<TransactionDto> findAllTransactionsByUserId(Long userId) {
        return dslContext.selectDistinct(
                TRANSACTION.ID,
                TRANSACTION.FROM_ACCOUNT_ID,
                TRANSACTION.TO_ACCOUNT_ID,
                TRANSACTION.AMOUNT,
                TRANSACTION.CREATED_AT
            )
            .from(TRANSACTION)
            .join(ACCOUNT).on(TRANSACTION.FROM_ACCOUNT_ID.eq(ACCOUNT.ID)
                .or(TRANSACTION.TO_ACCOUNT_ID.eq(ACCOUNT.ID)))
            .where(ACCOUNT.USER_ID.eq(userId))
            .fetch()
            .map(record -> new TransactionDto(
                record.get(TRANSACTION.ID),
                record.get(TRANSACTION.FROM_ACCOUNT_ID),
                record.get(TRANSACTION.TO_ACCOUNT_ID),
                record.get(TRANSACTION.AMOUNT),
                record.get(TRANSACTION.CREATED_AT)
            ));
    }
}