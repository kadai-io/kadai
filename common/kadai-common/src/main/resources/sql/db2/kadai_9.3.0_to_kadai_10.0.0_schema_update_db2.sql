SET SCHEMA %schemaName%;

ALTER TABLE TASK ADD COLUMN IS_REOPENED SMALLINT NOT NULL DEFAULT 0;

UPDATE TASK SET IS_REOPENED=0;

INSERT INTO KADAI_SCHEMA_VERSION (ID, VERSION, CREATED)
VALUES (KADAI_SCHEMA_VERSION_ID_SEQ.NEXTVAL, '10.0.0', CURRENT_TIMESTAMP);