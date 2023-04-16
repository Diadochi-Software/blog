CREATE TABLE users
(
    email          TEXT PRIMARY KEY,
    hashedPassword TEXT NOT NULL,
    firstName      TEXT,
    lastName       TEXT,
    company        TEXT,
    role           TEXT NOT NULL
);

INSERT INTO users(email, hashedPassword, firstName, lastName, company, role)
VALUES ('john@doe.com',
        '$2a$10$mvX89VIiN1BJIe7BJJ6jweQMDDcveZtNPZXtV/.3fljL6x3I1wy2K',
        'John',
        'Doe',
        'CompanyTM',
        'ADMIN');

INSERT INTO users(email, hashedPassword, firstName, lastName, company, role)
VALUES ('jane@doe.com',
        '$2a$10$XB5lNTTyHJVkGIDn5p8VueMhRrmotVkUVv9cQ5RiZkCGNPzGadnc6',
        'John',
        'Doe',
        'CompanyTM',
        'AUTHOR');
