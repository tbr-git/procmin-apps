package de.rwth.processmining.tb.core.data.variantlog.base;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantCondition;
import de.rwth.processmining.tb.core.data.variantlog.transform.CVariantTransformer;
import de.rwth.processmining.tb.core.data.variantlog.util.CVariantUtil;
import de.rwth.processmining.tb.core.data.variantlog.util.VariantCopyingException;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;

public class CVariantLogImpl<T extends CVariant> implements CVariantLog<T> {
	private final static Logger logger = LogManager.getLogger( CVariantLogImpl.class );

	//TODO Make Optional
	protected final XEventClassifier classifier;
	
	protected final ArrayList<T> variants;
	
	private final CategoryMapper categoryMapper;
	
	protected int size;
	
	public CVariantLogImpl(XEventClassifier classifier, CategoryMapper categoryMapper, ArrayList<T> variants) {
		super();
		this.classifier = classifier;
		this.variants = variants;
		this.categoryMapper = categoryMapper;
		int sizeTmp = 0;
		for(T variant: variants) {
			sizeTmp += variant.getSupport();
		}
		this.size = sizeTmp;
	}

	public CVariantLogImpl(XEventClassifier classifier, CategoryMapper categoryMapper, ArrayList<T> variants, int size) {
		super();
		this.classifier = classifier;
		this.variants = variants;
		this.categoryMapper = categoryMapper;
		this.size = size;
	}
	
	@Override
	public Iterator<T> iterator() {
		return this.variants.iterator();
	}

	@Override
	public CVariantLogImpl<T> project(Collection<String> eventClasses) {
		// Create BitSet containing the integer ids of activities on which we want to project
		BitSet classIds = new BitSet(this.categoryMapper.getMaxCategoryCode());
		for (String c: eventClasses) {
			classIds.set(this.categoryMapper.getCategory4Activity(c));
		}
		return this.project(classIds);
	}
	
	@Override
	public CVariantLogImpl<T> project(BitSet eventClasses) {
		ArrayList<T> filteredVariants = getProjectedVariants(eventClasses);
		return new CVariantLogImpl<T>(this.classifier, this.categoryMapper, filteredVariants);
	}
	
	protected ArrayList<T> getProjectedVariants(BitSet classIds) {
		// Hashmap to merge variants that become equal after projection 
		// Each variant is mapped to a counter
		TObjectIntHashMap<T> projectedVariants = new TObjectIntHashMap<>();

		
		// Project each variant and detect duplicates
		for(T v : this.variants) {
			// In our implementation we assume that every implementation of projectOnCategories overrides the 
			// return type with itself -> otherwise, we get a complicated generics structure.
			@SuppressWarnings("unchecked")
			T vProjected = (T) v.projectOnCategories(classIds);
			if(vProjected != null) {
				projectedVariants.adjustOrPutValue(vProjected, vProjected.getSupport(), vProjected.getSupport());
			}
		}

		// Create new variants array from projection hash map
		ArrayList<T> filteredVariants = new ArrayList<>(projectedVariants.size());
		for ( TObjectIntIterator<T> it = projectedVariants.iterator(); it.hasNext(); ) {
			it.advance();
			T v = it.key();
			v.setSupport(it.value());
			filteredVariants.add(v);
		}
		return filteredVariants;
	}

	@Override
	public CVariantLogImpl<T> filterTracesMandatoryActivities(Collection<String> eventClasses) throws VariantCopyingException {
		// Array containing the categorical codes of the mandatory activities
		int[] eventClassesIds = new int[eventClasses.size()];
		int i = 0;
		// Populate category array
		for(String c: eventClasses) {
			eventClassesIds[i] = this.categoryMapper.getCategory4Activity(c);
			i++;
		}
		
		return this.filterTracesMandatoryActivities(eventClassesIds);
	}

	@Override
	public CVariantLogImpl<T> filterTracesMandatoryActivities(int[] eventClasses) throws VariantCopyingException {
		ArrayList<T> filteredVariants = getFilteredMandatoryActivityTraces(eventClasses, true);
		return new CVariantLogImpl<T>(this.classifier, this.categoryMapper, filteredVariants);
	}
	
	@Override
	public Collection<T> getTracesMandatoryActivities(int[] eventClasses) throws VariantCopyingException {
		ArrayList<T> filteredVariants = getFilteredMandatoryActivityTraces(eventClasses, false);
		return filteredVariants;
	}
	
	protected ArrayList<T> getFilteredMandatoryActivityTraces(int[] eventClassesIds, boolean copy) throws VariantCopyingException {
		List<T> lSatisfyingVariants = new LinkedList<>();
		
		// Check if variant contains all mandatory categorical codes
		for(T v: this.variants) {
			if(v.containsAllCategories(eventClassesIds)) {
				// In our implementation we assume that every implementation of projectOnCategories overrides the 
				// return type with itself -> otherwise, we get a complicated generics structure.
				if (copy) {
					lSatisfyingVariants.add(CVariantUtil.copyVariant(v));
				}
				else {
					lSatisfyingVariants.add(v);
				}
			}
		}
		// Create new log from variants that meet the activity requirement
		ArrayList<T> filteredVariants = new ArrayList<>(lSatisfyingVariants.size());
		filteredVariants.addAll(lSatisfyingVariants);
		return filteredVariants;
	}

	@Override
	public XEventClassifier getClassifier() {
		return this.classifier;
	}

	@Override
	public int nbrVariants() {
		return this.variants.size();
	}

	@Override
	public int getNbrActivityClasses() {
		return categoryMapper.getCategoryCount();
	}

	@Override
	public int sizeLog() {
		return this.size;
	}

	@Override
	public boolean contains(Object variant) {
		T res = this.get(variant);
		return res != null;
	}

