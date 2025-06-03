CREATE TABLE location_user
(
    role        SMALLINT NOT NULL,
    location_id BIGINT   NOT NULL,
    user_id     UUID     NOT NULL,
    CONSTRAINT pk_location_user PRIMARY KEY (location_id, user_id)
);

CREATE TABLE users
(
    id         UUID                        NOT NULL,
    email      VARCHAR(255)                NOT NULL,
    password   VARCHAR(255)                NOT NULL,
    is_admin   BOOLEAN                     NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE location_user
    ADD CONSTRAINT FK_LOCATION_USER_ON_LOCATION FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE;

ALTER TABLE location_user
    ADD CONSTRAINT FK_LOCATION_USER_ON_USER FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
