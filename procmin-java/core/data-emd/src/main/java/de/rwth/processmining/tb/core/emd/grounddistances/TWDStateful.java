package de.rwth.processmining.tb.core.emd.grounddistances;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.rwth.processmining.tb.core.emd.language.tracedescriptors.cfplusxfeatures.TimedTrace;

public class TWDStateful implements TraceDescDistCalculator<TimedTrace> {
	private final static Logger logger = LogManager.getLogger( TWDStateful.class );
	
	private final double nu;
    private final double lambda;
	private Table <Integer, Integer, Double> twedLookup = null; 


	public TWDStateful(double nu, double lambda) {
		this.twedLookup = HashBasedTable.create();
		this.nu = nu;
		this.lambda = lambda;
	}

	public double TWED(TimedTrace t1, TimedTrace t2) {
		double[] t_t1 = t1.getTimes();
		double[] t_t2 = t2.getTimes();
		String[] s_t1 = t1.getTraceLabels();
		String[] s_t2 = t2.getTraceLabels();
		int n = t_t1.length;
		int m = t_t2.length;

		double[][] D = new double[n + 1][m + 1];
		double[] Di1 = new double[n + 1];
		double[] Dj1 = new double[m + 1];
		double dist, disti1, distj1;
		// local costs initializations
		for(int j = 1; j <= m; j++) {
			if(j > 1)
				distj1 = (s_t2[j - 2].contentEquals(s_t2[j - 1])) ? 0 : 1;
			  else
				distj1 = 1;
			Dj1[j] = distj1;
		}

		for(int i = 1; i <= n; i++) {
			if(i > 1)
				disti1 = (s_t1[i - 2].contentEquals(s_t1[i - 1])) ? 0 : 1;
			else 
				disti1 = 1;
			Di1[i] = disti1;

			for(int j = 1; j <= m; j++) {
				dist = 0;
				dist = (s_t1[i - 1].contentEquals(s_t2[j - 1])) ? 0 : 1;
				if(i > 1&&j > 1)
					dist += (s_t1[i - 2].contentEquals(s_t2[j - 2])) ? 0 : 1;
				D[i][j] = (dist);
			}
		}

		// border of the cost matrix initialization
		D[0][0] = 0;
		for(int i = 1; i <= n; i++)
			D[i][0] = D[i - 1][0] + Di1[i];
		for(int j = 1; j <= m; j++)
			D[0][j] = D[0][j - 1] + Dj1[j];

		double dmin, htrans, dist0;

		for (int i = 1; i <= n; i++){
			for (int j = 1; j <= m; j++){
				htrans = Math.abs((t_t1[i - 1] - t_t2[j - 1]));
				if(j > 1 && i > 1)
					htrans += Math.abs((t_t1[i - 2] - t_t2[j - 2]));
				dist0 = D[i - 1][j - 1] + nu * htrans + D[i][j];
				dmin = dist0;
				if(i > 1)
					htrans = ((t_t1[i - 1] - t_t1[i - 2]));
				else htrans = t_t1[i - 1];
					dist = Di1[i] + D[i - 1][j] + lambda + nu * htrans;
				if(dmin > dist){
					dmin = dist;
				}
				if(j > 1)
					htrans = (t_t2[j - 1] - t_t2[j - 2]);
				else htrans = t_t2[j - 1];
					dist = Dj1[j] + D[i][j - 1] + lambda + nu * htrans;
				if(dmin > dist){
					dmin = dist;
				}
				D[i][j] = dmin;
			}
		}
		dist = D[n][m];
		if (dist < 0) {
			logger.error("Distance negative!!!");
		}
		return dist;
	}

	@Override
	public double get_distance(TimedTrace t1, TimedTrace t2) {
		return TWED(t1, t2);
	}

	@Override
	public String toString() {
		return "TWDStateful [nu=" + nu + ", lambda=" + lambda + "]";
	}

	@Override
	public String getShortDescription() {
		return "TWD";
	}

}
