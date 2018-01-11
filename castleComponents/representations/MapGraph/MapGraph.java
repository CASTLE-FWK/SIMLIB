package castleComponents.representations.MapGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import castleComponents.Entity;
import castleComponents.objects.Range2D;
import castleComponents.objects.Vector2;
import castleComponents.objects.List;
import castleComponents.representations.LayoutParameters;
import stdSimLib.utilities.Utilities;

public class MapGraph {
	// Look at all the stuff I have to clone
	HashMap<Long, Node> nodes;
	HashMap<Vector2, Node> nodesMap;
	HashMap<Long, Link> links;
	HashMap<String, Edge> edges;
	Vector2 geoBoundingBox_Min;
	Vector2 geoBoundingBox_Max;
	Vector2 boundingBox_Min;
	Vector2 boundingBox_Max;
	HashMap<String, Entity> entitiesInMap;
	List<Node> transitPoints;
	List<TrafficLight> trafficLightLocations;
	List<Node> carParkNodes;
	long id;
	List<MapGraph> subMaps;
	Range2D range;

	public MapGraph() {
		links = new HashMap<Long, Link>();
		edges = new HashMap<String, Edge>();
		nodes = new HashMap<Long, Node>();
		nodesMap = new HashMap<Vector2, Node>();
		entitiesInMap = new HashMap<String, Entity>();
		transitPoints = new List<Node>(); // TODO Populate this list
		trafficLightLocations = new List<TrafficLight>(); // TODO populate this list
		carParkNodes = new List<Node>(); // TODO Populate this list
		id = -1;
		subMaps = new List<MapGraph>();
	}

	public void setID(long id) {
		this.id = id;
	}

	public long getID() {
		return this.id;
	}

	public Range2D getRange() {
		return range;
	}

	public void initialize(String pathToMapFile, LayoutParameters lp) {
		importMap(pathToMapFile);

		// Populate edges list
		HashSet<Link> theLinks = new HashSet<Link>(links.values());
		for (Link l : theLinks) {
			List<Edge> lEdges = l.getEdges();
			for (Edge e : lEdges) {
				edges.put(e.getID(), e);
			}
		}
	}

	public void importMap(String path) {
		MapGraphParser mgp = new MapGraphParser(this);
		mgp.parseMapGraph(path);
	}

	/** How to move Entities around **/
	// TODO HERE
	public Outcome moveEntity(Entity e, Edge currEdge, double moveDist, double distanceAlongEdge, Node destNode) {
		// How do we update the position along the edge?
		double newDist = distanceAlongEdge + moveDist;
		Outcome outcome = null;
		if (newDist > currEdge.getDistanceInKM()) {
			// TODO Needs to move to new node and update the destNode

		} else if (newDist > 1001209) {

		} else {
			outcome = new Outcome(OutcomeResult.VALID, newDist, destNode, this);
		}

		return outcome;

	}

	// TODO
	public Vector2 calculateEntitiesPosition(Edge currEdge, double distanceAlongEdge, Node destNode) {
		errLog("calculateEntitiesPosition is incomplete");

		return null;
	}

	// TODO
	public Node determineNextNode(Node currNode, Vector2 finalDestination) {
		errLog("determineNextNode is incomplete");
		return null;
	}

	public boolean addEntity(Entity e, Vector2 pos) {
		// Is pos oob?
		// Find the Node closest to pos and add there
		Node cand = getNodeAtPosition(pos);
		if (cand != null) {
			// TODO Add here some how

			entitiesInMap.put(e.getID(), e);
			return true;
		}

		return false;
	}

	public void calculateBounds() {
		HashSet<Node> theNodes = new HashSet<Node>(nodes.values());
		Vector2 min = new Vector2(Double.MAX_VALUE, Double.MAX_VALUE);
		Vector2 max = new Vector2(-Double.MAX_VALUE, -Double.MAX_VALUE);

		for (Node n : theNodes) {
			Vector2 coords = n.getGeoCoords();
			if (coords.getX() < min.getX()) {
				min.setX(coords.getX());
			}
			if (coords.getX() > max.getX()) {
				max.setX(coords.getX());
			}
			if (coords.getY() < min.getY()) {
				min.setY(coords.getY());
			}
			if (coords.getY() > max.getY()) {
				max.setY(coords.getY());
			}
		}
		geoBoundingBox_Min = new Vector2(min);
		geoBoundingBox_Max = new Vector2(max);
		printBounds();

	}

