ALTER TABLE account
ADD COLUMN user_id BIGINT;

ALTER TABLE account
ADD CONSTRAINT fk_user
    FOREIGN KEY (user_id)
    REFERENCES "user" (id);
