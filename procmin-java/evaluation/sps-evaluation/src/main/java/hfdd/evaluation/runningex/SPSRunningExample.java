package hfdd.evaluation.runningex;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.processmining.log.utils.XLogBuilder;

import de.rwth.processmining.tb.core.emd.dataview.PerspectiveDescriptor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDItManageBuilderFixed;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagement;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.HFDDIterationManagementBuildingException;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.AutoIterationDiagnosticsExtractor;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.CandidatesMetaDiagnostic;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.DiffCandidate;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.IVFilterIndepentDiff;
import de.rwth.processmining.tb.core.sps.algorithm.iteration.diffcandidates.InterestingVertexFinder;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDGraph;
import de.rwth.processmining.tb.core.sps.data.hfddgraph.HFDDVertex;

public class SPSRunningExample {

  public static void main(String[] args) throws HFDDIterationManagementBuildingException {
		Map<String, Integer> actPosMap = getIntraSetSorting();
    HFDDIterationManagement hfddItMan = createIterationManagement(createRunningExampleLogs());
    ////////////////////////////////////////
    // Parameters
    ////////////////////////////////////////
    final double tMetric = 0.001;
    final double tDom = 0.9;
    final double tPhi = 0.05;
    final int nbrMetaDiagnostics = 5;
    final int k = 3;
    final float jaccardUpper = 0.2f;
    final float filterDiff = 0.01f;
    final float filterFreq = 0.02f;
    
    // 1. Setup - Auto-Extractor
    PerspectiveDescriptor pDesc = hfddItMan.getPerspective4Iteration(0);
    AutoIterationDiagnosticsExtractor autoExtractor = new AutoIterationDiagnosticsExtractor(hfddItMan, 0,
        nbrMetaDiagnostics, tMetric, tDom, tPhi, k, jaccardUpper);

    // 2. Discovery - Meta Vertex Sets
    List<CandidatesMetaDiagnostic> metaDiagnostics = autoExtractor.getDiagnostics();
    
    printMetaDifferenceCollections(hfddItMan, metaDiagnostics);
    
		////////////////////////////////////////
		// Graph INFO
		////////////////////////////////////////
		for (var v : hfddItMan.getHfddGraph().getVertices()) {
			printVertexInfo(v, pDesc, actPosMap);
		}
		
		printEdges(hfddItMan.getGraph(), actPosMap);
		
    InterestingVertexFinder diffFinder = new IVFilterIndepentDiff(hfddItMan, 0, tMetric, tDom, tPhi);
		Collection<HFDDVertex> interestingVertices = diffFinder.findInterestingVertices()
				.stream().map(DiffCandidate::v).toList();;
		
		////////////////////////////////////////
		// Plot "Interestingess" Information
		////////////////////////////////////////
		////////////////////
		// Types
		////////////////////
    for (var v : hfddItMan.getHfddGraph().getVertices()) {
      String type = "";
      switch (diffFinder.getLastRunSPITypes()[v.getId()]) {
      case INTERESTING:
        type = "I";
        break;
      case PURE_UNINTERESTING:
        type = "PU";
        break;
      case SUB_INTERESTING:
        type = "SI";
        break;
      default:
        break;
      }
      System.out.println("[" + v.getId() + "] = \"" + type + "\",");
    }

		System.out.println("---------- Interesting Base ----------");
		StringBuilder builderInt = new StringBuilder();
		builderInt.append("local idsSelectedBase = {");
		for (var u : interestingVertices) {
			builderInt.append("[");
			builderInt.append(u.getId());
			builderInt.append("] = true, ");
			//printVertexInfo(u, p0, p1, p2, actPosMap);
		}
		builderInt.append("}");
		System.out.println(builderInt.toString());
    
  }
  
