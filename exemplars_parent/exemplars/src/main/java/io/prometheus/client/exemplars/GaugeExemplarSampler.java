package io.prometheus.client.exemplars;

public interface GaugeExemplarSampler {

  /**
   *
   * @param previous if an exemplar has already been observed since the last scrape,
   *                 it is passed here. In that case returning {@code null} means
   *                 "keep the previous exemplar", i.e. returning {@code null} is equivalent to returning {@code previous}.
   *                 If this is the first call since the last scrape, {@code previous} is {@code null}.
   * @return
   */
  Exemplar sample(double value, Exemplar previous);
}
