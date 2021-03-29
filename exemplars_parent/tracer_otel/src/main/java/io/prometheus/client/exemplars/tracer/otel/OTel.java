package io.prometheus.client.exemplars.tracer.otel;

import io.opentelemetry.api.trace.Span;
import io.prometheus.client.exemplars.tracer.common.TraceIdSupplier;

public class OTel implements TraceIdSupplier {

  public static boolean isAvailable() {
    try {
      Span.current();
      return true;
    } catch (NoClassDefFoundError e) {
      return false;
    }
  }

  @Override
  public String getTraceId() {
    return Span.current().getSpanContext().getTraceId();
  }
}
