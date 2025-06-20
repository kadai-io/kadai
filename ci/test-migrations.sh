#!/bin/bash
set -euo pipefail

# Helper to download and extract schema from a Maven artifact
# Args: groupId artifactId version schema_path output_file
function extract_schema_from_jar() {
  local groupId="$1"
  local artifactId="$2"
  local version="$3"
  local schema_path="$4"
  local output_file="$5"
  local group_path=$(echo $groupId | tr '.' '/')
  local jar_file="$HOME/.m2/repository/$group_path/$artifactId/$version/$artifactId-$version.jar"

  # Download the artifact if not present
  if [ ! -f "$jar_file" ]; then
    mvn dependency:get -DgroupId=$groupId -DartifactId=$artifactId -Dversion=$version -Dtransitive=false
  fi

  # Extract the schema SQL
  unzip -p "$jar_file" "$schema_path" > "$output_file"
}

# --- PostgreSQL ---
POSTGRES_CONTAINER=pg-migration-test
POSTGRES_USER=postgres
POSTGRES_DB=kadai
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5433

# Start PostgreSQL
(docker rm -f $POSTGRES_CONTAINER 2>/dev/null || true)
docker run --name $POSTGRES_CONTAINER -e POSTGRES_USER=$POSTGRES_USER -e POSTGRES_PASSWORD=$POSTGRES_PASSWORD -e POSTGRES_DB=$POSTGRES_DB -p $POSTGRES_PORT:5432 -d postgres:14

# Wait for PostgreSQL to be ready
until docker exec $POSTGRES_CONTAINER pg_isready -U $POSTGRES_USER; do sleep 1; done

pushd common/kadai-common/src/main/resources/sql/postgres > /dev/null
for migration in $(ls *_schema_update*.sql | sort); do
  # Parse migration filename: kadai_9.3.0_to_kadai_10.0.0_schema_update_postgres.sql
  # or taskana_8.2.0_to_kadai_9.0.0_schema_update.sql
  prev_version=$(echo $migration | sed -E 's/.*_([0-9]+\.[0-9]+\.[0-9]+)_to_.*/\1/')
  if [[ $migration == taskana* ]]; then
    groupId="pro.taskana"
    artifactId="taskana-common"
    schema_path="sql/postgres/taskana-schema-postgres.sql"
  else
    groupId="io.kadai"
    artifactId="kadai-common"
    schema_path="sql/postgres/kadai-schema-postgres.sql"
  fi
  echo "Testing migration $migration from $groupId:$artifactId:$prev_version ($schema_path)"
  tmp_schema="/tmp/base_schema_postgres.sql"
  extract_schema_from_jar "$groupId" "$artifactId" "$prev_version" "$schema_path" "$tmp_schema"

  # Recreate DB
  docker exec -i $POSTGRES_CONTAINER psql -U $POSTGRES_USER -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
  cat "$tmp_schema" | docker exec -i $POSTGRES_CONTAINER psql -U $POSTGRES_USER -d $POSTGRES_DB
  cat "$migration" | docker exec -i $POSTGRES_CONTAINER psql -U $POSTGRES_USER -d $POSTGRES_DB
  # re-apply for idempotency
  cat "$migration" | docker exec -i $POSTGRES_CONTAINER psql -U $POSTGRES_USER -d $POSTGRES_DB
done
popd > /dev/null
docker rm -f $POSTGRES_CONTAINER

# --- DB2 ---
DB2_CONTAINER=db2-migration-test
DB2_PORT=50001
DB2_USER=db2inst1
DB2_PASSWORD=passw0rd
DB2_DBNAME=kadai

# Build and start DB2 container
(docker rm -f $DB2_CONTAINER 2>/dev/null || true)
docker build -t kadai-db2-test docker-databases/db2_11-5

docker run --name $DB2_CONTAINER -e DB2INST1_PASSWORD=$DB2_PASSWORD -e LICENSE=accept -p $DB2_PORT:50000 -d kadai-db2-test

# Wait for DB2 to be ready
until docker exec $DB2_CONTAINER su - $DB2_USER -c "db2 connect to $DB2_DBNAME"; do sleep 5; done

pushd common/kadai-common/src/main/resources/sql/db2 > /dev/null
for migration in $(ls *_schema_update*.sql | sort); do
  prev_version=$(echo $migration | sed -E 's/.*_([0-9]+\.[0-9]+\.[0-9]+)_to_.*/\1/')
  if [[ $migration == taskana* ]]; then
    groupId="pro.taskana"
    artifactId="taskana-common"
    schema_path="sql/db2/taskana-schema-db2.sql"
  else
    groupId="io.kadai"
    artifactId="kadai-common"
    schema_path="sql/db2/kadai-schema-db2.sql"
  fi
  echo "Testing migration $migration from $groupId:$artifactId:$prev_version ($schema_path)"
  tmp_schema="/tmp/base_schema_db2.sql"
  extract_schema_from_jar "$groupId" "$artifactId" "$prev_version" "$schema_path" "$tmp_schema"

  # Recreate DB (drop all objects)
  docker exec -i $DB2_CONTAINER su - $DB2_USER -c "db2 -tvf -" <<EOF
CONNECT TO $DB2_DBNAME;
-- Drop all tables
BEGIN
  FOR v AS SELECT tabname FROM syscat.tables WHERE tabschema = 'DB2INST1' DO
    EXECUTE IMMEDIATE 'DROP TABLE DB2INST1.' || v.tabname || ' CASCADE';
  END FOR;
END;
/
EOF
  docker exec -i $DB2_CONTAINER su - $DB2_USER -c "db2 -tvf -" < "$tmp_schema"
  docker exec -i $DB2_CONTAINER su - $DB2_USER -c "db2 -tvf -" < "$migration"
  # re-apply for idempotency
  docker exec -i $DB2_CONTAINER su - $DB2_USER -c "db2 -tvf -" < "$migration"
done
popd > /dev/null
docker rm -f $DB2_CONTAINER

echo "Migration tests completed successfully." 