package io.prometheus.client.exemplars;

public interface SummaryExemplarSampler {
  Exemplar sample(double value);
}
