package io.prometheus.it.exemplars.client_java;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@SpringBootApplication
@RestController
public class ExemplarsClientJavaExampleApplication {

  private final OkHttpClient client = new OkHttpClient();
  private final Counter requestCounter = Counter.build()
      .name("requests_total")
      .help("Total number of requests.")
      .labelNames("path")
      .register();

  public static void main(String[] args) {
    DefaultExports.initialize();
    SpringApplication.run(ExemplarsClientJavaExampleApplication.class, args);
  }

  @GetMapping("/hello")
  public String hello() throws IOException {
    requestCounter.labels("/hello").inc();
    Request request = new Request.Builder()
        .url("http://localhost:8080/god-of-fire")
        .build();
    try (Response response = client.newCall(request).execute()) {
      return "Hello, " + response.body().string() + "!\n";
    }
  }

  @GetMapping("/god-of-fire")
  public String godOfFire() {
    requestCounter.labels("/god-of-fire").inc();
    return "Prometheus";
  }

  @Bean
  public ServletRegistrationBean<MetricsServlet> metricsServlet() {
    ServletRegistrationBean<MetricsServlet> bean = new ServletRegistrationBean<>(new MetricsServlet(), "/metrics");
    bean.setLoadOnStartup(1);
    return bean;
  }
}
