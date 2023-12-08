package de.rwth.processmining.tb.core.sps.algorithm.ptdfgdiscovery;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;

import org.processmining.plugins.graphviz.dot.Dot2Image.Engine;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;

/**
 * More or less a copy of {@link org.processmining.plugins.graphviz.dot.Dot2Image}.
 * 
 * However, it assumes that dot is on the path and avoids copying it (the resource used to do so, might not
 * even exist without performing the ProM plugin scan).
 */
public class MyDot2Image {
  
  public static InputStream dot2imageInputStream(String dot, Type type, Engine engine, 
      Optional<Path> pathDotExec) {
    
    // Dot executable
    // If provided, take it; otherwise assume it on the path
    Path dotExec = pathDotExec.orElse(Path.of("dot.exe"));
    
		String cmds[];
		switch (engine) {
			case dot :
				cmds = new String[3];
				cmds[0] = dotExec.toString();
				cmds[1] = "-T" + type;
				cmds[2] = "-q";
				break;
			case neato :
				cmds = new String[5];
				cmds[0] = dotExec.toString();
				cmds[1] = "-n";
				cmds[2] = "-Kneato";
				cmds[3] = "-T" + type;
				cmds[4] = "-q";
				break;
			default :
				cmds = new String[3];
				cmds[0] = dotExec.toString();
				cmds[1] = "-T" + type;
				cmds[2] = "-q";
				break;

		}

		final ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.redirectErrorStream(false);
		Process dotProcess = null;
		try {
			dotProcess = pb.start();
			BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(dotProcess.getOutputStream(), "UTF-8"));
			out2.write(dot.toString());
			out2.flush();
			out2.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		redirectIO(dotProcess.getErrorStream(), System.err);

		InputStream outputOfDot = new BufferedInputStream(dotProcess.getInputStream());
		return outputOfDot;
    
  }
  
  private static void redirectIO(final InputStream src, final PrintStream dest) {
		new Thread(new Runnable() {
			public void run() {
				Scanner sc = new Scanner(src);
				while (sc.hasNextLine()) {
					dest.println(sc.nextLine());
				}
				sc.close();
			}
		}).start();
	}
}
