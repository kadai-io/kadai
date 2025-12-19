SET SCHEMA %schemaName%;

INSERT INTO KADAI_SCHEMA_VERSION (ID, VERSION, CREATED)
VALUES (KADAI_SCHEMA_VERSION_ID_SEQ.NEXTVAL, '12.0.0', CURRENT_TIMESTAMP);

-- Depending on subsystem-parameter 'DDL_MATERIALIZATION',
-- affected indexes may be altered immediately or later where they then may require human intervention.
-- We therefore recommend running this script with 'DDL_MATERIALIZATION=ALWAYS_IMMEDIATE' in the maintenance-window.
-- For more information, consult below DB2-Docs.
-- https://www.ibm.com/docs/en/db2-for-zos/12.0.0?topic=adtc-what-happens-index-altered-columns-when-immediate-column-alteration-is-in-effect
ALTER TABLE CLASSIFICATION ALTER COLUMN APPLICATION_ENTRY_POINT SET DATA TYPE VARCHAR(511);
ALTER TABLE CLASSIFICATION_HISTORY_EVENT ALTER COLUMN APPLICATION_ENTRY_POINT SET DATA TYPE VARCHAR(511);
