SET search_path TO %schemaName%;

-- validates schema exists: can be any arbitrary query on the kadai-schema
SELECT * FROM KADAI_SCHEMA_VERSION;
