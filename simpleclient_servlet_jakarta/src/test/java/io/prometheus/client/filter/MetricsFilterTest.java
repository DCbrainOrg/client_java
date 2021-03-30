package io.prometheus.client.filter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetricsFilterTest {
	MetricsFilter f = new MetricsFilter();

	@After
	public void clear() {
		CollectorRegistry.defaultRegistry.clear();
	}

	@Test
	public void init() throws Exception {
		FilterConfig cfg = mock(FilterConfig.class);
		when(cfg.getInitParameter(anyString())).thenReturn(null);

		String metricName = "foo";

		when(cfg.getInitParameter(MetricsFilter.METRIC_NAME_PARAM)).thenReturn(metricName);
		when(cfg.getInitParameter(MetricsFilter.PATH_COMPONENT_PARAM)).thenReturn("4");

		f.init(cfg);

		assertEquals(f.pathComponents, 4);

		HttpServletRequest req = mock(HttpServletRequest.class);

		when(req.getRequestURI()).thenReturn("/foo/bar/baz/bang/zilch/zip/nada");
		when(req.getMethod()).thenReturn(HttpMethod.GET.asString());

		HttpServletResponse res = mock(HttpServletResponse.class);
		FilterChain c = mock(FilterChain.class);

		f.doFilter(req, res, c);

		verify(c).doFilter(req, res);

		final Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue(
				metricName + "_count",
				new String[] { "path", "method" },
				new String[] { "/foo/bar/baz/bang", HttpMethod.GET.asString() });
		assertNotNull(sampleValue);
		assertEquals(1, sampleValue, 0.0001);
	}

	@Test
	public void doFilter() throws Exception {
		HttpServletRequest req = mock(HttpServletRequest.class);
		final String path = "/foo/bar/baz/bang/zilch/zip/nada";

		when(req.getRequestURI()).thenReturn(path);
		when(req.getMethod()).thenReturn(HttpMethod.GET.asString());

		HttpServletResponse res = mock(HttpServletResponse.class);
		FilterChain c = mock(FilterChain.class);

		String name = "foo";
		FilterConfig cfg = mock(FilterConfig.class);
		when(cfg.getInitParameter(MetricsFilter.METRIC_NAME_PARAM)).thenReturn(name);
		when(cfg.getInitParameter(MetricsFilter.PATH_COMPONENT_PARAM)).thenReturn("0");

		f.init(cfg);
		f.doFilter(req, res, c);

		verify(c).doFilter(req, res);

		final Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue(name + "_count",
				new String[] { "path", "method" },
				new String[] { path, HttpMethod.GET.asString() });
		assertNotNull(sampleValue);
		assertEquals(1, sampleValue, 0.0001);
	}

	@Test
	public void testConstructor() throws Exception {
		HttpServletRequest req = mock(HttpServletRequest.class);
		final String path = "/foo/bar/baz/bang";
		when(req.getRequestURI()).thenReturn(path);
		when(req.getMethod()).thenReturn(HttpMethod.POST.asString());

		FilterChain c = mock(FilterChain.class);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				Thread.sleep(100);
				return null;
			}
		}).when(c).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

		MetricsFilter constructed = new MetricsFilter("foobar_baz_filter_duration_seconds",
				"Help for my filter", 0, null);
		constructed.init(mock(FilterConfig.class));

		HttpServletResponse res = mock(HttpServletResponse.class);
		constructed.doFilter(req, res, c);

		final Double sum = CollectorRegistry.defaultRegistry.getSampleValue(
				"foobar_baz_filter_duration_seconds_sum",
				new String[] { "path", "method" },
				new String[] { path, HttpMethod.POST.asString() });
		assertNotNull(sum);
		assertEquals(0.1, sum, 0.01);
	}

	@Test
	public void testBucketsAndName() throws Exception {
		HttpServletRequest req = mock(HttpServletRequest.class);
		final String path = "/foo/bar/baz/bang";
		when(req.getRequestURI()).thenReturn(path);
		when(req.getMethod()).thenReturn(HttpMethod.POST.asString());

		FilterChain c = mock(FilterChain.class);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				Thread.sleep(100);
				return null;
			}
		}).when(c).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

		final String buckets = "0.01,0.05,0.1,0.15,0.25";
		FilterConfig cfg = mock(FilterConfig.class);
		when(cfg.getInitParameter(MetricsFilter.BUCKET_CONFIG_PARAM)).thenReturn(buckets);
		when(cfg.getInitParameter(MetricsFilter.METRIC_NAME_PARAM)).thenReturn("foo");

		HttpServletResponse res = mock(HttpServletResponse.class);

		f.init(cfg);

		f.doFilter(req, res, c);

		final Double sum = CollectorRegistry.defaultRegistry.getSampleValue("foo_sum",
				new String[] { "path", "method" },
				new String[] { "/foo", HttpMethod.POST.asString() });
		assertEquals(0.1, sum, 0.01);

		final Double le05 = CollectorRegistry.defaultRegistry.getSampleValue("foo_bucket",
				new String[] { "path", "method", "le" },
				new String[] { "/foo", HttpMethod.POST.asString(), "0.05" });
		assertNotNull(le05);
		assertEquals(0, le05, 0.01);
		final Double le15 = CollectorRegistry.defaultRegistry.getSampleValue("foo_bucket",
				new String[] { "path", "method", "le" },
				new String[] { "/foo", HttpMethod.POST.asString(), "0.15" });
		assertNotNull(le15);
		assertEquals(1, le15, 0.01);

		final Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry
				.metricFamilySamples();
		Collector.MetricFamilySamples sample = null;
		while (samples.hasMoreElements()) {
			sample = samples.nextElement();
			if (sample.name.equals("foo")) {
				break;
			}
		}

		assertNotNull(sample);

		int count = 0;
		for (Collector.MetricFamilySamples.Sample s : sample.samples) {
			if (s.name.equals("foo_bucket")) {
				count++;
			}
		}
		// +1 because of the final le=+infinity bucket
		assertEquals(buckets.split(",").length + 1, count);
	}

	@Test
	public void testStatusCode() throws Exception {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getRequestURI()).thenReturn("/foo/bar/baz/bang");
		when(req.getMethod()).thenReturn(HttpMethod.GET.asString());

		HttpServletResponse res = mock(HttpServletResponse.class);
		when(res.getStatus()).thenReturn(200);

		FilterChain c = mock(FilterChain.class);

		MetricsFilter constructed = new MetricsFilter("foobar_filter", "Help for my filter", 2,
				null);
		constructed.init(mock(FilterConfig.class));

		constructed.doFilter(req, res, c);

		final Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue(
				"foobar_filter_status_total",
				new String[] { "path", "method", "status" },
				new String[] { "/foo/bar", HttpMethod.GET.asString(), "200" });
		assertNotNull(sampleValue);
		assertEquals(1, sampleValue, 0.0001);
	}

	@Test
	public void testStatusCodeWithNonHttpServletResponse() throws Exception {
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getRequestURI()).thenReturn("/foo/bar/baz/bang");
		when(req.getMethod()).thenReturn(HttpMethod.GET.asString());

		ServletResponse res = mock(ServletResponse.class);

		FilterChain c = mock(FilterChain.class);

		MetricsFilter constructed = new MetricsFilter("foobar_filter", "Help for my filter", 2,
				null);
		constructed.init(mock(FilterConfig.class));

		constructed.doFilter(req, res, c);

		final Double sampleValue = CollectorRegistry.defaultRegistry.getSampleValue(
				"foobar_filter_status_total",
				new String[] { "path", "method", "status" },
				new String[] {
						"/foo/bar",
						HttpMethod.GET.asString(),
						MetricsFilter.UNKNOWN_HTTP_STATUS_CODE });
		assertNotNull(sampleValue);
		assertEquals(1, sampleValue, 0.0001);
	}
}
