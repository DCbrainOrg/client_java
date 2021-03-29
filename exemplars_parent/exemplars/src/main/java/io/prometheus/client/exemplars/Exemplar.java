package io.prometheus.client.exemplars;

public class Exemplar {

  private final String traceId;
  private final double value;
  private final long timestamp;

  public Exemplar(String traceId, double value, long timestamp) {
    this.traceId = traceId;
    this.value = value;
    this.timestamp = timestamp;
  }

  public Exemplar(String traceId, double value) {
    this(traceId, value, System.currentTimeMillis() / 1000L);
  }
}
