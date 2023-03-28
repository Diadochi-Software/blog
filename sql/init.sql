CREATE DATABASE blog;

\c blog;

CREATE TABLE posts
(
    id                UUID PRIMARY KEY,
    author_email      TEXT,
    original_language TEXT      NOT NULL, -- ISO 639-1
    created_at        TIMESTAMP NOT NULL,
    tags              TEXT[]    NOT NULL,
    isActive          BOOLEAN   NOT NULL,
    image             TEXT      NULL
);

CREATE TABLE post_info
(
    post_id     UUID,
    lang        TEXT, -- ISO 639-1
    title       TEXT NOT NULL,
    description TEXT NOT NULL,
    markdown    TEXT NOT NULL,
    PRIMARY KEY (post_id, lang),
    FOREIGN KEY (post_id) REFERENCES posts (id)
);
