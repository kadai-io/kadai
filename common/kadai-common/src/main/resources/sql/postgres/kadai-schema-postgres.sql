CREATE SCHEMA IF NOT EXISTS %schemaName%;

SET
search_path TO %schemaName%;

CREATE TABLE KADAI_SCHEMA_VERSION
(
    ID      INT          NOT NULL,
    VERSION VARCHAR(255) NOT NULL,
    CREATED TIMESTAMP    NOT NULL,
    PRIMARY KEY (ID)
);

CREATE SEQUENCE KADAI_SCHEMA_VERSION_ID_SEQ
    MINVALUE 1
    START WITH 1000
    INCREMENT BY 1 CACHE 10;

-- The VERSION value must be equal or higher then the value of KadaiEngineImpl.MINIMAL_KADAI_SCHEMA_VERSION
INSERT INTO KADAI_SCHEMA_VERSION (ID, VERSION, CREATED)
VALUES (nextval('KADAI_SCHEMA_VERSION_ID_SEQ'), '10.0.0', CURRENT_TIMESTAMP);

CREATE TABLE CLASSIFICATION
(
    ID                      VARCHAR(40) NOT NULL,
    KEY                     VARCHAR(32) NOT NULL,
    PARENT_ID               VARCHAR(40) NOT NULL,
    PARENT_KEY              VARCHAR(32) NOT NULL,
    CATEGORY                VARCHAR(32),
    TYPE                    VARCHAR(32),
    DOMAIN                  VARCHAR(32) NOT NULL,
    VALID_IN_DOMAIN         BOOLEAN     NOT NULL,
    CREATED                 TIMESTAMP NULL,
    MODIFIED                TIMESTAMP NULL,
    NAME                    VARCHAR(255) NULL,
    DESCRIPTION             VARCHAR(255) NULL,
    PRIORITY                INT         NOT NULL,
    SERVICE_LEVEL           VARCHAR(255) NULL,
    APPLICATION_ENTRY_POINT VARCHAR(255) NULL,
    CUSTOM_1                VARCHAR(255) NULL,
    CUSTOM_2                VARCHAR(255) NULL,
    CUSTOM_3                VARCHAR(255) NULL,
    CUSTOM_4                VARCHAR(255) NULL,
    CUSTOM_5                VARCHAR(255) NULL,
    CUSTOM_6                VARCHAR(255) NULL,
    CUSTOM_7                VARCHAR(255) NULL,
    CUSTOM_8                VARCHAR(255) NULL,
    PRIMARY KEY (ID),
    CONSTRAINT UC_CLASS_KEY_DOMAIN UNIQUE (KEY, DOMAIN)
);

CREATE TABLE WORKBASKET
(
    ID                  VARCHAR(40)  NOT NULL,
    KEY                 VARCHAR(64)  NOT NULL,
    CREATED             TIMESTAMP NULL,
    MODIFIED            TIMESTAMP NULL,
    NAME                VARCHAR(255) NOT NULL,
    DOMAIN              VARCHAR(32)  NOT NULL,
    TYPE                VARCHAR(16)  NOT NULL,
    DESCRIPTION         VARCHAR(255) NULL,
    OWNER               VARCHAR(128) NULL,
    CUSTOM_1            VARCHAR(255) NULL,
    CUSTOM_2            VARCHAR(255) NULL,
    CUSTOM_3            VARCHAR(255) NULL,
    CUSTOM_4            VARCHAR(255) NULL,
    ORG_LEVEL_1         VARCHAR(255) NULL,
    ORG_LEVEL_2         VARCHAR(255) NULL,
    ORG_LEVEL_3         VARCHAR(255) NULL,
    ORG_LEVEL_4         VARCHAR(255) NULL,
    MARKED_FOR_DELETION BOOLEAN      NOT NULL,
    CUSTOM_5            VARCHAR(255) NULL,
    CUSTOM_6            VARCHAR(255) NULL,
    CUSTOM_7            VARCHAR(255) NULL,
    CUSTOM_8            VARCHAR(255) NULL,
    PRIMARY KEY (ID),
    CONSTRAINT WB_KEY_DOMAIN UNIQUE (KEY, DOMAIN)
);

