package org.processmining.processcomparator.tbutils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;

public class MetricExtractor {
  
  private static MetricExtractor instance;
  
  /**
   * Store the effect size for each state of the TS.
   */
  private Map<State, Double> stateEffectSize; 

  /**
   * Stores for each state of the TS whether there is a difference.
   */
  private Map<State, Boolean> stateSignificance;

  /**
   * Store the effect size for each transition of the TS.
   */
  private Map<Transition, Double> transitionEffectSize; 

  /**
   * Stores for each transition of the TS whether there is a difference.
   */
  private Map<Transition, Boolean> transitionSignificance;
  
  private MetricExtractor() {
    stateEffectSize = new HashMap<>();
    stateSignificance = new HashMap<>();
    transitionEffectSize = new HashMap<>();
    transitionSignificance = new HashMap<>();
  }
  
  public static MetricExtractor instance() {
    if (instance == null) {
      instance = new MetricExtractor();
    }
    return instance;
  }
  
  public void clearMetrics() {
    this.stateEffectSize.clear();;
    this.stateSignificance.clear();;
    this.transitionEffectSize.clear();
    this.transitionSignificance.clear();
  }
  
  public void setStateSignificance(State s, Boolean significant) {
    this.stateSignificance.put(s, significant);
  }

  public void setStateEffectSize(State s, Double effectSize) {
    this.stateEffectSize.put(s, effectSize);
  }

  public void setTransitionSignificance(Transition t, Boolean significant) {
    this.transitionSignificance.put(t, significant);
  }

  public void setTransitionEffectSize(Transition t, Double effectSize) {
    this.transitionEffectSize.put(t, effectSize);
  }
  
  public String getMetricCSVFormat() {

    // If effect size, then also significance result
    assert this.stateSignificance.keySet().containsAll(
        this.stateEffectSize.keySet());
    assert this.transitionSignificance.keySet().containsAll(
        this.transitionEffectSize.keySet());


    StringBuilder resString = new StringBuilder();
    // Header of csv
    resString.append("Element,Significant,EffectSize");
    
    // Add state information
    resString.append(this.stateSignificance.entrySet().stream()
        .map(e -> {
          // If significant => Determine effect size
          String label = e.getKey().getLabel();
          boolean significant = e.getValue();
          if (significant) {
            Double effectSize = this.stateEffectSize.get(e.getKey());
            return label + "," + Boolean.toString(significant) + "," + Double.toString(effectSize);
          }
          else {
            return label + "," + Boolean.toString(significant) + ",";
          }
        })
        .collect(Collectors.joining("\n", "", "\n"))
      );
    
    // Add transition information
    resString.append(this.transitionSignificance.entrySet().stream()
        .map(e -> {
          // If significant => Determine effect size
          String label = e.getKey().getLabel();
          boolean significant = e.getValue();
          if (significant) {
            Double effectSize = this.transitionEffectSize.get(e.getKey());
            return label + "," + Boolean.toString(significant) + "," + Double.toString(effectSize);
          }
          else {
            return label + "," + Boolean.toString(significant) + ",";
          }
        })
        .collect(Collectors.joining("\n"))
      );
    
    return resString.toString();
  }

  public Map<State, Double> getStateEffectSize() {
    return stateEffectSize;
  }

  public Map<State, Boolean> getStateSignificance() {
    return stateSignificance;
  }

  public Map<Transition, Double> getTransitionEffectSize() {
    return transitionEffectSize;
  }

  public Map<Transition, Boolean> getTransitionSignificance() {
    return transitionSignificance;
  }

}