	public static HFDDIterationManagement createIterationManagement(Pair<XLog, XLog> logs) throws HFDDIterationManagementBuildingException {
		XEventClassifier classifier = XLogInfoImpl.NAME_CLASSIFIER;

    //HFDDIterationManagementBuilderRunningEx hfddManBuilder = new HFDDIterationManagementBuilderRunningEx();
    //hfddManBuilder.setMinSetSupport(0.02)
    //  .setClassifier(classifier)
    //  .setXlogL(logs.getLeft()).setXlogR(logs.getRight());
    
		// Create the iteration management instance
		HFDDItManageBuilderFixed builder = new HFDDItManageBuilderFixed();
		builder.setClassifier(classifier).setXlogL(logs.getLeft()).setXlogR(logs.getRight());
	
		builder
			.addItemset(Set.of("l"))
			.addItemset(Set.of("h"))
			.addItemset(Set.of("c1"))
			.addItemset(Set.of("c2"))
			.addItemset(Set.of("c"))
			.addItemset(Set.of("a"))
			.addItemset(Set.of("d"))
			.addItemset(Set.of("p"))
			//.addItemset(Set.of("l", "c1"))
			.addItemset(Set.of("a", "p"))
			.addItemset(Set.of("c1", "c2"))
			.addItemset(Set.of("c1", "c"))
			.addItemset(Set.of("l", "a"))
			.addItemset(Set.of("l", "p"))
			.addItemset(Set.of("l", "d"))
			.addItemset(Set.of("h", "a"))
			.addItemset(Set.of("h", "p"))
			.addItemset(Set.of("h", "d"))
			.addItemset(Set.of("c1", "c2", "c"))
			.addItemset(Set.of("l", "a", "p"))
			.addItemset(Set.of("h", "a", "p"));
		
		HFDDIterationManagement hfddItMan = builder.build();
		return hfddItMan;
	}
	
	private static void printVertexInfo(HFDDVertex v, PerspectiveDescriptor p, Map<String, Integer> actPosMap) {

		List<String> activityItems = Arrays.stream(v.getVertexInfo().getItemsetHumanReadable())
				.map(String::toLowerCase)
				.map(s -> s.replace("+complete", ""))
				.sorted((s1, s2) -> Integer.compare(actPosMap.get(s1), actPosMap.get(s2)))
				.toList();
		String strItemset = activityItems.stream()
				.map(s -> "\"" + s + "\"")
				.collect(Collectors.joining(",", "{", "}"));
		String tikzKey = activityItems.stream()
				.collect(Collectors.joining("_"));

		System.out.println(String.format(Locale.ROOT, "{id=%d, metric=%f, pNeLeft=%f, pNeRight=%f, "
				+ "act=%s, tikzKey=\"%s\"},", v.getId(),
				v.getVertexInfo().getMeasurements().get(p).getMetric().get(),
				v.getVertexInfo().getMeasurements().get(p).getProbLeftNonEmpty(),
				v.getVertexInfo().getMeasurements().get(p).getProbRightNonEmpty(),
				strItemset,
				tikzKey));
	}
	
	private static void printEdges(HFDDGraph hfddGraph, Map<String, Integer> actPosMap) {
		Graph<HFDDVertex, DefaultEdge> g = hfddGraph.getGraph();
		
		for (final HFDDVertex v : g.vertexSet()) {
			String vTikzKey = getVertexTikZKey(v, actPosMap);
			String childrenKeys  = g.outgoingEdgesOf(v).stream()
				.map(g::getEdgeTarget)
				.map(u -> getVertexTikZKey(u, actPosMap))
				.collect(Collectors.joining(", ", "{", "}"));
			if (!childrenKeys.equals("{}")) {
				System.out.println(vTikzKey + " --[spEdge] " + childrenKeys + ";" );
			}
		}
	}
	
	private static String getVertexTikZKey(HFDDVertex v,Map<String, Integer> actPosMap) {
		return Arrays.stream(v.getVertexInfo().getItemsetHumanReadable())
			.map(String::toLowerCase)
			.map(s -> s.replace("+complete", ""))
			.sorted((s1, s2) -> Integer.compare(actPosMap.get(s1), actPosMap.get(s2)))
			.collect(Collectors.joining("_"));
	}