CREATE TABLE TASK
(
    ID                         VARCHAR(40)  NOT NULL,
    EXTERNAL_ID                VARCHAR(64)  NOT NULL,
    CREATED                    TIMESTAMP NULL,
    CLAIMED                    TIMESTAMP NULL,
    COMPLETED                  TIMESTAMP NULL,
    MODIFIED                   TIMESTAMP NULL,
    RECEIVED                   TIMESTAMP NULL,
    PLANNED                    TIMESTAMP NULL,
    DUE                        TIMESTAMP NULL,
    NAME                       VARCHAR(255) NULL,
    CREATOR                    VARCHAR(32) NULL,
    DESCRIPTION                VARCHAR(1024) NULL,
    NOTE                       VARCHAR(4096) NULL,
    PRIORITY                   INT NULL,
    MANUAL_PRIORITY            INT NULL,
    STATE                      VARCHAR(20) NULL,
    CLASSIFICATION_CATEGORY    VARCHAR(32) NULL,
    CLASSIFICATION_KEY         VARCHAR(32) NULL,
    CLASSIFICATION_ID          VARCHAR(40) NULL,
    WORKBASKET_ID              VARCHAR(40) NULL,
    WORKBASKET_KEY             VARCHAR(64) NULL,
    DOMAIN                     VARCHAR(32) NULL,
    BUSINESS_PROCESS_ID        VARCHAR(128) NULL,
    PARENT_BUSINESS_PROCESS_ID VARCHAR(128) NULL,
    OWNER                      VARCHAR(32) NULL,
    POR_COMPANY                VARCHAR(32)  NOT NULL,
    POR_SYSTEM                 VARCHAR(32),
    POR_INSTANCE               VARCHAR(32),
    POR_TYPE                   VARCHAR(32)  NOT NULL,
    POR_VALUE                  VARCHAR(128) NOT NULL,
    IS_READ                    BOOLEAN      NOT NULL,
    IS_TRANSFERRED             BOOLEAN      NOT NULL,
    IS_REOPENED                BOOLEAN      NOT NULL,
    CALLBACK_INFO              TEXT NULL,
    CALLBACK_STATE             VARCHAR(30) NULL,
    CUSTOM_ATTRIBUTES          TEXT NULL,
    CUSTOM_1                   VARCHAR(255) NULL,
    CUSTOM_2                   VARCHAR(255) NULL,
    CUSTOM_3                   VARCHAR(255) NULL,
    CUSTOM_4                   VARCHAR(255) NULL,
    CUSTOM_5                   VARCHAR(255) NULL,
    CUSTOM_6                   VARCHAR(255) NULL,
    CUSTOM_7                   VARCHAR(255) NULL,
    CUSTOM_8                   VARCHAR(255) NULL,
    CUSTOM_9                   VARCHAR(255) NULL,
    CUSTOM_10                  VARCHAR(255) NULL,
    CUSTOM_11                  VARCHAR(255) NULL,
    CUSTOM_12                  VARCHAR(255) NULL,
    CUSTOM_13                  VARCHAR(255) NULL,
    CUSTOM_14                  VARCHAR(255) NULL,
    CUSTOM_15                  VARCHAR(255) NULL,
    CUSTOM_16                  VARCHAR(255) NULL,
    CUSTOM_INT_1               INT NULL,
    CUSTOM_INT_2               INT NULL,
    CUSTOM_INT_3               INT NULL,
    CUSTOM_INT_4               INT NULL,
    CUSTOM_INT_5               INT NULL,
    CUSTOM_INT_6               INT NULL,
    CUSTOM_INT_7               INT NULL,
    CUSTOM_INT_8               INT NULL,
    NUMBER_OF_COMMENTS         INT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT UC_EXTERNAL_ID UNIQUE (EXTERNAL_ID),
    CONSTRAINT TASK_WB FOREIGN KEY (WORKBASKET_ID) REFERENCES WORKBASKET ON DELETE NO ACTION,
    CONSTRAINT TASK_CLASS FOREIGN KEY (CLASSIFICATION_ID) REFERENCES CLASSIFICATION ON DELETE NO ACTION
);

CREATE TABLE DISTRIBUTION_TARGETS
(
    SOURCE_ID VARCHAR(40) NOT NULL,
    TARGET_ID VARCHAR(40) NOT NULL,
    PRIMARY KEY (SOURCE_ID, TARGET_ID)
);

