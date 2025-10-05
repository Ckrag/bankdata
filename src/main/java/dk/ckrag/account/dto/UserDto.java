package dk.ckrag.account.dto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class UserDto {
    private final Long id;
    private final String name;

    public UserDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
