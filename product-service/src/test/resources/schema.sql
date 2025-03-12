CREATE TABLE products
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    description VARCHAR(255),
    price       DECIMAL(10, 2) NOT NULL,
    stock       INT            NOT NULL
);