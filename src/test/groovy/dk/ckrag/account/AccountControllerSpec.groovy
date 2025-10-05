package dk.ckrag.account

import dk.ckrag.account.dto.UserDto
import dk.ckrag.account.request.CreateAccountRequest
import dk.ckrag.account.response.AccountListResponse
import dk.ckrag.account.response.AccountResponse
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject

@MicronautTest
class AccountControllerSpec extends RepoSpecification {

    @Inject
    @Client("/")
    HttpClient client

    @Inject
    UserRepository userRepository

    UserDto testUser
    String authToken

    def setup() {
        // Create unique user for each test to avoid duplicates
        String uniqueUserName = "Test User " + System.currentTimeMillis()
        testUser = userRepository.create(uniqueUserName)

        // Login using Micronaut's standard /login endpoint to get JWT token
        def credentials = new UsernamePasswordCredentials(testUser.name, "password")
        def loginRequest = HttpRequest.POST("/login", credentials)
        BearerAccessRefreshToken tokenResponse = client.toBlocking().retrieve(loginRequest, BearerAccessRefreshToken)
        authToken = tokenResponse.accessToken
    }

    void "test createAccount creates account for user"() {
        given: "an initial amount"
        Integer initialAmount = 100

        and: "a CreateAccountRequest"
        CreateAccountRequest request = new CreateAccountRequest(initialAmount)

        when: "the createAccount endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.POST("/banking/account", request)
                .bearerAuth(authToken)
        AccountResponse response = client.toBlocking().retrieve(httpRequest, AccountResponse)

        then: "the response should match the expected AccountResponse"
        response != null
        response.id != null
        response.amount == initialAmount
        response.userId == testUser.id
    }

    void "test listAccounts returns all accounts for user"() {
        given: "two CreateAccountRequests"
        CreateAccountRequest request1 = new CreateAccountRequest(100)
        CreateAccountRequest request2 = new CreateAccountRequest(200)

        and: "two accounts are created"
        client.toBlocking().retrieve(HttpRequest.POST("/banking/account", request1).bearerAuth(authToken), AccountResponse)
        client.toBlocking().retrieve(HttpRequest.POST("/banking/account", request2).bearerAuth(authToken), AccountResponse)

        when: "the listAccounts endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.GET("/banking/account").bearerAuth(authToken)
        def response = client.toBlocking().retrieve(httpRequest, AccountListResponse)

        then: "all accounts for the user are returned"
        response != null
        response.accounts != null
        response.accounts.size() == 2
        response.accounts.every { it.userId == testUser.id }
        response.accounts*.amount.sort() == [100, 200]
    }


    void "test listAccounts only returns authenticated user's accounts"() {
        given: "another user with accounts"
        String otherUserName = "Other User " + System.currentTimeMillis()
        UserDto otherUser = userRepository.create(otherUserName)

        def otherCredentials = new UsernamePasswordCredentials(otherUser.name, "password")
        def otherLoginRequest = HttpRequest.POST("/login", otherCredentials)
        BearerAccessRefreshToken otherTokenResponse = client.toBlocking().retrieve(otherLoginRequest, BearerAccessRefreshToken)
        String otherAuthToken = otherTokenResponse.accessToken

        and: "the other user creates 3 accounts"
        client.toBlocking().retrieve(HttpRequest.POST("/banking/account", new CreateAccountRequest(1000)).bearerAuth(otherAuthToken), AccountResponse)
        client.toBlocking().retrieve(HttpRequest.POST("/banking/account", new CreateAccountRequest(2000)).bearerAuth(otherAuthToken), AccountResponse)
        client.toBlocking().retrieve(HttpRequest.POST("/banking/account", new CreateAccountRequest(3000)).bearerAuth(otherAuthToken), AccountResponse)

        and: "the authenticated user creates 2 accounts"
        client.toBlocking().retrieve(HttpRequest.POST("/banking/account", new CreateAccountRequest(100)).bearerAuth(authToken), AccountResponse)
        client.toBlocking().retrieve(HttpRequest.POST("/banking/account", new CreateAccountRequest(200)).bearerAuth(authToken), AccountResponse)

        when: "the authenticated user lists their accounts"
        HttpRequest<?> httpRequest = HttpRequest.GET("/banking/account").bearerAuth(authToken)
        def response = client.toBlocking().retrieve(httpRequest, dk.ckrag.account.response.AccountListResponse)

        then: "only the authenticated user's accounts are returned"
        response != null
        response.accounts != null
        response.accounts.size() == 2
        response.accounts.every { it.userId == testUser.id }
        response.accounts*.amount.sort() == [100, 200]
    }

    void "test createAccount creates account for authenticated user only"() {
        given: "an initial amount"
        Integer initialAmount = 500

        and: "a CreateAccountRequest"
        CreateAccountRequest request = new CreateAccountRequest(initialAmount)

        when: "the createAccount endpoint is called with auth token"
        HttpRequest<?> httpRequest = HttpRequest.POST("/banking/account", request).bearerAuth(authToken)
        AccountResponse response = client.toBlocking().retrieve(httpRequest, AccountResponse)

        then: "the account is created for the authenticated user"
        // Implicit authorization: the account is automatically assigned to the authenticated user
        // There is no way to specify a different userId in the request
        response != null
        response.id != null
        response.amount == initialAmount
        response.userId == testUser.id
    }


}