CREATE TABLE WORKBASKET_ACCESS_LIST
(
    ID              VARCHAR(40)  NOT NULL,
    WORKBASKET_ID   VARCHAR(40)  NOT NULL,
    ACCESS_ID       VARCHAR(255) NOT NULL,
    ACCESS_NAME     VARCHAR(255) NULL,
    PERM_READ       BOOLEAN      NOT NULL,
    PERM_OPEN       BOOLEAN      NOT NULL,
    PERM_APPEND     BOOLEAN      NOT NULL,
    PERM_TRANSFER   BOOLEAN      NOT NULL,
    PERM_DISTRIBUTE BOOLEAN      NOT NULL,
    PERM_CUSTOM_1   BOOLEAN      NOT NULL,
    PERM_CUSTOM_2   BOOLEAN      NOT NULL,
    PERM_CUSTOM_3   BOOLEAN      NOT NULL,
    PERM_CUSTOM_4   BOOLEAN      NOT NULL,
    PERM_CUSTOM_5   BOOLEAN      NOT NULL,
    PERM_CUSTOM_6   BOOLEAN      NOT NULL,
    PERM_CUSTOM_7   BOOLEAN      NOT NULL,
    PERM_CUSTOM_8   BOOLEAN      NOT NULL,
    PERM_CUSTOM_9   BOOLEAN      NOT NULL,
    PERM_CUSTOM_10  BOOLEAN      NOT NULL,
    PERM_CUSTOM_11  BOOLEAN      NOT NULL,
    PERM_CUSTOM_12  BOOLEAN      NOT NULL,
    PERM_READTASKS  BOOLEAN      NOT NULL,
    PERM_EDITTASKS  BOOLEAN      NOT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT UC_ACCESSID_WBID UNIQUE (ACCESS_ID, WORKBASKET_ID),
    CONSTRAINT ACCESS_LIST_WB FOREIGN KEY (WORKBASKET_ID) REFERENCES WORKBASKET ON DELETE CASCADE
);

CREATE TABLE OBJECT_REFERENCE
(
    ID              VARCHAR(40)  NOT NULL,
    TASK_ID         VARCHAR(40)  NOT NULL,
    COMPANY         VARCHAR(32)  NOT NULL,
    SYSTEM          VARCHAR(32),
    SYSTEM_INSTANCE VARCHAR(32),
    TYPE            VARCHAR(32)  NOT NULL,
    VALUE           VARCHAR(128) NOT NULL
);

CREATE TABLE ATTACHMENT
(
    ID                 VARCHAR(40)  NOT NULL,
    TASK_ID            VARCHAR(40)  NOT NULL,
    CREATED            TIMESTAMP NULL,
    MODIFIED           TIMESTAMP NULL,
    CLASSIFICATION_KEY VARCHAR(32) NULL,
    CLASSIFICATION_ID  VARCHAR(40) NULL,
    REF_COMPANY        VARCHAR(32)  NOT NULL,
    REF_SYSTEM         VARCHAR(32),
    REF_INSTANCE       VARCHAR(32),
    REF_TYPE           VARCHAR(32)  NOT NULL,
    REF_VALUE          VARCHAR(128) NOT NULL,
    CHANNEL            VARCHAR(64) NULL,
    RECEIVED           TIMESTAMP NULL,
    CUSTOM_ATTRIBUTES  TEXT NULL,
    PRIMARY KEY (ID),
    CONSTRAINT ATT_CLASS FOREIGN KEY (CLASSIFICATION_ID) REFERENCES CLASSIFICATION ON DELETE NO ACTION
);

CREATE TABLE TASK_COMMENT
(
    ID         VARCHAR(40) NOT NULL,
    TASK_ID    VARCHAR(40) NOT NULL,
    TEXT_FIELD VARCHAR(1024) NULL,
    CREATOR    VARCHAR(32) NULL,
    CREATED    TIMESTAMP NULL,
    MODIFIED   TIMESTAMP NULL,
    PRIMARY KEY (ID),
    CONSTRAINT COMMENT_TASK FOREIGN KEY (TASK_ID) REFERENCES TASK ON DELETE CASCADE
);

CREATE TABLE SCHEDULED_JOB
(
    JOB_ID       INTEGER NOT NULL,
    PRIORITY     INTEGER NULL,
    CREATED      TIMESTAMP NULL,
    DUE          TIMESTAMP NULL,
    STATE        VARCHAR(32) NULL,
    LOCKED_BY    VARCHAR(128) NULL,
    LOCK_EXPIRES TIMESTAMP NULL,
    TYPE         VARCHAR(255) NULL,
    RETRY_COUNT  INTEGER NOT NULL,
    ARGUMENTS    TEXT NULL,
    PRIMARY KEY (JOB_ID)
);

