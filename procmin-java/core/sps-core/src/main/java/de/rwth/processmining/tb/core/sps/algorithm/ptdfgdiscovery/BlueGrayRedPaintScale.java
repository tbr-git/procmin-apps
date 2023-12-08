package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery;

import java.awt.Color;
import java.awt.Paint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.data.Range;

public class BlueGrayRedPaintScale implements RangeAdjustablePaintScale {
	
	private final Logger logger = LogManager.getLogger( BlueGrayRedPaintScale.class );

    private Range range;

    public BlueGrayRedPaintScale(Range r) {
        this.range = r;
    }
    
    public void setRange(Range r) {
    	this.range = r;
    }

    @Override
    public double getLowerBound() {
        return range.getLowerBound();
    }

    @Override
    public double getUpperBound() {
        return range.getUpperBound();
    }

    @Override
    public Paint getPaint(double value) {
    	if(value < 0) {
    		return Color.getHSBColor(0.666f, (float) Math.min(1, value / getLowerBound()) , 0.7f);
    	}
    	else {
    		return Color.getHSBColor(0.0f, (float) Math.min(1, value / getUpperBound()) , 0.7f);
    	}
    }
}