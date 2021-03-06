package visualisation;

import interactionGraph.InteractionGraph;
import observationTool.DataCollector_FileSystem;
import observationTool.DataCollector_MongoDB;
import observationTool.VEntity;
import observationTool.metrics.ClusterTrack;
import observationTool.metrics.Entropy;
import observationTool.metrics.SelfAdaptiveSystems;
import observationTool.metrics.SystemComplexity;
import observationTool.results.MetricResult;

import java.awt.Color;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;

import org.bson.Document;

import castleComponents.objects.Vector2;
import experimentExecution.MetricInfo;
import experimentExecution.SystemInfo;
import visualisation.phorcys.API;
import visualisation.phorcys.PNG;
import visualisation.phorcys.Mesh;
import visualisation.phorcys.Plot2D;
import stdSimLib.Interaction;

/**
 * So, what happens in this? Well, we take a dataset ID, find out how long the
 * simulation ran Draw agents on a window (define their colour based on a state
 * [Hardcode for now])
 * 
 * 
 * 
 * @author lachlanbirdsey
 *
 */
public class DataSetSimulator {

	String datasetID;
	String db = "simulations";
	HashMap<String, VAgentSpec> specs; // <AgentName, VAgentSpec>
	int numberOfSteps;
	int currentTime = 0;
	DataCollector_FileSystem collector;
	String windowID = null;
	Vector2 dimensions;
	boolean cycling = false;
	VSystem fakeSystem;
	int startTime = 1;

	// Have to do this to make sure the plots don't vanish :(
	Plot2DHelper scPlot;
	// String scPlotWindowID = null;
	Plot2DHelper entPlot;
	// String entPlotWindowID = null;
	Plot2DHelper watPlot;
	// String watPlotWindowID = null;
	Plot2DHelper perfPlot;
	// String perfPlotWindowID = null;
	Plot2DHelper AUPlot;
	Plot2DHelper ODPlot;
	Plot2DHelper TTPlot;

	// For AU
	int consecutiveDowntime = 2; // The shortest amount of consecutive down time
	HashMap<String, Integer> theAgentsDowntime = new HashMap<String, Integer>();
	HashMap<String, Integer> theAgentsUptime = new HashMap<String, Integer>();

	// For OD
	ArrayList<BitSet> bitsOverTime = new ArrayList<BitSet>();
	int bitsetCounter = 0;

	// Phorcys stuff
	// API phorcysAccess;
	PNG png;
	Mesh mesh;

	// TODO: Add flags for mesh, pane, network, etc
	public DataSetSimulator() {
		// Init some form of pane (or connect to phorcys...)
		specs = new HashMap<String, VAgentSpec>();
		mesh = new Mesh();

	}

	public void begin() {
		stepToTime(startTime);
	}

	public void stepForward() {
		stepToTime(currentTime + 1);
	}

	public void stepBack() {
		stepToTime(currentTime - 1);
	}

	public void restart() {
		currentTime = 0;
		stepToTime(0);
	}

	public void stepToTime(int time) {
		currentTime = time;
		if (currentTime > numberOfSteps) {
			currentTime = numberOfSteps;
		}
		if (currentTime < startTime) {
			currentTime = startTime;
		}
		// VSystemAtTime currSystem = fakeSystem.getVSystemAtTime(time);
		// Get agents
		ArrayList<VEntity> agents = collector.buildVAgentList(currentTime);
		// ArrayList<VAgent> agents = currSystem.getAgentList();

		// Get interactions(?)

		// ReInit
		png.newImage();
		// Display things
		for (VEntity agt : agents) {
			drawAgentOnGrid(agt);

		}
		png.prepImage();

		// Send to Phorcys...
		// ...as Mesh
		// if (windowID == null){
		// windowID = API.mesh(null, "Game of Life: Step "+time, mesh.toJsonFromMap());
		// } else {
		// API.mesh(windowID, "Game of Life: Step "+time, mesh.toJsonFromMap());
		// }

		// ...as PNG
		if (windowID == null) {
			windowID = API.image(null, "Game of Life: Step " + time, png.toJson());
		} else {
			API.image(windowID, "GoL: Step " + time, png.toJson());
		}

		// Draw metrics on screen
		if (time > startTime) {
			// drawSimpleMetrics(time, agents, collector);
		}
	}