	@Override
	public T get(Object variant) {
		T res = null;
		for(T variantLog : this.variants) {
			if(variantLog.equals(variant)) {
				res = variantLog;
				break;
			}
		}
		return res;
	}


	@Override
	public int getMaxCategoryCode() {
		return categoryMapper.getMaxCategoryCode();
	}

	@Override
	public CVariantLogImpl<T> applyVariantTransformer(CVariantTransformer<T> variantTransformer, boolean inplace) {
		
		if(variantTransformer.requiresDuplicateDetection()) {
			// Hashmap to merge variants that become equal after transformation 
			// Each variant is mapped to a counter
			TObjectIntHashMap<T> projectedVariants = new TObjectIntHashMap<>();

			// Transform each variant and detect duplicates
			for(T v : this.variants) {
				T vTransformed = variantTransformer.apply(v, false);
				// Increase variant counter w.r.t. transformed variant
				if(vTransformed != null) {
					projectedVariants.adjustOrPutValue(vTransformed, 
							vTransformed.getSupport(), vTransformed.getSupport());
				}
			}

			// Create new variants array from projection hash map
			// If inplace, these variants we directly insert them into the 
			// cleared array that backs THIS variant log
			ArrayList<T> filteredVariants;
			if (inplace) {
				this.variants.clear();
				filteredVariants = this.variants;
			}
			else {
				filteredVariants = new ArrayList<>(projectedVariants.size());
			}

			// Fill 
			for ( TObjectIntIterator<T> it = projectedVariants.iterator(); it.hasNext(); ) {
				it.advance();
				T v = it.key();
				v.setSupport(it.value());
				filteredVariants.add(v);
			}
			
			// If not inplace, create new log
			if (!inplace) {
				return new CVariantLogImpl<>(this.classifier, this.categoryMapper, filteredVariants);
			}
			else {
				return this;
			}
		}
		else {
			if (inplace) {
				for(T variant : this.variants) {
					// Apply inplace
					variantTransformer.apply(variant, true);
				}
				return this;
			}
			else {
				ArrayList<T> filteredVariants = new ArrayList<>(this.size);
				for(T variant : this.variants) {
					filteredVariants.add(variantTransformer.apply(variant, false));
				}
				return new CVariantLogImpl<>(this.classifier, this.categoryMapper, filteredVariants);
			}
		}
	}
	
	@Override
	public String toStringFull() {
		StringBuilder builder = new StringBuilder("CCCVariantLog(\n");
		
		for(T v : this) {
			builder.append(v.getSupport());
			builder.append(": ");
			builder.append("<");
			Iterator<Integer> catCodes = v.iteratorVariantCategorical();
			
			builder.append(StreamSupport.stream(Spliterators.spliteratorUnknownSize(catCodes, Spliterator.ORDERED), false)
				.map(i -> this.categoryMapper.getActivity4Category(i)).collect(Collectors.joining(", ")));
			builder.append(">\n");
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public VariantKeys getVariantKey() {
		return this.variants.get(0).getVariantKey();
	}

	@Override
	public String getActivity4Category(int category) {
		return categoryMapper.getActivity4Category(category);
	}

	@Override
	public CVariantLog<T> copyLog() throws VariantCopyingException {

		ArrayList<T> variantsCopied = new ArrayList<>();
		for(T variant: variants) {
			variantsCopied.add(CVariantUtil.copyVariant(variant));
		}
	
		return new CVariantLogImpl<>(this.classifier, this.categoryMapper, 
				variantsCopied, this.size);
	}

	@Override
	public CVariantLog<T> filterVariantByCondition(CVariantCondition<T> condition, boolean keep)
			throws VariantCopyingException {
		ArrayList<T> filteredVariants = new ArrayList<>(this.variants.size());
	
		if(keep) {
			for(T v: this.variants) {
				if(condition.satisfies(v)) {
					filteredVariants.add(v);
				}
			}
		}
		else {
			for(T v: this.variants) {
				if(!condition.satisfies(v)) {
					filteredVariants.add(v);
				}
			}
		}
		return new CVariantLogImpl<T>(this.classifier, this.categoryMapper, filteredVariants);
	}
	
	@Override
	public CVariantLog<T> filterVariantByConditionAny(Collection<CVariantCondition<T>> conditions, 
	    boolean keep)  throws VariantCopyingException {

		ArrayList<T> filteredVariants = new ArrayList<>(this.variants.size());
		
		// Check Variants
		for(T v: this.variants) {
		  // Check Condition
      for(CVariantCondition<T> condition : conditions) {
        if (condition.satisfies(v)) {
          if(keep) {
            filteredVariants.add(v);
          }
          // STOP as soon as the first condition is satisfied
          break;
        }
      }
		}

		return new CVariantLogImpl<T>(this.classifier, this.categoryMapper, filteredVariants);
	}

	@Override
	public CVariantLog<T> getEmptyCopy() {
		return new CVariantLogImpl<T>(this.classifier, this.categoryMapper, new ArrayList<T>(), 0);
	}

	@Override
	public CategoryMapper getCategoryMapper() {
		return categoryMapper;
	}

	@Override
	public XLog variants2XLog(String logName, boolean activityClearNames) {
		final XConceptExtension conceptInstance = XConceptExtension.instance();
		XLog log = XFactoryRegistry.instance().currentDefault().createLog();
		conceptInstance.assignName(log, logName);

		for (int i = 0; i < variants.size(); i++) {
			XTrace t;
			if (activityClearNames) {
				t = variants.get(i).variant2XTrace("trace " + i, Optional.of(categoryMapper));
			}
			else {
				t = variants.get(i).variant2XTrace("trace " + i, Optional.empty());
			}
			log.add(t);
		}
		return log;
	}


	
}
