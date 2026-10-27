FROM mirror.ccs.tencentyun.com/library/eclipse-temurin:21-jre
WORKDIR /app
COPY easy-query-web/target/easy-query-web.jar ./
ENTRYPOINT ["/bin/sh", "-c", "java ${JAVA_OPTS} -jar /app/easy-query-web.jar"]
