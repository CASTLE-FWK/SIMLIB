package experimentExecution;

import java.util.ArrayList;
import java.util.HashSet;

public class Experiment {
	String experimentID;
	String systemName;
	ArrayList<SystemInfo> theTestSystems;
	String description;
	ArrayList<MetricInfo> metricsUsed;

	
	HashSet<String> enabledMetrics;
	boolean usingAllMetrics = false;

	public Experiment(String experimentID, String description, String sysName) {
		metricsUsed = new ArrayList<MetricInfo>();
		this.experimentID = experimentID;
		this.description = description;
		systemName = sysName;
		theTestSystems = new ArrayList<SystemInfo>();
		enabledMetrics = new HashSet<String>();
	}
	
	public String getExperimentSystemName() {
		return systemName;
	}
	
	public void addEnabledMetric(String s) {
		if (s.compareToIgnoreCase("ALL") == 0) {
			usingAllMetrics = true;
		} else {
			enabledMetrics.add(s);
		}
	}
	
	public String metricsBeingUsed() {
		String str = "Metrics being used: [";
		if (usingAllMetrics) {
			str += "ALL ]\n";
		} else {
			str += enabledMetrics.toString()+" ]\n";
		}
		return str;
	}
	
	public HashSet<String> getEnabledMetrics(){
		return enabledMetrics;
	}
	public boolean isUsingAllMetrics() {
		return usingAllMetrics;
	}

	public void setUsingAllMetrics(boolean t) {
		usingAllMetrics = t;
	}
	
	public void addMetricInfos(ArrayList<MetricInfo> MetricInfos) {
		metricsUsed = MetricInfos;
	}

	public void addMetricInfo(MetricInfo m) {
		metricsUsed.add(m);
	}

	public ArrayList<SystemInfo> getTestSystems() {
		return theTestSystems;
	}

	public int numberOfTestSystems() {
		return theTestSystems.size();
	}

	public void addTestSystem(SystemInfo si) {
		theTestSystems.add(si);
	}

	public ArrayList<MetricInfo> getMetrics() {
		return metricsUsed;
	}

	public String getExperimentID() {
		return experimentID;
	}

	public String experimentInfo() {
		String str = "TODO";

		return str;
	}

	public String toString() {
		String str = "Experiment: " + experimentID + "\n";
		str += "Description: " + description;
		str += "Testing Systems: ";
		for (int i = 0; i < theTestSystems.size(); i++) {
			str += theTestSystems.get(i).toString() + "\n";
		}
		str += "Metrics: \n";
		for (int i = 0; i < metricsUsed.size(); i++) {
			str += metricsUsed.get(i).toString() + "\n";
		}

		return str;
	}
}