	private static Map<String, Integer> getIntraSetSorting() {
		Map<String, Integer> res = new HashMap<>();
		res.put("l", 0);
		res.put("h", 1);
		res.put("c1", 2);
		res.put("c2", 3);
		res.put("c", 4);
		res.put("d", 5);
		res.put("a", 6);
		res.put("p", 7);
		return res;
	}

	/**
	 * Build two artificial running example logs.
	 * @return
	 */
	public static Pair<XLog, XLog> createRunningExampleLogs() {
		/*
		 * Left Log:
		 * 	<l, c1, c2, c a, p>^{42}
		 * 
		 * Right Log:
		 */
		// Build logs
		// Left log
		int tracesL = 0;
		int factor = 5;
		XLogBuilder logBuilderL = XLogBuilder.newInstance().startLog("Log left");
		// Low claims
		for(int i = 0; i < factor * 42; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("l").addEvent("c1").addEvent("c2").addEvent("c").addEvent("a").addEvent("p");
			tracesL++;
		}
		for(int i = 0; i < factor * 9; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("l").addEvent("c2").addEvent("c1").addEvent("c").addEvent("d");
			tracesL++;
		}
		for(int i = 0; i < factor * 9; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("l").addEvent("c1").addEvent("c2").addEvent("c").addEvent("d");
			tracesL++;
		}
		// High claims
		for(int i = 0; i < factor * 28; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("h").addEvent("c1").addEvent("c2").addEvent("c").addEvent("a").addEvent("p");
			tracesL++;
		}
		for(int i = 0; i < factor * 6; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("h").addEvent("c2").addEvent("c1").addEvent("c").addEvent("d");
			tracesL++;
		}
		for(int i = 0; i < factor * 6; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("h").addEvent("c1").addEvent("c2").addEvent("c").addEvent("d");
			tracesL++;
		}
    ////////////////////
		// Right Log
    ////////////////////
		int tracesR = 0;
		XLogBuilder logBuilderR = XLogBuilder.newInstance().startLog("Log right");
		// Low claims
		for(int i = 0; i < factor * 50; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("l").addEvent("c1").addEvent("c2").addEvent("c").addEvent("a").addEvent("p");
			tracesR++;
		}
		for(int i = 0; i < factor * 7; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("l").addEvent("c2").addEvent("c1").addEvent("c").addEvent("d");
			tracesR++;
		}
		for(int i = 0; i < factor * 3; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("l").addEvent("c1").addEvent("c2").addEvent("c").addEvent("d");
			tracesR++;
		}
		// High claims
		for(int i = 0; i < factor * 20; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("h").addEvent("c1").addEvent("c2").addEvent("c").addEvent("a").addEvent("p");
			tracesR++;
		}
		for(int i = 0; i < factor * 13; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("h").addEvent("c2").addEvent("c1").addEvent("c").addEvent("d");
			tracesR++;
		}
		for(int i = 0; i < factor * 7; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("h").addEvent("c1").addEvent("c2").addEvent("c").addEvent("d");
			tracesR++;
		}

		XLog logL = logBuilderL.build();
		XLog logR = logBuilderR.build();

		// Add Lifecyles
		for(XTrace t: logL) {
			for(XEvent e: t) {
				XLifecycleExtension.instance().assignStandardTransition(e, XLifecycleExtension.StandardModel.COMPLETE);
				
			}
		}
		for(XTrace t: logR) {
			for(XEvent e: t) {
				XLifecycleExtension.instance().assignStandardTransition(e, XLifecycleExtension.StandardModel.COMPLETE);
				
			}
		}
		return Pair.of(logL, logR);
	
	}