	public void drawAgentOnGrid(VEntity agt) {
		Vector2 pos = agt.getPosition();
		Color currColor = Color.WHITE;
		// TODO HARDCODE BECAUSE FUCK IT
		if (agt.containsParameter("Alive")) {
			if (agt.getParameterValueFromStringAsString("Alive").compareToIgnoreCase("true") == 0) {
				currColor = Color.BLACK;
			} else {
				currColor = Color.WHITE;
			}
		}

		if (agt.containsParameter("alive")) {
			if (agt.getParameterValueFromStringAsString("alive").compareToIgnoreCase("true") == 0) {
				currColor = Color.BLACK;
			} else {
				currColor = Color.WHITE;
			}
		}

		// Here we go the colours things
		// Since time is important, we will hardcode

		// Ants
		boolean antFood = false;
		if (agt.containsParameter("hasFood")) {
			if (agt.getParameterValueFromStringAsString("hasFood").compareToIgnoreCase("true") == 0) {
				currColor = Color.RED;
				antFood = true;
			} else {
				currColor = Color.BLACK;
			}
		}

		if (agt.containsParameter("onPheromone")) {
			if (agt.getParameterValueFromStringAsString("onPheromone").compareToIgnoreCase("true") == 0) {
				if (antFood) {
					currColor = Color.RED;
				} else {
					currColor = Color.GREEN;
				}
			} else {
				if (antFood) {
					currColor = Color.RED;
				} else {
					currColor = Color.BLACK;
				}

			}
		}

		String colourAsThreeString = "rgb(" + currColor.getRed() + "," + currColor.getGreen() + ","
				+ currColor.getBlue() + ")";
		// How do we draw it on a grid?
		// We use Phorcys PNG
		png.addElementToImage((int) pos.getX(), (int) pos.getY(), currColor);
		// Or Phorcys MeshPane
		// mesh.addEntityToMap(agt.getName(), pos.getX()/dimensions.getX(),
		// pos.getY()/dimensions.getY(), 0, colourAsThreeString);

	}

	public void newSimulation(String datasetID) {
		this.datasetID = datasetID;
		// Connect to DB
		collector = new DataCollector_FileSystem(db);
		// Get access to the desired dataset
		collector.setCollection(this.datasetID);

		// Wouldn't it be great to grab the entire dataset and jam it into memory here?

		// Get useful information about the system
		numberOfSteps = collector.getTerminationStep();
		HashMap<String, String> params = collector.getInitialisationParameters();

		dimensions = new Vector2(100, 100);
		String initName = "";
		int numberOfCells = 0;
		// for (Document doc : sysParams){
		// if (doc.getString("parameter-name").compareToIgnoreCase("Size (X)") == 0){
		// dimensions.setX(Integer.parseInt(doc.getString("parameter-value")));
		// } else if (doc.getString("parameter-name").compareToIgnoreCase("Size(Y)") ==
		// 0){
		// dimensions.setY(Integer.parseInt(doc.getString("parameter-value")));
		// } else if (doc.getString("parameter-name").compareToIgnoreCase("initPath") ==
		// 0){
		// initName = doc.getString("parameter-value");
		// }
		// }
		theAgentsDowntime = new HashMap<String, Integer>();
		theAgentsUptime = new HashMap<String, Integer>();
		bitsOverTime = new ArrayList<BitSet>(numberOfSteps);

		// fakeSystem = new VSystem();
		// fakeSystem.fillSystem(collector, numberOfSteps);
		//
		// Now we have the basic information, we can define shit?

		// Set up the phorcys sender
		png = new PNG((int) dimensions.getX(), (int) dimensions.getY());
		if ((int) dimensions.getX() > 1500) {
			png.setScale(2, 2);
		} else {
			png.setScale(10, 10);
		}

		png.setLabels("OLOLOLO");

		// Make sure the plots don't keep
		scPlot = new Plot2DHelper("System Complexity", "Time", "Complexity", "Time", "Complexity");
		entPlot = new Plot2DHelper("Entropy", "Time", "Entropy", "Time", "Shannon Entropy", "Shannon Entropy (SC)",
				"Conditional Entropy");
		watPlot = new Plot2DHelper("WAT", "Time", "WAT", "Time", "WAT");
		perfPlot = new Plot2DHelper("PerfSit", "Time", "Performance", "Time", "Performance");
		AUPlot = new Plot2DHelper("AU", "Time", "Score", "Time", "Availability", "Unavilability");
		ODPlot = new Plot2DHelper("Oscillation Detector", "Time", "Oscillation Found", "Time", "Oscillation Found");
		TTPlot = new Plot2DHelper("TagAndTrack", "Time", "Result", "Time", "averageArea", "clustersIntersecting",
				"RunningUniqueClusters", "averageAgentDensity");
	}

