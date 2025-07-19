--liquibase formatted sql
--changeset SadykovRI:v1_user_role
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS "role" (
    id            uuid         NOT NULL DEFAULT gen_random_uuid(),
    "name"        VARCHAR(50)  NOT NULL UNIQUE,
    created_date  timestamp(6) DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT role_name_key   UNIQUE (name),
    CONSTRAINT role_pkey       PRIMARY KEY (id)
);

INSERT INTO "role" (name) VALUES ('ROLE_USER'), ('ROLE_MENTOR'), ('ROLE_ADMIN');

CREATE TABLE IF NOT EXISTS "user" (
    id         uuid         NOT NULL,
    username   VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT user_pkey    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS user_roles (
    role_id uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT user_roles_pkey PRIMARY KEY (role_id, user_id),
    CONSTRAINT user_roles_user_fk FOREIGN KEY (user_id) REFERENCES "user" (id),
    CONSTRAINT user_roles_role_fk FOREIGN KEY (role_id) REFERENCES "role" (id)
);
