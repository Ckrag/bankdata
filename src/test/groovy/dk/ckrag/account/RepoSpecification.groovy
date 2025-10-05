package dk.ckrag.account

import io.micronaut.test.support.TestPropertyProvider
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

abstract class RepoSpecification extends Specification implements TestPropertyProvider {

    @Shared
    @AutoCleanup
    PostgreSQLContainer postgresContainer

    @Override
    Map<String, String> getProperties() {
        if (postgresContainer == null) {
            postgresContainer = new PostgreSQLContainer("postgres:bullseye")
            postgresContainer.start()

            Flyway flyway = Flyway.configure()
                    .dataSource(
                            postgresContainer.getJdbcUrl(),
                            postgresContainer.getUsername(),
                            postgresContainer.getPassword()
                    )
                    .locations("filesystem:./flyway/sql")
                    .load()
            flyway.migrate()
        }

        return [
                'datasources.default.url': postgresContainer.getJdbcUrl(),
                'datasources.default.password': postgresContainer.getPassword(),
                'datasources.default.username': postgresContainer.getUsername(),
                'datasources.default.driverClassName': 'org.postgresql.Driver',
                'jooq.datasources.default.enabled': 'true',
                'micronaut.security.token.jwt.signatures.secret.generator.secret': 'testSecretKeyForJwtTokenGenerationInTests',
        ]
    }

}
