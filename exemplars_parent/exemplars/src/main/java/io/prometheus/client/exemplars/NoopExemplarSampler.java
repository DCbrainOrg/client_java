package io.prometheus.client.exemplars;

public class NoopExemplarSampler implements CounterExemplarSampler, GaugeExemplarSampler, HistogramExemplarSampler,
    SummaryExemplarSampler {

  @Override
  public Exemplar sample(double value, Exemplar previous) {
    return null;
  }

  @Override
  public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
    return null;
  }

  @Override
  public Exemplar sample(double value) {
    return null;
  }
}