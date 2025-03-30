#!/bin/bash
set -e #fail fast

#check that first dir has same amount of files as second dir
function verifyDocs() {
  test -d "$1"
  test "$(find "$1" | wc -l)" -eq "$(grep -c "$2" <<<"$JAR_CONTENT")"
}

REL=$(dirname "$0")
JAR_FILE_LOCATION="$REL/../rest/kadai-rest-spring-example-boot/target/kadai-rest-spring-example-boot.jar"
set -x
test -e "$JAR_FILE_LOCATION"
set +x
JAR_CONTENT=$(jar -tf "$JAR_FILE_LOCATION")
set -x

verifyDocs "$REL/../lib/kadai-core/target/reports/apidocs" "/static/docs/java/kadai-core"
verifyDocs "$REL/../lib/kadai-spring/target/reports/apidocs" "/static/docs/java/kadai-spring"
test -n "$(jar -tf "$JAR_FILE_LOCATION" | grep /static/docs/java/kadai-core/index.html)"
test -n "$(jar -tf "$JAR_FILE_LOCATION" | grep /static/docs/java/kadai-spring/index.html)"
set +x
echo "the jar file '$JAR_FILE_LOCATION' contains documentation"