CREATE TABLE TASK_HISTORY_EVENT
(
    ID                             VARCHAR(40) NOT NULL,
    BUSINESS_PROCESS_ID            VARCHAR(128) NULL,
    PARENT_BUSINESS_PROCESS_ID     VARCHAR(128) NULL,
    TASK_ID                        VARCHAR(40) NULL,
    EVENT_TYPE                     VARCHAR(32) NULL,
    CREATED                        TIMESTAMP NULL,
    USER_ID                        VARCHAR(32) NULL,
    DOMAIN                         VARCHAR(32) NULL,
    WORKBASKET_KEY                 VARCHAR(64) NULL,
    WORKBASKET_NAME                VARCHAR(255) NULL,
    POR_COMPANY                    VARCHAR(32) NULL,
    POR_SYSTEM                     VARCHAR(32) NULL,
    POR_INSTANCE                   VARCHAR(32) NULL,
    POR_TYPE                       VARCHAR(32) NULL,
    POR_VALUE                      VARCHAR(128) NULL,
    TASK_PRIORITY                  INT NULL,
    TASK_PLANNED                   TIMESTAMP NULL,
    TASK_DUE                       TIMESTAMP NULL,
    TASK_OWNER                     VARCHAR(32) NULL,
    TASK_CLASSIFICATION_KEY        VARCHAR(32) NULL,
    TASK_CLASSIFICATION_NAME       VARCHAR(32) NULL,
    TASK_CLASSIFICATION_CATEGORY   VARCHAR(32) NULL,
    ATTACHMENT_CLASSIFICATION_KEY  VARCHAR(32) NULL,
    ATTACHMENT_CLASSIFICATION_NAME VARCHAR(255) NULL,
    OLD_VALUE                      VARCHAR(255) NULL,
    NEW_VALUE                      VARCHAR(255) NULL,
    CUSTOM_1                       VARCHAR(128) NULL,
    CUSTOM_2                       VARCHAR(128) NULL,
    CUSTOM_3                       VARCHAR(128) NULL,
    CUSTOM_4                       VARCHAR(128) NULL,
    DETAILS                        TEXT NULL,
    PRIMARY KEY (ID)
);

CREATE TABLE WORKBASKET_HISTORY_EVENT
(
    ID            VARCHAR(40) NOT NULL,
    EVENT_TYPE    VARCHAR(40) NULL,
    CREATED       TIMESTAMP NULL,
    USER_ID       VARCHAR(32) NULL,
    DOMAIN        VARCHAR(32) NULL,
    WORKBASKET_ID VARCHAR(40) NULL,
    KEY           VARCHAR(64) NULL,
    TYPE          VARCHAR(64) NULL,
    OWNER         VARCHAR(128) NULL,
    CUSTOM_1      VARCHAR(255) NULL,
    CUSTOM_2      VARCHAR(255) NULL,
    CUSTOM_3      VARCHAR(255) NULL,
    CUSTOM_4      VARCHAR(255) NULL,
    ORGLEVEL_1    VARCHAR(255) NULL,
    ORGLEVEL_2    VARCHAR(255) NULL,
    ORGLEVEL_3    VARCHAR(255) NULL,
    ORGLEVEL_4    VARCHAR(255) NULL,
    DETAILS       TEXT NULL,
    PRIMARY KEY (ID)
);

