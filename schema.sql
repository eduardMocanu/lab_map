-- Social network schema (Lab 5)

CREATE TABLE IF NOT EXISTS users (
    id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR NOT NULL UNIQUE,
    email    VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS friendships (
    id1          BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    id2          BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    friends_from TIMESTAMP NOT NULL,
    PRIMARY KEY (id1, id2),
    CHECK (id1 < id2)
    );