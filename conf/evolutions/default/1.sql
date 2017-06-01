# --- !Ups

CREATE TABLE user (
  id int(11) unsigned NOT NULL AUTO_INCREMENT,
  uuid varchar(255) NOT NULL,
  email varchar(255) DEFAULT NULL,
  password varchar(255) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uuid (uuid),
  UNIQUE KEY email (email)
) DEFAULT CHARSET=utf8;

# --- !Downs

DROP TABLE user;