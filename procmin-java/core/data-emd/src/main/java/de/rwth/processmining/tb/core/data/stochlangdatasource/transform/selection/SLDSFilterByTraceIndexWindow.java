package de.rwth.processmining.tb.core.data.stochlangdatasource.transform.selection;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.utils.XLogBuilder;

import de.rwth.processmining.tb.core.data.stochlangdatasource.StochasticLanguageDataSource;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.SLDSTransformationError;
import de.rwth.processmining.tb.core.data.stochlangdatasource.transform.StochasticLangDataSourceTransformer;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariant;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLog;
import de.rwth.processmining.tb.core.data.variantlog.base.CVariantLogFactory;
import de.rwth.processmining.tb.core.data.variantlog.util.LogBuildingException;

/**
 * Extracts a window from the log.
 * 
 * The window is specified by a trace index and a window size as well as if the window should
 * be extracted to the left of the index or to the right.
 * 
 * Extraction to the left: Extracts window size many traces to the left <b>excluding</b> trace index.
 * Extraction to the right: Extracts window size many traces to the left <b>including</b> trace index.
 * 
 * 
 * @author brockhoff
 *
 * @param <E>
 */
public class SLDSFilterByTraceIndexWindow<E extends CVariant> extends StochasticLangDataSourceTransformer<E> {
	private final static Logger logger = LogManager.getLogger( SLDSFilterByTraceIndexWindow.class );

	/**
	 * Trace index of window center
	 */
	private final int traceIndex;
	
	/**
	 * Window size
	 */
	private final int windowSize;

	/**
	 * Log name
	 */
	private final String logName;
	
	/**
	 * Extract log to the left of trace index
	 */
	private final boolean extractLeft;


	public SLDSFilterByTraceIndexWindow(StochasticLanguageDataSource<E> stochLangDataSource, int traceIndex, int windowSize,
			String logName, boolean extractLeft) {
		super(stochLangDataSource);
		this.traceIndex = traceIndex;
		this.windowSize = windowSize;
		this.logName = logName;
		this.extractLeft = extractLeft;
	}
	
	private XLog extractWindow(XLog log) throws SLDSTransformationError {
		if(extractLeft && traceIndex - windowSize < 0) {
			logger.error("Window too big left");
			throw new SLDSTransformationError("Window extraction failed.");
		}
		else if(!extractLeft && traceIndex + windowSize > log.size()) {
			logger.error("Window too big right");
			throw new SLDSTransformationError("Window extraction failed.");
		}
		else {
			XLog xlogWindow = XLogBuilder.newInstance().startLog(logName).build();

			Stream<XTrace> stream = null;
			if(extractLeft) {
				// Trace index exclusive
				stream = IntStream.range(traceIndex - windowSize, traceIndex).mapToObj(log::get);
			}
			else {
				// Trace index inclusive
				stream = IntStream.range(traceIndex, traceIndex + windowSize).mapToObj(log::get);
			}
			
			xlogWindow.addAll(stream.collect(Collectors.toList()));
			
			xlogWindow.getClassifiers().addAll(log.getClassifiers());
			return xlogWindow;
		}
		
	}

	@Override
	public XLog getDataRawTransformed() throws SLDSTransformationError {
		XLog log = super.getDataRawTransformed();
		XLog logFiltered = extractWindow(log);
		return logFiltered;
	}

	@Override
	public CVariantLog<E> getVariantLog() throws SLDSTransformationError {
		XLog logFiltered = this.getDataRawTransformed();
		CVariantLogFactory<E> factory = super.getVariantLogFactory();
		factory.setLog(logFiltered);
		CVariantLog<E> variantLog;
		try {
			variantLog = factory.build();
		} catch (LogBuildingException e) {
			throw new SLDSTransformationError("Log building during window creation at trace index filter step failed:\n" + e.getMessage());
		}
		return variantLog;
	}

}
