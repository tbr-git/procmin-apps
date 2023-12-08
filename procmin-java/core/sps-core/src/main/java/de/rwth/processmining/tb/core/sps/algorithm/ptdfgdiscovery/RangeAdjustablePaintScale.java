package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery;

import org.jfree.chart.renderer.PaintScale;
import org.jfree.data.Range;

public interface RangeAdjustablePaintScale extends PaintScale {

    public void setRange(Range r);
}
