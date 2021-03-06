package observationTool.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import experimentExecution.MetricInfo;
import experimentExecution.MetricVariableMapping;
import experimentExecution.SystemInfo;
import observationTool.DataCollector_FileSystem;
import observationTool.MetricRunner_ED;
import observationTool.Universals;
import observationTool.VEntity;
import observationTool.results.MetricResult;

/*
 * DESCRIPTION:
 * 	Based on WKV Chan's Interaction Metric (2011)
 * 	
 * 
 * LOGIC:
 * 	1: Get all cells from step t and t-1
 *  2: Count which cells have changed their state betweeen t-1 and t. Store this in I[t]
 *  3: Also maintain a count of changes per cell. Xit. Probably use a Map<String,Double> for this. 
 *  4: Determine which cell has the largest number of changes at time t X*T. 
 *  5: Calculate the normalised cumulative state changes Yit
 *  6: Calculate the total cumulative state changes up to time t, Zt = sum(Xit)
 *  7: 
 *  8: 
 * 
 * REQUIREMENTS:
 *	• List of agents at each step and the step before
 *	• 
 * 
 * 
 * @author lachlan
 *
 */

public class ChanGoLInterMetric extends MetricBase implements MetricInterface {

	public HashMap<String, Double> cumulativeIndiv;
	public int[] overallChanges;
	public double[] maxAtT;

	public long result_Zt;
	public double result_It;
	public double[][] result_Yit;

	// What are the states that this metric requires
	final String STATE_1 = "STATE_1";

	public ChanGoLInterMetric(MetricInfo mi) {
		// TODO Auto-generated constructor stub
		super("ChanGoLInterMetric", mi);
		cumulativeIndiv = new HashMap<String, Double>();
		this.mi = mi;
		metricVariableMappings = mi.getMetricVariableMappings();
	}

	// What needs to happen here?
	public void setup(ArrayList<VEntity> step_zero, int numberOfSteps) {
		for (VEntity vagent : step_zero) {
			cumulativeIndiv.put(vagent.getID(), 0.0);
		}
		overallChanges = new int[numberOfSteps];
		maxAtT = new double[numberOfSteps];
		result_Yit = new double[numberOfSteps][];
	}

	public void calculateResults(ArrayList<VEntity> step_tm1, ArrayList<VEntity> step_t, int currentStep) {
		// These lists should be the same size
		MetricVariableMapping mvm1 = metricVariableMappings.get(STATE_1);

		if (step_tm1.size() != step_t.size()) {
			System.out.println("Agent lists are not the same size. Terminating metric.");
			return;
		}

		// Sort to fix any issues
		Collections.sort(step_tm1, VEntity.sortByName());
		Collections.sort(step_t, VEntity.sortByName());
		result_Yit[currentStep] = new double[step_tm1.size()];

		// compare life state between each one
		for (int i = 0; i < step_tm1.size(); i++) {
			VEntity vat = step_t.get(i);
			VEntity vatm1 = step_tm1.get(i);
			if (vat.getID().compareTo(vatm1.getID()) != 0) {
				// System.out.println("Issue with comparing non-identical
				// agents. Terminating metric.");
				continue;
			}
			if (vat.getEntityID().getEntityType().compareToIgnoreCase(Universals.BIRD) == 0
					&& vatm1.getEntityID().getEntityType().compareToIgnoreCase(Universals.BIRD) == 0) {
				int currNum = Universals.numberOfNeighbours(vat, step_t, 5);
				int prevNum = Universals.numberOfNeighbours(vatm1, step_tm1, 5);
				if (currNum != prevNum) {
					cumulativeIndiv.put(vat.getID(), cumulativeIndiv.get(vat.getID()) + 1.0);
					overallChanges[currentStep]++;
					if (cumulativeIndiv.get(vat.getID()) > maxAtT[currentStep]) {
						maxAtT[currentStep] = cumulativeIndiv.get(vat.getID()).intValue();
					}
				}

			} else {
				if (entityIsOfType(vatm1, mvm1) && entityIsOfType(vat, mvm1)) {
					if (!compareParameters(vatm1, vat, mvm1)) {
						cumulativeIndiv.put(vat.getID(), cumulativeIndiv.get(vat.getID()) + 1.0);
						overallChanges[currentStep]++;
						if (cumulativeIndiv.get(vat.getID()) > maxAtT[currentStep]) {
							maxAtT[currentStep] = cumulativeIndiv.get(vat.getID()).intValue();
						}
					}
				}
			}
		}

		// calculate It, Yit, and Zt

		// Calc It
		result_It = overallChanges[currentStep];

		// Calc Yit and Zt
		for (int i = 0; i < step_t.size(); i++) {
			Double tmp = cumulativeIndiv.get(step_t.get(i).getID());
			if (tmp == null) {
				cumulativeIndiv.put(step_t.get(i).getID(), 0.0);
				tmp = 0.0;
			} else {
				tmp = tmp.doubleValue();
			}
			// Theres an issue with this array
			// errLog("currentStep: "+currentStep+", i: "+i+", result_Yit
			// size:"+result_Yit.length);

			result_Yit[currentStep][i] = tmp / (double) maxAtT[currentStep];
			result_Zt += cumulativeIndiv.get(step_t.get(i).getID()).intValue();
		}
	}

	public long getResult_Zt() {
		return result_Zt;
	}

	public double getResult_It() {
		return result_It;
	}

	public double[] getResult_Yit(int currStep) {
		return result_Yit[currStep];
	}

	public double[] getResultArray(int currStep) {
		double[] res = new double[2];
		res[0] = result_Zt;
		res[1] = result_It;
		// res[2] = result_Yit[currStep];
		return res;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public MetricResult getResults() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCollector(DataCollector_FileSystem dfs) {
		// TODO Auto-generated method stub

	}
}
