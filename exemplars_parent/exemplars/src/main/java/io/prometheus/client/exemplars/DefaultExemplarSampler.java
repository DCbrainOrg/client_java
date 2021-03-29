package io.prometheus.client.exemplars;

import io.prometheus.client.exemplars.tracer.common.TraceIdSupplier;

public class DefaultExemplarSampler
    implements CounterExemplarSampler, GaugeExemplarSampler, HistogramExemplarSampler, SummaryExemplarSampler {

  private final TraceIdSupplier traceIdSupplier;

  public DefaultExemplarSampler(TraceIdSupplier traceIdSupplier) {
    this.traceIdSupplier = traceIdSupplier;
  }

  @Override
  public Exemplar sample(double value, Exemplar previous) {
    if (previous == null) {
      String traceId = traceIdSupplier.getTraceId();
      if (traceId != null) {
        return new Exemplar(traceId, value);
      }
    }
    return null;
  }

  @Override
  public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
    return sample(value, previous);
  }

  @Override
  public Exemplar sample(double value) {
    return sample(value, null);
  }
}