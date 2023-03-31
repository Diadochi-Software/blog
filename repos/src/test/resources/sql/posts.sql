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

INSERT INTO posts(id, author_email, original_language, created_at, tags, isActive, image)
VALUES ('00000000-0000-0000-0000-000000000001',
        'john@doe.com',
        'en',
        '2023-03-31T18:56:32.728924',
        ARRAY ['tag1', 'tag2'],
        FALSE,
        NULL)