CREATE TABLE CLASSIFICATION_HISTORY_EVENT
(
    ID                      VARCHAR(40) NOT NULL,
    EVENT_TYPE              VARCHAR(40) NULL,
    CREATED                 TIMESTAMP NULL,
    USER_ID                 VARCHAR(32) NULL,
    CLASSIFICATION_ID       VARCHAR(40) NULL,
    APPLICATION_ENTRY_POINT VARCHAR(255) NULL,
    CATEGORY                VARCHAR(64) NULL,
    DOMAIN                  VARCHAR(32) NULL,
    KEY                     VARCHAR(40) NULL,
    NAME                    VARCHAR(255) NULL,
    PARENT_ID               VARCHAR(40) NOT NULL,
    PARENT_KEY              VARCHAR(32) NOT NULL,
    PRIORITY                INT         NOT NULL,
    SERVICE_LEVEL           VARCHAR(255) NULL,
    TYPE                    VARCHAR(32),
    CUSTOM_1                VARCHAR(255) NULL,
    CUSTOM_2                VARCHAR(255) NULL,
    CUSTOM_3                VARCHAR(255) NULL,
    CUSTOM_4                VARCHAR(255) NULL,
    CUSTOM_5                VARCHAR(255) NULL,
    CUSTOM_6                VARCHAR(255) NULL,
    CUSTOM_7                VARCHAR(255) NULL,
    CUSTOM_8                VARCHAR(255) NULL,
    DETAILS                 TEXT NULL,
    PRIMARY KEY (ID)
);

CREATE TABLE CONFIGURATION
(
    NAME              VARCHAR(8) NOT NULL,
    ENFORCE_SECURITY  BOOLEAN NULL,
    CUSTOM_ATTRIBUTES TEXT NULL,
    PRIMARY KEY (NAME)
);

INSERT INTO CONFIGURATION (NAME)
VALUES ('MASTER');


CREATE TABLE USER_INFO
(
    USER_ID      VARCHAR(32) NOT NULL,
    FIRST_NAME   VARCHAR(32) NULL,
    LASTNAME     VARCHAR(32) NULL,
    FULL_NAME    VARCHAR(64) NULL,
    LONG_NAME    VARCHAR(64) NULL,
    E_MAIL       VARCHAR(64) NULL,
    PHONE        VARCHAR(32) NULL,
    MOBILE_PHONE VARCHAR(32) NULL,
    ORG_LEVEL_4  VARCHAR(32) NULL,
    ORG_LEVEL_3  VARCHAR(32) NULL,
    ORG_LEVEL_2  VARCHAR(32) NULL,
    ORG_LEVEL_1  VARCHAR(32) NULL,
    DATA         TEXT NULL,
    PRIMARY KEY (USER_ID)
);

CREATE TABLE GROUP_INFO
(
    USER_ID     VARCHAR(32) NOT NULL,
    GROUP_ID    VARCHAR(256) NOT NULL,
    PRIMARY KEY (USER_ID, GROUP_ID)
);

CREATE TABLE PERMISSION_INFO
(
    USER_ID     VARCHAR(32) NOT NULL,
    PERMISSION_ID    VARCHAR(256) NOT NULL,
    PRIMARY KEY (USER_ID, PERMISSION_ID)
);

CREATE SEQUENCE SCHEDULED_JOB_SEQ
    MINVALUE 1
    START WITH 1
    INCREMENT BY 1 CACHE 10;

CREATE INDEX IDX_CLASSIFICATION_ID ON CLASSIFICATION
    (ID ASC, CUSTOM_8, CUSTOM_7, CUSTOM_6, CUSTOM_5, CUSTOM_4,
     CUSTOM_3, CUSTOM_2, CUSTOM_1, APPLICATION_ENTRY_POINT,
     SERVICE_LEVEL, PRIORITY, DESCRIPTION, NAME, MODIFIED,
     CREATED, VALID_IN_DOMAIN, DOMAIN, TYPE, CATEGORY, PARENT_KEY,
     PARENT_ID, KEY);
COMMIT WORK;
CREATE INDEX IDX_CLASSIFICATION_CATEGORY ON CLASSIFICATION
    (CATEGORY ASC, DOMAIN ASC, TYPE ASC, CUSTOM_1
     ASC, CUSTOM_8 ASC, CUSTOM_7 ASC, CUSTOM_6 ASC,
     CUSTOM_5 ASC, CUSTOM_4 ASC, CUSTOM_3 ASC, CUSTOM_2
     ASC, APPLICATION_ENTRY_POINT ASC, SERVICE_LEVEL
     ASC, PRIORITY ASC, DESCRIPTION ASC, NAME ASC,
     CREATED ASC, VALID_IN_DOMAIN ASC, PARENT_KEY ASC, PARENT_ID
     ASC, KEY ASC, ID ASC);
