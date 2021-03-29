FROM openjdk:11-jre
RUN mkdir -p /app/
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.0.1/opentelemetry-javaagent-all.jar /app/
COPY exemplars_client_java.jar /app/
CMD java \
        -Dotel.traces.exporter=logging \
        -Dotel.metrics.exporter=none \
        -javaagent:/app/opentelemetry-javaagent-all.jar \
        -jar \
        /app/exemplars_client_java.jar
