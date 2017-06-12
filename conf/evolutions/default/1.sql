# --- !Ups

CREATE TABLE auth_token(
  user_id BIGINT NOT NULL,
  expiry TIMESTAMP DEFAULT now(),
  id BIGSERIAL PRIMARY KEY
);

CREATE TABLE "user" (
  id BIGSERIAL PRIMARY KEY,
  user_id varchar(255) NOT NULL,
  email varchar(255) NOT NULL,
  password varchar(255) NOT NULL
);

CREATE UNIQUE INDEX unique_user_id_user ON "user" (user_id);
CREATE INDEX index_email_user ON "user" (email);

# --- !Downs

DROP TABLE IF EXISTS "user" CASCADE;