COMMIT WORK;
CREATE INDEX IDX_CLASSIFICATION_KEY_DOMAIN ON CLASSIFICATION
    (KEY ASC, DOMAIN ASC, CUSTOM_8, CUSTOM_7, CUSTOM_6,
     CUSTOM_5, CUSTOM_4, CUSTOM_3, CUSTOM_2, CUSTOM_1,
     APPLICATION_ENTRY_POINT, SERVICE_LEVEL, PRIORITY,
     DESCRIPTION, NAME, CREATED, VALID_IN_DOMAIN,
     TYPE, CATEGORY, PARENT_KEY, PARENT_ID, ID);
COMMIT WORK;


 CREATE INDEX IDX_TASK_WORKBASKET_KEY_DOMAIN ON TASK
   (WORKBASKET_KEY ASC, DOMAIN DESC);
   COMMIT WORK ;
 CREATE INDEX IDX_TASK_LOWER_POR_VALUE ON TASK
   (LOWER(POR_VALUE) ASC, WORKBASKET_ID ASC);
   COMMIT WORK ;
 CREATE INDEX IDX_TASK_POR_VALUE ON TASK
   (POR_VALUE ASC, WORKBASKET_ID ASC);
   COMMIT WORK ;

CREATE INDEX IDX_ATTACHMENT_TASK_ID ON ATTACHMENT
    (TASK_ID ASC, RECEIVED ASC, CLASSIFICATION_ID
     ASC, CLASSIFICATION_KEY ASC, MODIFIED ASC, CREATED
     ASC, ID ASC);
COMMIT WORK;


CREATE INDEX IDX_WORKBASKET_ID ON WORKBASKET
    (ID ASC, ORG_LEVEL_4, ORG_LEVEL_3, ORG_LEVEL_2,
     ORG_LEVEL_1, OWNER, DESCRIPTION, TYPE, DOMAIN, NAME, KEY);
COMMIT WORK;
CREATE INDEX IDX_WORKBASKET_KEY_DOMAIN ON WORKBASKET
    (KEY ASC, DOMAIN ASC, ORG_LEVEL_4,
     ORG_LEVEL_3, ORG_LEVEL_2, ORG_LEVEL_1, CUSTOM_4,
     CUSTOM_3, CUSTOM_2, CUSTOM_1, OWNER, DESCRIPTION,
     TYPE, NAME, MODIFIED, CREATED, ID);
COMMIT WORK;
CREATE INDEX IDX_WORKBASKET_KEY_DOMAIN_ID ON WORKBASKET
    (KEY ASC, DOMAIN ASC, ID);
COMMIT WORK;


CREATE INDEX IDX_WORKBASKET_ACCESS_LIST_ACCESS_ID ON WORKBASKET_ACCESS_LIST
    (ACCESS_ID ASC, WORKBASKET_ID ASC, PERM_READ ASC);
COMMIT WORK;
CREATE INDEX IDX_WORKBASKET_ACCESS_LIST_WORKBASKET_ID ON WORKBASKET_ACCESS_LIST
    (WORKBASKET_ID ASC, PERM_CUSTOM_12 ASC, PERM_CUSTOM_11
     ASC, PERM_CUSTOM_10 ASC, PERM_CUSTOM_9 ASC, PERM_CUSTOM_8
     ASC, PERM_CUSTOM_7 ASC, PERM_CUSTOM_6 ASC, PERM_CUSTOM_5
     ASC, PERM_CUSTOM_4 ASC, PERM_CUSTOM_3 ASC, PERM_CUSTOM_2
     ASC, PERM_CUSTOM_1 ASC, PERM_DISTRIBUTE ASC, PERM_TRANSFER
     ASC, PERM_APPEND ASC, PERM_OPEN ASC, PERM_READ
     ASC, ACCESS_ID ASC);
COMMIT WORK;

CREATE INDEX IDX_OBJECT_REFERE_PK_ID ON OBJECT_REFERENCE
    (ID ASC);
COMMIT WORK ;
CREATE INDEX IDX_OBJECT_REFERE_FK_TASK_ID ON OBJECT_REFERENCE
    (TASK_ID ASC);
COMMIT WORK ;
CREATE INDEX IDX_OBJECT_REFERE_ACCESS_LIST ON OBJECT_REFERENCE
    (VALUE ASC, TYPE ASC, SYSTEM_INSTANCE ASC, SYSTEM ASC, COMPANY ASC, ID ASC);
COMMIT WORK ;
CREATE INDEX IDX_TASK_ID_HISTORY_EVENT ON TASK_HISTORY_EVENT
    (TASK_ID ASC);
COMMIT WORK ;
