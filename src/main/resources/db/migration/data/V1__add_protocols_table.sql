CREATE TABLE protocols (
    id CHARACTER VARYING(255) NOT NULL,
    version INTEGER NOT NULL,
    protocol BYTEA,
    PRIMARY KEY (id, version)
);
