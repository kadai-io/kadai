FROM maven:3.9.9-eclipse-temurin-21 AS application-build

WORKDIR /build

COPY . ./

RUN ./mvnw -B -pl :kadai-gmt -am package -DskipTests -Djacoco.skip -T 1C

FROM eclipse-temurin:21-jre-jammy

LABEL org.opencontainers.image.source="https://github.com/kadai-io/kadai"

RUN groupadd --system kadai && useradd --system --gid kadai --create-home --home-dir /app kadai

WORKDIR /app

COPY --from=application-build --chown=kadai:kadai /build/rest/kadai-gmt/target/kadai-gmt.jar ./

USER kadai

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/kadai-gmt.jar"]
