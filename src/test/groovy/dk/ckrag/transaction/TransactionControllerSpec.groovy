package dk.ckrag.transaction

import dk.ckrag.account.RepoSpecification
import dk.ckrag.account.UserRepository
import dk.ckrag.account.dto.UserDto
import dk.ckrag.account.request.CreateAccountRequest
import dk.ckrag.account.request.TransferRequest
import dk.ckrag.account.response.AccountResponse
import dk.ckrag.account.response.TransactionResponse
import dk.ckrag.transaction.response.TransactionAuditResponse
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest
class TransactionControllerSpec extends RepoSpecification {

    @Inject
    @Client("/")
    HttpClient client

    @Inject
    UserRepository userRepository

    UserDto testUser
    String authToken

    def setup() {
        String uniqueUserName = "Test User " + System.currentTimeMillis()
        testUser = userRepository.create(uniqueUserName)

        // Login using Micronaut's standard /login endpoint to get JWT token
        def credentials = new UsernamePasswordCredentials(testUser.name, "password")
        def loginRequest = HttpRequest.POST("/login", credentials)
        BearerAccessRefreshToken tokenResponse = client.toBlocking().retrieve(loginRequest, BearerAccessRefreshToken)
        authToken = tokenResponse.accessToken
    }

    void "test transfer between own accounts succeeds"() {
        given: "two accounts for the authenticated user"
        AccountResponse fromAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(1000)).bearerAuth(authToken),
            AccountResponse
        )
        AccountResponse toAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(500)).bearerAuth(authToken),
            AccountResponse
        )

        and: "a transfer request"
        def transferRequest = new TransferRequest(fromAccount.id, toAccount.id, 300)

        when: "the transfer endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.POST("/banking/transaction/transfer", transferRequest).bearerAuth(authToken)
        def response = client.toBlocking().retrieve(httpRequest, TransactionResponse)

        then: "the transfer succeeds"
        response != null
        response.amount == 300
    }

    void "test transfer from another user's account fails"() {
        given: "another user with an account"
        String otherUserName = "Other User " + System.currentTimeMillis()
        UserDto otherUser = userRepository.create(otherUserName)

        def otherCredentials = new UsernamePasswordCredentials(otherUser.name, "password")
        def otherLoginRequest = HttpRequest.POST("/login", otherCredentials)
        BearerAccessRefreshToken otherTokenResponse = client.toBlocking().retrieve(otherLoginRequest, BearerAccessRefreshToken)
        String otherAuthToken = otherTokenResponse.accessToken

        and: "the other user creates an account"
        AccountResponse otherUserAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(1000)).bearerAuth(otherAuthToken),
            AccountResponse
        )

        and: "the authenticated user creates their own account"
        AccountResponse myAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(500)).bearerAuth(authToken),
            AccountResponse
        )

        and: "a transfer request attempting to transfer FROM the other user's account"
        def transferRequest = new TransferRequest(otherUserAccount.id, myAccount.id, 100)

        when: "the transfer endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.POST("/banking/transaction/transfer", transferRequest).bearerAuth(authToken)
        client.toBlocking().retrieve(httpRequest, TransactionResponse)

        then: "the transfer is rejected with a forbidden error"
        def exception = thrown(HttpClientResponseException)
        exception.status.code == 403
    }

    void "test transfer with negative amount returns 400"() {
        given: "two accounts for the authenticated user"
        AccountResponse fromAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(1000)).bearerAuth(authToken),
            AccountResponse
        )
        AccountResponse toAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(500)).bearerAuth(authToken),
            AccountResponse
        )

        and: "a transfer request with negative amount"
        def transferRequest = new TransferRequest(fromAccount.id, toAccount.id, -100)

        when: "the transfer endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.POST("/banking/transaction/transfer", transferRequest).bearerAuth(authToken)
        client.toBlocking().retrieve(httpRequest, TransactionResponse)

        then: "bad request is returned"
        def exception = thrown(HttpClientResponseException)
        exception.status.code == 400
    }

    void "test transfer with zero amount returns 400"() {
        given: "two accounts for the authenticated user"
        AccountResponse fromAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(1000)).bearerAuth(authToken),
            AccountResponse
        )
        AccountResponse toAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(500)).bearerAuth(authToken),
            AccountResponse
        )

        and: "a transfer request with zero amount"
        def transferRequest = new TransferRequest(fromAccount.id, toAccount.id, 0)

        when: "the transfer endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.POST("/banking/transaction/transfer", transferRequest).bearerAuth(authToken)
        client.toBlocking().retrieve(httpRequest, TransactionResponse)

        then: "bad request is returned"
        def exception = thrown(HttpClientResponseException)
        exception.status.code == 400
    }

    void "test transfer from non-existent source account returns 404"() {
        given: "a valid destination account"
        AccountResponse toAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(500)).bearerAuth(authToken),
            AccountResponse
        )

        and: "a transfer request with non-existent source account"
        def transferRequest = new TransferRequest(99999L, toAccount.id, 100)

        when: "the transfer endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.POST("/banking/transaction/transfer", transferRequest).bearerAuth(authToken)
        client.toBlocking().retrieve(httpRequest, TransactionResponse)

        then: "not found is returned"
        def exception = thrown(HttpClientResponseException)
        exception.status.code == 404
    }

    void "test transfer to non-existent destination account returns 404"() {
        given: "a valid source account with funds"
        AccountResponse fromAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(1000)).bearerAuth(authToken),
            AccountResponse
        )

        and: "a transfer request with non-existent destination account"
        def transferRequest = new TransferRequest(fromAccount.id, 99999L, 100)

        when: "the transfer endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.POST("/banking/transaction/transfer", transferRequest).bearerAuth(authToken)
        client.toBlocking().retrieve(httpRequest, TransactionResponse)

        then: "not found is returned"
        def exception = thrown(HttpClientResponseException)
        exception.status.code == 404
    }

    void "test transfer with insufficient funds returns 400"() {
        given: "two accounts"
        AccountResponse fromAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(100)).bearerAuth(authToken),
            AccountResponse
        )
        AccountResponse toAccount = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(500)).bearerAuth(authToken),
            AccountResponse
        )

        and: "a transfer request exceeding available balance"
        def transferRequest = new TransferRequest(fromAccount.id, toAccount.id, 500)

        when: "the transfer endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.POST("/banking/transaction/transfer", transferRequest).bearerAuth(authToken)
        client.toBlocking().retrieve(httpRequest, TransactionResponse)

        then: "bad request is returned"
        def exception = thrown(HttpClientResponseException)
        exception.status.code == 400
    }

    void "test audit returns all transactions for authenticated user"() {
        given: "two accounts for the authenticated user"
        AccountResponse account1 = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(1000)).bearerAuth(authToken),
            AccountResponse
        )
        AccountResponse account2 = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(500)).bearerAuth(authToken),
            AccountResponse
        )

        and: "two transfers between the accounts"
        client.toBlocking().retrieve(
            HttpRequest.POST("/banking/transaction/transfer", new TransferRequest(account1.id, account2.id, 100)).bearerAuth(authToken),
            TransactionResponse
        )
        client.toBlocking().retrieve(
            HttpRequest.POST("/banking/transaction/transfer", new TransferRequest(account2.id, account1.id, 50)).bearerAuth(authToken),
            TransactionResponse
        )

        when: "the audit endpoint is called"
        HttpRequest<?> httpRequest = HttpRequest.GET("/banking/transaction/audit/${testUser.id}").bearerAuth(authToken)
        def response = client.toBlocking().retrieve(httpRequest, TransactionAuditResponse)

        then: "all transactions are returned"
        response != null
        response.transactions != null
        response.transactions.size() == 2
        response.transactions*.amount.sort() == [50, 100]
    }

    void "test audit only returns transactions user is involved in"() {
        given: "another user with accounts"
        String otherUserName = "Other User " + System.currentTimeMillis()
        UserDto otherUser = userRepository.create(otherUserName)

        def otherCredentials = new UsernamePasswordCredentials(otherUser.name, "password")
        def otherLoginRequest = HttpRequest.POST("/login", otherCredentials)
        BearerAccessRefreshToken otherTokenResponse = client.toBlocking().retrieve(otherLoginRequest, BearerAccessRefreshToken)
        String otherAuthToken = otherTokenResponse.accessToken

        and: "the other user creates two accounts and makes a transfer"
        AccountResponse otherAccount1 = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(1000)).bearerAuth(otherAuthToken),
            AccountResponse
        )
        AccountResponse otherAccount2 = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(500)).bearerAuth(otherAuthToken),
            AccountResponse
        )
        client.toBlocking().retrieve(
            HttpRequest.POST("/banking/transaction/transfer", new TransferRequest(otherAccount1.id, otherAccount2.id, 200)).bearerAuth(otherAuthToken),
            TransactionResponse
        )

        and: "the authenticated user creates accounts and makes a transfer"
        AccountResponse myAccount1 = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(1000)).bearerAuth(authToken),
            AccountResponse
        )
        AccountResponse myAccount2 = client.toBlocking().retrieve(
            HttpRequest.POST("/banking/account", new CreateAccountRequest(500)).bearerAuth(authToken),
            AccountResponse
        )
        client.toBlocking().retrieve(
            HttpRequest.POST("/banking/transaction/transfer", new TransferRequest(myAccount1.id, myAccount2.id, 150)).bearerAuth(authToken),
            TransactionResponse
        )

        when: "the authenticated user audits their transactions"
        HttpRequest<?> httpRequest = HttpRequest.GET("/banking/transaction/audit/${testUser.id}").bearerAuth(authToken)
        def response = client.toBlocking().retrieve(httpRequest, TransactionAuditResponse)

        then: "only the authenticated user's transaction is returned"
        response != null
        response.transactions != null
        response.transactions.size() == 1
        response.transactions[0].amount == 150
    }

    void "test audit fails when requesting another user's audit"() {
        given: "another user"
        String otherUserName = "Other User " + System.currentTimeMillis()
        UserDto otherUser = userRepository.create(otherUserName)

        when: "the authenticated user tries to audit the other user's transactions"
        HttpRequest<?> httpRequest = HttpRequest.GET("/banking/transaction/audit/${otherUser.id}").bearerAuth(authToken)
        client.toBlocking().retrieve(httpRequest, TransactionAuditResponse)

        then: "forbidden error is returned"
        def exception = thrown(HttpClientResponseException)
        exception.status.code == 403
    }

}