	/**
	 * Build two artificial running example logs.
	 * @return
	 */
	public static Pair<XLog, XLog> createRunningExampleLogs2() {
		/*
		 * Left Log:
		 * 	<l, c1, c2, c a>^{18}
		 * 	<R, A, L, P>^{17}
		 * 	<R, A, L, S>^{18}
		 * 	<R, L, A, S>^{17}
		 * 
		 * 	<R, H, A, P>^{15}
		 * 	<R, H, A, S>^{15}
		 * 
		 * Right Log:
		 * 	<R, L, A, P>^{25}
		 * 	<R, L, A, S>^{25}
		 * 
		 * 	<R, H, A, P>^{20}
		 * 	<R, H, A, S>^{30}
		 */
		// Build logs
		// Left log
		int tracesL = 0;
		int factor = 5;
		XLogBuilder logBuilderL = XLogBuilder.newInstance().startLog("Log left");
		// Low claims
		for(int i = 0; i < factor * 42; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("l").addEvent("c1").addEvent("c2").addEvent("c").addEvent("a");
			tracesL++;
		}
		for(int i = 0; i < factor * 9; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("l").addEvent("c2").addEvent("c1").addEvent("c").addEvent("d");
			tracesL++;
		}
		for(int i = 0; i < factor * 9; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("l").addEvent("c1").addEvent("c2").addEvent("c").addEvent("d");
			tracesL++;
		}
		// High claims
		for(int i = 0; i < factor * 28; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("h").addEvent("c1").addEvent("c2").addEvent("c").addEvent("a");
			tracesL++;
		}
		for(int i = 0; i < factor * 6; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("h").addEvent("c2").addEvent("c1").addEvent("c").addEvent("d");
			tracesL++;
		}
		for(int i = 0; i < factor * 6; i++) {
			logBuilderL.addTrace("L " + tracesL)
					.addEvent("h").addEvent("c1").addEvent("c2").addEvent("c").addEvent("d");
			tracesL++;
		}
    ////////////////////
		// Right Log
    ////////////////////
		int tracesR = 0;
		XLogBuilder logBuilderR = XLogBuilder.newInstance().startLog("Log right");
		// Low claims
		for(int i = 0; i < factor * 50; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("l").addEvent("c1").addEvent("c2").addEvent("c").addEvent("a");
			tracesR++;
		}
		for(int i = 0; i < factor * 5; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("l").addEvent("c2").addEvent("c1").addEvent("c").addEvent("d");
			tracesR++;
		}
		for(int i = 0; i < factor * 5; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("l").addEvent("c1").addEvent("c2").addEvent("c").addEvent("d");
			tracesR++;
		}
		// High claims
		for(int i = 0; i < factor * 20; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("h").addEvent("c1").addEvent("c2").addEvent("c").addEvent("a");
			tracesR++;
		}
		for(int i = 0; i < factor * 10; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("h").addEvent("c2").addEvent("c1").addEvent("c").addEvent("d");
			tracesR++;
		}
		for(int i = 0; i < factor * 10; i++) {
			logBuilderR.addTrace("R " + tracesR)
					.addEvent("h").addEvent("c1").addEvent("c2").addEvent("c").addEvent("d");
			tracesR++;
		}

		XLog logL = logBuilderL.build();
		XLog logR = logBuilderR.build();

		// Add Lifecyles
		for(XTrace t: logL) {
			for(XEvent e: t) {
				XLifecycleExtension.instance().assignStandardTransition(e, XLifecycleExtension.StandardModel.COMPLETE);
				
			}
		}
		for(XTrace t: logR) {
			for(XEvent e: t) {
				XLifecycleExtension.instance().assignStandardTransition(e, XLifecycleExtension.StandardModel.COMPLETE);
				
			}
		}
		return Pair.of(logL, logR);
	
	}

  public static void printMetaDifferenceCollections(HFDDIterationManagement hfddItMan, 
      List<CandidatesMetaDiagnostic> metaDiagnostics) {
		PerspectiveDescriptor pDesc = hfddItMan.getPerspective4Iteration(0);
    int i = 0;
    for (CandidatesMetaDiagnostic metaDiag : metaDiagnostics) {
      System.out.println("Meta diagnostic set " + i + " for "
          + HFDDIterationManagement.generateShortInfo(hfddItMan.getGraph(), metaDiag.diffCandidate(), pDesc) + ":");
      System.out.println(metaDiag.complDifferences().stream()
        .map(c -> HFDDIterationManagement.generateShortInfo(hfddItMan.getGraph(), c, pDesc))
        .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
        .toString()
      );
      i++;
    }
  }
}
