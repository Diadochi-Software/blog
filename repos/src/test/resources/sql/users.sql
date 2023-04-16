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
        '$2a$10$cOnfEuIG3SsmNp3WpzHmTOOYk4DSfY8KAtWoxhpq0LM6RgKL76mrS',
        'John',
        'Doe',
        'CompanyTM',
        'ADMIN');

INSERT INTO users(email, hashedPassword, firstName, lastName, company, role)
VALUES ('jane@doe.com',
        '$2a$10$cOnfEuIG3SsmNp3WpzHmTOOYk4DSfY8KAtWoxhpq0LM6RgKL76mrS',
        'John',
        'Doe',
        'CompanyTM',
        'AUTHOR');