	void addParameterSpec(String agentType, String stateName, String stateValue, String colour) {
		VAgentSpec vas = specs.get(agentType);
		if (specs.get(agentType) == null) {
			vas = new VAgentSpec(agentType);
			vas.addParameterSpec(stateName, stateValue, colour);
			specs.put(agentType, vas);
		} else {
			vas.addParameterSpec(stateName, stateValue, colour);
		}
	}

	/***** METRIC PRINTING ******/
	// void drawSimpleMetrics(int time, ArrayList<VEntity> currAgents,
	// DataCollector_MongoDB collector){
	// //System Complexity
	// SystemComplexity sc = new SystemComplexity();
	// ArrayList<VEntity> agentList_t = currAgents;
	//// HashMap<String, VAgent> agentMap_t = collector.buildVAgentMap(time);
	// ArrayList<Interaction> interactions_t =
	// collector.getAllInteractionsFromStep(time);
	//// InteractionGraph ig_t = collector.buildInteractionGraph(time);
	//
	//// ArrayList<VAgent> agentList_tm1 = collector.buildVAgentList(time-1);
	// HashMap<String, VEntity> agentMap_tm1 = collector.buildVAgentMap(time-1);
	// ArrayList<Interaction> interactions_tm1 =
	// collector.getAllInteractionsFromStep(time-1);
	// HashMap<String, ArrayList<Interaction>> interactionsMap_tm1 =
	// collector.getAgentInteractionMap(time-1);
	//// ig_t = collector.buildInteractionGraph(time);
	//
	// //SystemComplexity
	// sc.runMetric(interactions_tm1.size(), interactions_t.size());
	// double currentResults = (double)sc.getLatestResult();
	// scPlot.addPoints(time,currentResults);
	// displayPlot(scPlot);
	//
	// //Entropy: SE, SE (Change), CE,
	// Entropy entropyCalculator = new Entropy();
	//
	// double shannonEntropy =
	// entropyCalculator.shannonEntropy_Neighbours(agentList_t, dimensions);
	// double shannonEntropyChange =
	// entropyCalculator.shannonEntropy_Change(agentList_t, agentMap_tm1);
	// double conditionalEntropy = entropyCalculator.conditionalEntropy(agentList_t,
	// agentMap_tm1, dimensions);
	// entPlot.addPoints(time,shannonEntropy,shannonEntropyChange,conditionalEntropy);
	// displayPlot(entPlot);
	//
	// //Kaddoum WAT
	// double workingTime = agentList_t.size() * 8;
	// double watScore = SelfAdaptiveSystems.KaddoumWAT(agentList_t, agentMap_tm1,
	// interactionsMap_tm1, workingTime);
	// watPlot.addPoints(time, watScore);
	// displayPlot(watPlot);
	//
	// //PerfSit
	// double perfSit = SelfAdaptiveSystems.PerfSit(agentList_t, agentMap_tm1,
	// dimensions);
	// perfPlot.addPoints(time,perfSit);
	// displayPlot(perfPlot);
	//
	// //AU
	// double[] AU = Metric_VillegasAU(agentList_t, agentMap_tm1);
	// AUPlot.addPoints(time,AU[0],AU[1]);
	// displayPlot(AUPlot);
	//
	// //OD
	// int OD = OscillationDetector(agentList_t);
	// ODPlot.addPoints(time,OD);
	// displayPlot(ODPlot);
	//
	// //ClusterTrack
	//// ClusterTrack tt = new ClusterTrack();
	//// HashMap<String, Double> ttRes = tt.examineClusters(tt.dfsHelper(ig_t));
	//// //"averageArea","clustersIntersecting","RunningUniqueClusters","averageAgentDensity");
	//// TTPlot.addPoints(time,ttRes.get("AverageArea"),ttRes.get("ClustersIntersecting"),ttRes.get("RunningUniqueClusters"),ttRes.get("averageAgentDensity"));
	//// displayPlot(TTPlot);
	// }

