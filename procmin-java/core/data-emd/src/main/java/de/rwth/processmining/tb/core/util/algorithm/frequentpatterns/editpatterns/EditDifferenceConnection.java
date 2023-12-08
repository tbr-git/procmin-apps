package de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.editpatterns;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.BiMap;

import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.prefixspan.SequenceDatabase;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.prefixspan.SequentialPattern;
import de.rwth.processmining.tb.core.util.algorithm.frequentpatterns.prefixspan.SequentialPatterns;

public class EditDifferenceConnection {
	private final static Logger logger = LogManager.getLogger( EditDifferenceConnection.class );
	
	private final BiMap<String, Integer> op2int;

	private Map<String, LVSEditOpTriple> op2Repr;	

	private final SequenceDatabase seqDatabase;
	
	
	public EditDifferenceConnection(BiMap<String, Integer> op2int, Map<String, LVSEditOpTriple> op2Repr,
			SequenceDatabase seqDatabase) {
		super();
		this.op2int = op2int;
		this.op2Repr = op2Repr;
		this.seqDatabase = seqDatabase;
	}


	public SequenceDatabase getSeqDatabase() {
		return seqDatabase;
	}
	
	public List<FreqEditOpReprSequence> getFreqEditSequences(SequentialPatterns patterns) {
		List<FreqEditOpReprSequence> l = new LinkedList<>();
		
		for(List<SequentialPattern> lLevel : patterns.getLevels()) {
			for(SequentialPattern p : lLevel) {
				l.add(convertPatternToEditSequence(p).get());
			}
		}
		return l;
		
	}
	
	private Optional<FreqEditOpReprSequence> convertPatternToEditSequence(SequentialPattern pattern) {
		
		List<LVSEditOpTriple> l = new LinkedList<>();
		
		for(int id : pattern.getItems()) {
			String desc = op2int.inverse().get(id);
			if(desc == null) {
				logger.error("No inverse mapping found for id {}",  id);
			}
			else {
				l.add(op2Repr.get(desc));
			}
		}
		if(l.size() == 0) {
			logger.error("Trying to construct empty Freq Edit operation");
			return Optional.empty();
		}
		else {
			return Optional.of(new FreqEditOpReprSequence(l, pattern.getAbsoluteWeight()));
		}
	}


}
