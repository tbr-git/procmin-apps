package hfdd.evaluation.cdrift;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.deckfour.xes.model.XLog;
import org.junit.jupiter.api.Test;
import org.processmining.log.utils.XLogBuilder;

import hfdd.evaluation.cdrift.taskcreation.PVAOnCDTestSpec;

class PVATaskExtractionFromCDLogTest {
  
  @Test
  void test() {
    PVATaskExtractionFromCDLog.MIN_LOGSIZE = 10;
    XLog cdLog = createArtificialCDLog();
    List<Integer> driftPoints = List.of(75, 150, 225, 325);
    List<PVAOnCDTestSpec> testSpecs = PVATaskExtractionFromCDLog.createPVATasksFromCDTask("ArtTest", "artLog", 
        cdLog, driftPoints);
    
    for (PVAOnCDTestSpec testSpec : testSpecs) {
      System.out.println(testSpec.idExtraction());
      System.out.println(testSpec.logL().size());
      System.out.println(testSpec.logR().size());
    }

    assertEquals(9, testSpecs.size());
    assertEquals(37, testSpecs.get(0).logL().size());
    assertEquals(38, testSpecs.get(0).logR().size());
    assertNotEquals(testSpecs.get(0).idExtraction(), testSpecs.get(1).idExtraction());
    assertFalse(testSpecs.get(0).driftInducedDifference());
    assertTrue(testSpecs.get(1).driftInducedDifference());
  } 

	/**
	 * Build an artificial concept drift log.
	 * @return
	 */
	public static XLog createArtificialCDLog() {
		/*
		 * Log:
		 * 1.	<A, B>^{75}
		 * 2.	<C, D>^{75}
		 * 3.	<E, F>^{75}
		 * 4.	<G, H>^{100}
		 * 5.	<I, J>^{75}
		 */
		int traces = 0;
		XLogBuilder logBuilder = XLogBuilder.newInstance().startLog("Log CD");
		// <A, B>
		for(int i = 0; i < 75; i++) {
			logBuilder.addTrace("T-" + traces)
					.addEvent("A").addEvent("B");
			traces++;
		}
		// <C, D>
		for(int i = 0; i < 75; i++) {
			logBuilder.addTrace("T-" + traces)
					.addEvent("C").addEvent("D");
			traces++;
		}
		// <E, F>
		for(int i = 0; i < 75; i++) {
			logBuilder.addTrace("T-" + traces)
					.addEvent("E").addEvent("F");
			traces++;
		}
		// <G, H>
		for(int i = 0; i < 100; i++) {
			logBuilder.addTrace("T-" + traces)
					.addEvent("G").addEvent("H");
			traces++;
		}
		// <I, J>
		for(int i = 0; i < 75; i++) {
			logBuilder.addTrace("T-" + traces)
					.addEvent("I").addEvent("J");
			traces++;
		}
		
		return logBuilder.build();
	}

}
