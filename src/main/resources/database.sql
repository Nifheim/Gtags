DROP TABLE selected;
DROP TABLE tags;
DROP TABLE designer;
CREATE TABLE designer
(
    designer_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY NOT NULL,
    uniqueId    BINARY(16)                              NOT NULL,
    username    VARCHAR(16)                             NOT NULL,
    UNIQUE INDEX (uniqueId, username)
);
CREATE TABLE tags
(
    name        VARCHAR(50) PRIMARY KEY NOT NULL,
    displayName VARCHAR(50)             NOT NULL,
    lore        TEXT,
    permission  VARCHAR(50),
    designer_id INT UNSIGNED REFERENCES designer (designer_id)
);
CREATE TABLE selected
(
    name     VARCHAR(50) REFERENCES tags (name),
    uniqueId BINARY(16) REFERENCES designer (uniqueId)
);