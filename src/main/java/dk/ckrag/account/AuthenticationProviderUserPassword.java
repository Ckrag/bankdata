package dk.ckrag.account;

import dk.ckrag.account.dto.UserDto;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.provider.HttpRequestAuthenticationProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AuthenticationProviderUserPassword<B> implements HttpRequestAuthenticationProvider<B> {

    @Inject
    private UserRepository userRepository;

    @Override
    public AuthenticationResponse authenticate(@Nullable HttpRequest<B> httpRequest,
                                                AuthenticationRequest<String, String> authenticationRequest) {
        String username = authenticationRequest.getIdentity();

        UserDto user = userRepository.findByName(username);

        if (user == null) {
            return AuthenticationResponse.failure(AuthenticationFailureReason.USER_NOT_FOUND);
        }

        // This is a bit silly since we don't have any passwords, but we want to demonstrate auth checks
        // normally this is where you do actual verification :o)

        return AuthenticationResponse.success(
                user.getId().toString(),
                java.util.List.of("ROLE_USER")
        );
    }
}
