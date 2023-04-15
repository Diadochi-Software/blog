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
        'a hashed password',
        'John',
        'Doe',
        'CompanyTM',
        'ADMIN');

INSERT INTO users(email, hashedPassword, firstName, lastName, company, role)
VALUES ('jane@doe.com',
        'another hashed password',
        'John',
        'Doe',
        'CompanyTM',
        'AUTHOR');
