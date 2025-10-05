package dk.ckrag.account;

import dk.ckrag.account.dto.UserDto;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import static dk.ckrag.jooq.tables.User.USER;

@Singleton
public class UserRepository {

    private final DSLContext dslContext;

    public UserRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public UserDto create(String name) {
        Long userId = dslContext.insertInto(USER)
                .columns(USER.NAME)
                .values(name)
                .returning(USER.ID)
                .fetchOne()
                .getId();
        return new UserDto(userId, name);
    }

    public UserDto findById(Long id) {
        return dslContext.select(USER.ID, USER.NAME)
                .from(USER)
                .where(USER.ID.eq(id))
                .fetchOptional()
                .map(record -> new UserDto(record.get(USER.ID), record.get(USER.NAME)))
                .orElse(null);
    }

    public UserDto findByName(String name) {
        return dslContext.select(USER.ID, USER.NAME)
                .from(USER)
                .where(USER.NAME.eq(name))
                .fetchOptional()
                .map(record -> new UserDto(record.get(USER.ID), record.get(USER.NAME)))
                .orElse(null);
    }
}
