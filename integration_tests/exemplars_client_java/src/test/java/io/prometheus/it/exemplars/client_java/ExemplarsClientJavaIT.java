package io.prometheus.it.exemplars.client_java;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;

public class ExemplarsClientJavaIT {

  private final OkHttpClient client = new OkHttpClient();

  private static class DockerContainer extends GenericContainer<DockerContainer> {
    DockerContainer() {
      super(new ImageFromDockerfile("exemplars-client_java-example-app")
          .withFileFromPath("exemplars_client_java.jar", Paths.get("target/exemplars_client_java.jar"))
          .withFileFromClasspath("Dockerfile", "Dockerfile"));
    }
  }

  @Rule
  public DockerContainer dockerContainer = new DockerContainer()
      .withExposedPorts(8080)
      .waitingFor(Wait.forLogMessage(".* Started .*", 1));

  @Test
  public void testExemplars() throws IOException {
    Request request = new Request.Builder()
        .url("http://localhost:" + dockerContainer.getMappedPort(8080) + "/hello")
        .build();
    try (Response response = client.newCall(request).execute()) {
      Assert.assertEquals("Hello, Prometheus!\n", response.body().string());
    }
    String logs = dockerContainer.getLogs();
    String regex = ".*LoggingSpanExporter - '/hello' : ([0-9a-f]+).*";
    Matcher matcher = Pattern.compile(regex, DOTALL).matcher(logs);
    Assert.assertTrue(logs + "\nERROR: Trace ID not found with regex: " + regex, matcher.matches());
    String traceId = matcher.group(1);

    System.out.println("TODO(1): Trace ID " + traceId + " should be as exemplar in the following metrics:");
    for (String m : getMetric("requests_total")) {
      System.out.println(m);
    }

    System.out.println("TODO(2): Trace ID " + traceId + " must not be as exemplar in the following metric:");
    for (String m : getMetric("process_cpu_seconds_total")) {
      System.out.println(m);
    }
  }

  private List<String> getMetric(String name) throws IOException {
    List<String> result = new ArrayList<>();
    Request request = new Request.Builder()
        .url("http://localhost:" + dockerContainer.getMappedPort(8080) + "/metrics")
        .build();
    try (Response response = client.newCall(request).execute()) {
      for (String line : response.body().string().split("\\n")) {
        if (line.contains(name) && ! line.startsWith("#")) {
          result.add(line);
        }
      }
    }
    return result;
  }
}
