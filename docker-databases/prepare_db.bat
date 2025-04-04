@ECHO OFF
SETLOCAL

:MENU
    ECHO.
    ECHO -----------------------------------------------------
    ECHO PRESS a number to select your task - anything to EXIT.
    ECHO -----------------------------------------------------
    ECHO.
    ECHO 1 - Start DB2 11.5
    ECHO 2 - Stop  DB2 11.5
    ECHO.
    ECHO 3 - Start POSTGRES 14
    ECHO 4 - Stop  POSTGRES 14
    ECHO.
    ECHO 5 - Stop all
    ECHO.
    SET /P MENU=Select task then press ENTER:
    ECHO.
    IF [%MENU%]==[1] GOTO START_DB2_11_5
    IF [%MENU%]==[2] GOTO STOP_DB2_11_5
    IF [%MENU%]==[3] GOTO START_POSTGRES_14
    IF [%MENU%]==[4] GOTO STOP_POSTGRES_14
    IF [%MENU%]==[7] GOTO STOP_ALL
    EXIT /B

:START_DB2_11_5
    ECHO ---
    ECHO docker compose -f %~dp0/docker-compose.yml up -d kadai-db2_11-5
    docker compose -f %~dp0/docker-compose.yml up -d kadai-db2_11-5

    ECHO ---
    GOTO MENU

:STOP_DB2_11_5
    ECHO ---
    ECHO docker compose -f %~dp0/docker-compose.yml rm -f -s -v kadai-db2_11-5
    docker compose -f %~dp0/docker-compose.yml rm -f -s -v kadai-db2_11-5
    ECHO ---
    GOTO MENU

:START_POSTGRES_14
    ECHO ---
    ECHO docker compose -f %~dp0/docker-compose.yml up -d kadai-postgres_14
    docker compose -f %~dp0/docker-compose.yml up -d kadai-postgres_14

    ECHO ---
    GOTO MENU

:STOP_POSTGRES_14
    ECHO ---
    ECHO docker stop kadai-postgres_14
    ECHO docker compose -f %~dp0/docker-compose.yml rm -f -s -v kadai-postgres_14
    docker compose -f %~dp0/docker-compose.yml rm -f -s -v kadai-postgres_14
    ECHO ---
    GOTO MENU

:STOP_ALL
    ECHO ---
    ECHO docker compose -f %~dp0/docker-compose.yml down -v
    docker compose -f %~dp0/docker-compose.yml down -v
    ECHO ---
    GOTO MENU
