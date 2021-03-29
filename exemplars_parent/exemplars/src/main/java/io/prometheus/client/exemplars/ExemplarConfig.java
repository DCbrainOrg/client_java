package io.prometheus.client.exemplars;

import io.prometheus.client.exemplars.tracer.common.NoopTraceIdSupplier;
import io.prometheus.client.exemplars.tracer.common.TraceIdSupplier;
import io.prometheus.client.exemplars.tracer.otel.OTel;
import io.prometheus.client.exemplars.tracer.otel_agent.OTelAgent;

public class ExemplarConfig {

  private static final ExemplarConfig instance = new ExemplarConfig();

  private final NoopExemplarSampler noopExemplarSampler = new NoopExemplarSampler();
  private volatile HistogramExemplarSampler defaultHistogramExemplarSampler;
  private volatile SummaryExemplarSampler defaultSummaryExemplarSampler;
  private volatile GaugeExemplarSampler defaultGaugeExemplarSampler;
  private volatile CounterExemplarSampler defaultCounterExemplarSampler;

  private ExemplarConfig() {
    DefaultExemplarSampler defaultExemplars = new DefaultExemplarSampler(findTraceIdSupplier());
    defaultCounterExemplarSampler = defaultExemplars;
    defaultGaugeExemplarSampler = defaultExemplars;
    defaultHistogramExemplarSampler = defaultExemplars;
    defaultSummaryExemplarSampler = defaultExemplars;
  }

  public void disableExemplars() {
    defaultCounterExemplarSampler = noopExemplarSampler;
    defaultGaugeExemplarSampler = noopExemplarSampler;
    defaultHistogramExemplarSampler = noopExemplarSampler;
    defaultSummaryExemplarSampler = noopExemplarSampler;
  }

  public static HistogramExemplarSampler getDefaultHistogramExemplarSampler() {
    return instance.defaultHistogramExemplarSampler;
  }

  public static void setDefaultHistogramExemplarSampler(HistogramExemplarSampler defaultHistogramExemplarSampler) {
    if (defaultHistogramExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.defaultHistogramExemplarSampler = defaultHistogramExemplarSampler;
  }

  public static SummaryExemplarSampler getDefaultSummaryExemplarSampler() {
    return instance.defaultSummaryExemplarSampler;
  }

  public static void setDefaultSummaryExemplarSampler(SummaryExemplarSampler defaultSummaryExemplarSampler) {
    if (defaultSummaryExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.defaultSummaryExemplarSampler = defaultSummaryExemplarSampler;
  }

  public static GaugeExemplarSampler getDefaultGaugeExemplars() {
    return instance.defaultGaugeExemplarSampler;
  }

  public static void setDefaultGaugeExemplarSampler(GaugeExemplarSampler defaultGaugeExemplarSampler) {
    if (defaultGaugeExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.defaultGaugeExemplarSampler = defaultGaugeExemplarSampler;
  }

  public static CounterExemplarSampler getDefaultCounterExemplarSampler() {
    return instance.defaultCounterExemplarSampler;
  }

  public static void setDefaultCounterExemplarSampler(CounterExemplarSampler defaultCounterExemplarSampler) {
    if (defaultCounterExemplarSampler == null) {
      throw new NullPointerException();
    }
    instance.defaultCounterExemplarSampler = defaultCounterExemplarSampler;
  }

  public static NoopExemplarSampler getNoopExemplarSampler() {
    return instance.noopExemplarSampler;
  }

  private static TraceIdSupplier findTraceIdSupplier() {
    try {
      if (OTel.isAvailable()) {
        return new OTel();
      }
      if (OTelAgent.isAvailable()) {
        return new OTelAgent();
      }
    } catch (UnsupportedClassVersionError ignored) {
      // OpenTelemetry requires Java 8, but client_java might be run in Java 6.
    }
    return new NoopTraceIdSupplier();
  }
}