	public void normalise() {
		HashSet<Node> theNodes = new HashSet<Node>(nodes.values());
		double geoBBWidth = geoBoundingBox_Max.getX() - geoBoundingBox_Min.getX();
		double geoBBHeight = geoBoundingBox_Max.getY() - geoBoundingBox_Min.getY();
		double bbWidth = Link.calculateCoordinateDistance(
				new Vector2(geoBoundingBox_Min.getX(), geoBoundingBox_Max.getY()), geoBoundingBox_Max);
		double bbHeight = Link.calculateCoordinateDistance(
				new Vector2(geoBoundingBox_Max.getX(), geoBoundingBox_Min.getY()), geoBoundingBox_Max);

		for (Node n : theNodes) {
			Vector2 coords = n.getGeoCoords();
			double xPerc = (coords.getX() - geoBoundingBox_Min.getX()) / geoBBWidth;
			double yPerc = (coords.getY() - geoBoundingBox_Min.getY()) / geoBBHeight;
			xPerc = bbWidth * xPerc;
			yPerc = bbHeight * yPerc;
			n.setCoords(new Vector2(xPerc, yPerc));
			nodesMap.put(n.getCoords(), n);
		}

		boundingBox_Min = new Vector2(0, 0);
		boundingBox_Max = new Vector2(bbWidth, bbHeight);
		range = new Range2D(boundingBox_Min, new Vector2(boundingBox_Min.getX(), boundingBox_Max.getY()),
				new Vector2(boundingBox_Max.getX(), boundingBox_Min.getY()),
				new Vector2(boundingBox_Max.getX(), boundingBox_Max.getY()));
	}

	// TODO
	public void clone(MapGraph mg, LayoutParameters lp) {

	}

	// TODO SubGraphs
	public void createSubMaps(int num) {
		errLog("createSubMaps is incomplete");
		if (num == 1) {
			subMaps.add(this);
		}
	}

	public List<MapGraph> getSubMapsAsList() {
		return subMaps;
	}

	// TODO
	public List<Range2D> getSubMapRangesAsList() {
		errLog("getSubMapRangesAsList is incomplete");
		List<Range2D> ranges = new List<Range2D>();
		for (MapGraph mg : subMaps) {
			ranges.add(mg.getRange());
		}
		return ranges;
	}

	// TODO
	public List<Entity> getEntitiesOfTypeAtPos(String type, Vector2 pos) {
		errLog("getEntitiesOfTypeAtPos is incomplete");
		return null;
	}

	// TODO This might return funky nulls
	public Node getNodeAtPosition(Vector2 pos) {
		return nodesMap.get(pos);
	}

	// TODO
	public void changeNodeType(Vector2 pos, String type) {
		errLog("changeNodeType is incomplete");
	}

	// TODO
	public Node findNearestNodeOfType(Vector2 pos, String type) {
		errLog("findNearestNodeOfType is incomplete");
		switch (type) {

		default:
			break;
		}

		return null;
	}

	// TODO
	public MapGraph extractMapSection(Range2D range) {
		errLog("extractMapSection is incomplete");
		return null;
	}

	// TODO
	public void changeSectionOfMapToType(String eventName, String type) {
		errLog("changeSectionOfMapToType is incomplete");
	}

	// TODO
	public void replaceSectionOfMap(Range2D r, MapGraph oldMapData) {
		errLog("replaceSectionOfMap is incomplete");
	}

	// TODO
	public int countEntitiesInRangeWithType(Entity e, double range, String theType) {
		errLog("countEntitiesInRangeWithType is incomplete");

		return -1;
	}

	// TODO
	public Park getCarParkAtPosition(Vector2 pos) {
		errLog("getCarParkAtPosition is incomplete");
		return null;
	}

	public Vector2 getPositionOfEntity(Entity e) {
		return null;
	}

	public String printMap() {
		String str = "";
		return str;
	}

	public void printBounds() {
		System.out.println("MapGraph Bounds [ min=" + geoBoundingBox_Min + ", max=" + geoBoundingBox_Max + " ]");
	}

	public List<Node> getTransitPoints() {
		return transitPoints;
	}

	public void addTransitPoint(Node n) {
		transitPoints.add(n);
	}

	public List<TrafficLight> getListOfTrafficLights() {
		return trafficLightLocations;
	}

	public void addTrafficLight(Node n) {
		// TODO something about adding traffic lights
		errLog("addTrafficLight is incomplete");
		trafficLightLocations.add(null);
	}

	public List<Node> getCarParkNodes() {
		return carParkNodes;
	}

	public List<Vector2> getCarParkLocations() {
		List<Vector2> v = new List<Vector2>();
		for (Node n : carParkNodes) {
			v.add(n.getCoords());
		}
		return v;
	}

	public void addCarParkLocation(Node n) {
		carParkNodes.add(n);
	}

	/**
	 * @return the nodes
	 */
	public ArrayList<Node> getNodesAsList() {
		return new ArrayList<Node>(nodes.values());
	}

	/**
	 * @return the links
	 */
	public HashMap<Long, Link> getLinks() {
		return links;
	}

	public void addNode(Node n) {
		nodes.put(n.getID(), n);
	}

	public Node findNode(Long id) {
		return nodes.get(id);
	}

	public void addLink(Link e) {
		links.put(e.getID(), e);
	}

	public void addEdge(Edge e) {
		edges.put(e.getID(), e);
	}

	@Override
	public String toString() {
		return "MapGraph [ size of nodesMap: " + nodes.size() + ", size of links: " + links.size() + ", size in km: "
				+ boundingBox_Max + "]";
	}

	public void errLog(Object o) {
		System.err.println("MapGraph Warning: " + o.toString());
	}
}