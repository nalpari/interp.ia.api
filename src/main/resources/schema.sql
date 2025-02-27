CREATE TABLE IF NOT EXISTS `member`
(
    id            BIGINT AUTO_INCREMENT,
    user_id       VARCHAR(255)          NOT NULL UNIQUE,
    password      VARCHAR(255)          NOT NULL,
    email         VARCHAR(255)          NOT NULL UNIQUE,
    name          VARCHAR(255)          NOT NULL,
    role          ENUM ('ADMIN','USER') NOT NULL DEFAULT 'USER',
    refresh_token VARCHAR(1000),
    created_at    TIMESTAMP(6),
    updated_at    TIMESTAMP(6),
    PRIMARY KEY (id)
);