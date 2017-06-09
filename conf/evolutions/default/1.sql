# --- !Ups

CREATE TABLE "user" (
  id BIGSERIAL PRIMARY KEY,
  uuid varchar(255) NOT NULL,
  email varchar(255) DEFAULT NULL,
  password varchar(255) DEFAULT NULL
);

CREATE UNIQUE INDEX unique_uuid_user ON "user" (uuid);
CREATE INDEX index_email_user ON "user" (email);

# --- !Downs

DROP TABLE "user";
