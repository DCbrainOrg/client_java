package io.prometheus.client.exemplars.tracer.common;

import java.util.List;

public interface TraceIdSupplier {
  String getTraceId();
}