	void displayPlot(Plot2DHelper plot) {
		if (plot.getWindowID() == null) {
			String w = API.plot2D(null, plot.getName(), plot.getPlot());
			plot.setWindowID(w);
		} else {
			API.plot2D(plot.getWindowID(), plot.getName(), plot.getPlot());
		}
	}
	// public double[] Metric_VillegasAU(ArrayList<VEntity> agents, HashMap<String,
	// VEntity> prevAgents ){
	//
	//
	// double MTTF = 0.0; //mean time to fail
	// //What if we consider that a failure is a non-statechange for some consec
	// //What if we consider that a faiure is the length of time with
	// non-consecutive changes
	// int failCounter = 0;
	//
	// double MTTR = 0.0; //mean time to recover
	// double A = 0.0;
	// double U = 0.0;
	// //What if we consider that a recovery is the length of a failure
	// int recoveryCounter = 0;
	//
	// for (VEntity v : agents){
	// boolean lifeState =
	// v.getParameterValueFromStringAsString("Alive").compareToIgnoreCase("true") ==
	// 0;
	// VEntity pv = prevAgents.get(v.getName());
	// if (pv == null){
	// System.out.println("Agent didnt exist...");
	// continue;
	// }
	// boolean prevState =
	// pv.getParameterValueFromStringAsString("Alive").compareToIgnoreCase("true")
	// == 0;
	// if (lifeState == prevState){
	// Integer agentUpTime = theAgentsUptime.get(v.getName());
	// Integer agentDowntime = theAgentsDowntime.get(v.getName());
	//
	// if (agentUpTime == null){
	// theAgentsUptime.put(v.getName(), 0);
	//// agentUpTime = theAgentsUptime.get(v.getName());
	// }
	// if (agentDowntime == null){
	// theAgentsDowntime.put(v.getName(),0);
	//// agentDowntime = theAgentsDowntime.get(v.getName());
	// }
	// if (agentDowntime != null && agentUpTime != null){
	// //Has now entered a "downtime" state
	// if (agentDowntime == consecutiveDowntime){
	// MTTF += agentUpTime;
	// failCounter++;
	// theAgentsUptime.put(v.getName(),0);
	// theAgentsDowntime.put(v.getName(), agentDowntime + 1);
	//// System.out.println("MTTF: "+MTTF);
	// } else {
	//// theAgentsUptime.put(v.getName(), 0);
	// theAgentsDowntime.put(v.getName(), agentDowntime + 1);
	// }
	// }
	//
	// } else {
	// Integer agentUpTime = theAgentsUptime.get(v.getName());
	// Integer agentDowntime = theAgentsDowntime.get(v.getName());
	//
	// if (agentUpTime == null){
	// theAgentsUptime.put(v.getName(), 0);
	//// agentUpTime = theAgentsUptime.get(v.getName());
	// }
	// if (agentDowntime == null){
	// theAgentsDowntime.put(v.getName(),0);
	//// agentDowntime = theAgentsDowntime.get(v.getName());
	// }
	//
	// if (agentDowntime != null && agentUpTime != null){
	// if (agentDowntime >= consecutiveDowntime){
	// MTTR += agentDowntime;
	// recoveryCounter++;
	// theAgentsDowntime.put(v.getName(),0);
	// theAgentsUptime.put(v.getName(), agentUpTime + 1);
	// } else {
	// theAgentsDowntime.put(v.getName(),0);
	// theAgentsUptime.put(v.getName(), agentUpTime + 1);
	// }
	// }
	// }
	// }
	//
	// if (failCounter == 0){
	// MTTF = 0;
	// } else {
	// MTTF = MTTF / (double)failCounter;
	// }
	// if (recoveryCounter == 0){
	// MTTR = 0;
	// } else {
	// MTTR = MTTR / (double)recoveryCounter;
	// }
	//
	// if ((MTTR + MTTF) == 0){
	// A = 0.5;
	// U = 0.5;
	// } else {
	// A = MTTF / (MTTF + MTTR); //availability
	// U = MTTR / (MTTF + MTTR); //unavailability
	// //A + U = 1
	// }
	// double[] res = new double[2];
	// res[0] = A;
	// res[1] = U;
	// return res;
	//
	//
	// }
	//
	//
	// public int OscillationDetector(ArrayList<VEntity> agents){
	// Collections.sort(agents,VEntity.sortByName()); //Sort by name or position?
	// BitSet bs = new BitSet(agents.size());
	//
	// for (int i = 0; i < agents.size(); i++){
	// bs.set(i,
	// agents.get(i).getParameterValueFromStringAsString("Alive").compareToIgnoreCase("true")
	// == 0);
	// }
	// bitsOverTime.add(bitsetCounter, bs);
	// boolean hit = false;
	//
	// for (int t = bitsetCounter - 1; t >= 0; t--){
	// BitSet xor = (BitSet) bitsOverTime.get(t).clone();
	// xor.xor(bs);
	// int diff = xor.length()-1;
	// if (diff == -1){
	// if (!hit){
	// int dist = currentTime - t;
	//// if (dist > distance){
	//// distance = dist;
	//// maxDistStart = t;
	//// }
	////
	//// if (dist < minDistance){
	//// minDistance = dist;
	//// }
	// hit = true;
	// }
	//// if (t == currentTime - 1) {
	//// consecutive++;
	//// }
	//
	// if (hit){
	// break;
	// }
	// }
	// }
	//
	// bitsetCounter++;
	// if (hit){
	// return 1;
	// } else {
	// return 0;
	// }
	// }

}

