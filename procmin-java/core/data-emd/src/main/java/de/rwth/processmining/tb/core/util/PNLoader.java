package de.rwth.processmining.tb.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.importing.PnmlImportUtils;

public class PNLoader {

	/**
	 * Load the Petri net from the provided file (path).
	 * @param path Path of the Petri net file
	 * @param pnName Name of the Petri net
	 * @return Petri net
	 * @throws Exception
	 */
	public static Object[] loadWorkflowPetriNet(String path, String pnName) throws Exception {
		final File pnFile = new File(path);
		return loadWorkflowPetriNet(pnFile, pnName);
	}
	
	/**
	 * Load the Petri net from the provided file.
	 * @param path Path of the Petri net file
	 * @param pnName Name of the Petri net
	 * @return Petri net
	 * @throws Exception
	 */
	public static Object[] loadWorkflowPetriNet(File pnFile, String fileName) throws Exception {
		InputStream pnFileStream = new FileInputStream(pnFile);
		
		PnmlImportUtils inpUtil = new PnmlImportUtils();
		
		// Plugin Context not use (if there is no error)
		// If there is an error in the file, it will try to access the context,
		// and we will get a null pointer exception
		Pnml pnml = inpUtil.importPnmlFromStream(null, pnFileStream, 
				fileName, 0);
		Petrinet pn = PetrinetFactory.newPetrinet(pnml.getLabel());

		/*
		 * Create fresh marking(s) and layout.
		 */
		Marking marking = new Marking();
		Collection<Marking> fm = new HashSet<Marking>();
		GraphLayoutConnection layout = new GraphLayoutConnection(pn);

		/*
		 * Initialize the Petri net, marking(s), and layout from the PNML
		 * element.
		 */
		pnml.convertToNet(pn, marking, fm, layout);

		
		//////////////////////////////
        // Initial and Final Marking
		//////////////////////////////
    // Assumption: Workflow net
    Place source = null;
    Place sink = null;
    for (Place p: pn.getPlaces()) {
      if (pn.getInEdges(p).size() == 0) {
        source = p;
      }
      else if (pn.getOutEdges(p).size() == 0) {
        sink = p;
      }
    }
    
    // Initial Marking
    Marking initMarking = new Marking();
    initMarking.add(source);
    
    // Final Marking
    Marking[] finalMarkings = new Marking[1];
    Marking finalMarking = new Marking();
    finalMarking.add(sink);
    finalMarkings[0] = finalMarking;
    
    //		Object[] objects = (Object[]) inpUtil.connectNet(context, pnml, net);

    Object[] res = new Object[3];
    res[0] = pn;
    res[1] = initMarking;
    res[2] = finalMarkings;
		return res;
		
	}

}
