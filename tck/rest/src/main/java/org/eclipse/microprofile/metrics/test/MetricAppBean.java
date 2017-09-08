package org.eclipse.microprofile.metrics.test;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnit;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

@ApplicationScoped
public class MetricAppBean {

	@Inject
	@Metric
	Counter redCount;

	@Inject
	@Metric(name="blue")
	Counter blueCount;

	@Inject
	@Metric(absolute=true)
	Counter greenCount;

	@Inject
	@Metric(name="purple", absolute=true)
	Counter purpleCount;
	
	@Inject
	//@RegistryType(type=MetricRegistry.Type.BASE)
	MetricRegistry metrics;
	
	public void countMe() {	
		Counter counter = metrics.counter("metricTest.test1.count");
		counter.inc();		
	}
	
	@Counted(name="metricTest.test1.countMeA",monotonic=true,absolute=true)
	public void countMeA() {
		
	}

	public void gaugeMe() {
		
		@SuppressWarnings("unchecked")
		Gauge<Long> gauge = metrics.getGauges().get("metricTest.test1.gauge");
		if (gauge == null) {
			gauge = () -> { return 19L; }; 
			metrics.register("metricTest.test1.gauge", gauge);
		}
	
	}

	@org.eclipse.microprofile.metrics.annotation.Gauge(unit=MetricUnit.KIBIBITS)
	public long gaugeMeA() {
		return 1000L;	
	}

	
	public void histogramMe() {
		
		Histogram histogram = metrics.histogram("metricTest.test1.histogram");
		
		for (int i=0; i<1000; i++)
			histogram.update(i);
		
	}


	public void meterMe() {
		
		Meter meter = metrics.meter("metricTest.test1.meter");
		meter.mark();

	}

	@Metered(absolute=true)
	public void meterMeA() {
		
	}
	

	public void timeMe() {
	
		Timer timer = metrics.timer("metricTest.test1.timer");
		
		Timer.Context context = timer.time();
		try {
			Thread.sleep((long) (Math.random()*1000));
		}
		catch (InterruptedException e) {}
		finally {
			context.stop();
		}
		
	}

	@Timed
	public void timeMeA() {
		
	}
	
}