class Plot2DHelper {
	Plot2D thePlot;
	String windowID = null;
	String name;

	public Plot2DHelper(String name, String xLabel, String yLabel, String... values) {
		this.name = name;
		thePlot = new Plot2D(values);
		thePlot.setXLabel(xLabel);
		thePlot.setYLabel(yLabel);
	}

	public void addPoints(double... values) {
		thePlot.addPoints(values);
	}

	public void setWindowID(String w) {
		windowID = w;
	}

	public String getWindowID() {
		return windowID;
	}

	public Plot2D getPlot() {
		return thePlot;
	}

	public String getName() {
		return name;
	}

}

class VSystem {
	String systemName;
	HashMap<Integer, VSystemAtTime> stepInformation;

	VSystem() {
		stepInformation = new HashMap<Integer, VSystemAtTime>();
	}

	void fillSystem(DataCollector_MongoDB collector, int terminationTime) {
		for (int time = 0; time < terminationTime; time++) {
			VSystemAtTime thisStep = new VSystemAtTime(time, collector);
			stepInformation.put(time, thisStep);
		}
	}

	VSystemAtTime getVSystemAtTime(int time) {
		return stepInformation.get(time);
	}
}

class VSystemAtTime {
	int time;
	ArrayList<VEntity> agentList;
	HashMap<String, VEntity> agentMap;
	ArrayList<Interaction> interactions;
	InteractionGraph ig;

	VSystemAtTime(int time, DataCollector_MongoDB collector) {
		agentList = collector.buildVAgentList(time);
		agentMap = collector.buildVAgentMap(time);
		interactions = collector.getAllInteractionsFromStep(time);
		ig = collector.buildInteractionGraph(time);
	}

	public int getTime() {
		return time;
	}

	public ArrayList<VEntity> getAgentList() {
		return agentList;
	}

	public HashMap<String, VEntity> getAgentMap() {
		return agentMap;
	}

	public ArrayList<Interaction> getInteractions() {
		return interactions;
	}

	public InteractionGraph getIg() {
		return ig;
	}
}

// TODO
// CellAlivetrue, Black
// CellAlivefalse, White
class VAgentSpec {
	String agentType;
	HashMap<String, StateValueColours> parameterTargets;

	VAgentSpec(String agentType) {
		this.agentType = agentType;
		parameterTargets = new HashMap<String, StateValueColours>();
	}

	void addParameterSpec(String stateName, String stateValue, String colour) {
		parameterTargets.put(stateName, new StateValueColours());
	}

	Color getColor(String stateName, String stateValue) {
		String col = parameterTargets.get(stateName).getColorString(stateValue);
		if (col == null) {
			return null;
		} else {
			return Color.getColor(col);
		}

	}
}

// TODO
class StateValueColours {
	String stateName;
	String values;
	String colours;

	StateValueColours() {
		colours = "";
	}

	String getColorString(String stateValue) {
		return colours;
	}
}
