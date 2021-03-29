package io.prometheus.client.exemplars.tracer.common;

public class NoopTraceIdSupplier implements TraceIdSupplier {

  @Override
  public String getTraceId() {
    return null;
  }
}
