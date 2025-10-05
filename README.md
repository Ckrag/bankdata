## Coding Challenge

### The Solution

This project offers user-authenticated creation of value accounts and transaction of value between them.
Access is strictly limited to only relevant user.

Service is interacted with through:
* `POST: /banking/account` - Create account
* `GET: /banking/account` - List all accounts of user
* `POST: /banking/transaction/transfer` - Transfer money between accounts
* `GET: /banking/transaction/audit/<user_id>` Get all transactions user is involved in

We don't expose a way to create a user, only do it through tests as use-creation is a bit out of scope
and would be handled in another service anyway :D

### Auditing

For auditing beyond just listing transactions of a user, we need a more fine-grained
permission system to allow "admin" and "users", as the current audit route is "simple" and meant
for the user itself.
Additionally, as auditing/audit-generation can be he heavy, it is likely to be a more async operation
through jobs and queuing.
To surface data and manage alarms I would use Grafana.

### Observability

The service implements the standard micronaut health-check, though with tightly restricted
meta-data as the route is open.
The database also has a health-check that just asserts responsiveness.

In production you would also add alerts for exceptions in the application log
as well as graph avg/mean response-times and general DB load and a log showing slowest SQL queries.

### Security

Currently it uses a simple JWT token with no user password. In a real production environment
you would naturally integrate with a shared auth service.

### Coding standard

A lint check should exist in the project that matches the company-agreed coding style.

### Optimizations

The DB-tables should have appropriate indexes beyond just PK/FKs.
Which, obviously depends on (future) usage.

Don't do work/IO on "UI"-thread. Should be offloaded to appropriate handler.

### Testing

As the service is critical, this is pretty heavily tested. All happy-paths and expected exceptions are tested.

### Deployment

Deploying this: I would just SSH to my server and run the docker-compose 
1. SSH to server
2. Run docker compose as daemon
3. Setup Nginx reverse-proxy to forward requests to docker

## Development

### jOOQ Code Generation

The jOOQ code generation is not run automatically. To manually generate the jOOQ code from the database schema, run the following command:

```bash
./gradlew generateJooq
```

After generating the jOOQ code, you can move the generated files to your source directory using the provided script:

```bash
./move_jooq.sh
```

### Database

To connect to the PostgreSQL database, you can use the following command, which includes the password:

```bash
psql "postgresql://user:password@localhost:5432/bankdata"
```

#### Flyway Migrations

To manually run the Flyway database migrations, you can use the following command:

```bash
./gradlew flywayMigrate
```