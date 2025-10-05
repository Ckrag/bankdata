package dk.ckrag.account;

import dk.ckrag.account.dto.UserDto;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Singleton;

@Singleton
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getAuthorizedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String userIdentifier = authentication.getName();

        // Try to parse as ID first (for backward compatibility)
        UserDto user;
        try {
            Long userId = Long.parseLong(userIdentifier);
            user = userRepository.findById(userId);
        } catch (NumberFormatException e) {
            // If not a number, treat as username
            user = userRepository.findByName(userIdentifier);
        }

        if (user == null) {
            throw new HttpStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        return user;
    }
}
