package castleComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

import org.bson.Document;
import stdSimLib.Parameter;
import stdSimLib.utilities.Utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.bson.BasicBSONEncoder;
import com.mongodb.DBObject;

public class Logger {

	private boolean muted = true;

	// Output paths
	private String systemLogPath;
	private String systemSpecPath;
	private String systemLogDirPath;
	private String systemOutputDirPath;
	private String systemStepInfoDir;
	private String sysName;

	private StringBuffer stringBuilder;
	private Output output;

	private SimulationInfo simInfo;

	Document doc;
	private ConcurrentHashMap<String, List<Document>> documentStorage;

	private String systemTerminationInfoPath;

	public Logger(Output op, SimulationInfo simInfo) {
		this.output = op;
		this.simInfo = simInfo;
		documentStorage = new ConcurrentHashMap<String, List<Document>>();
	}

	public void setOutput(Output op) {
		this.output = op;
	}

	public void mute() {
		muted = true;
	}

	public void unmute() {
		muted = false;
	}

	public void setup(String filePath, String sysName) {
		// muted = isMuted;
		// output.setLoggingToConsole(toConsole);
		// output.setLoggingToFile(toFile);
		systemLogPath = filePath;
		this.sysName = sysName;

		if (output.isLoggingToFile() || output.isWritingModelDataToFile()) {
			stringBuilder = new StringBuffer();
			setUpLog(systemLogPath);
		}
	}

	public void setupFromOutput() {
		if (output == null) {
			System.out.println("Output is null, please fix the generator.");
			return;
		}
	}

	public void newStep(int stepNumber) {
		output.sendLogToConsole("\n***************\nStep " + stepNumber);
		stringBuilder = new StringBuffer();
		// stringBuilder.append("Step " + stepNumber + "\n");
		stringBuilder.append("{ \"Step-Number\" : " + stepNumber + " }\n");
		doc = new Document();
		documentStorage = new ConcurrentHashMap<String, List<Document>>();
		doc.append("step-number", stepNumber);
	}

	public void endOfStep(int stepNumber) {
		if (output.isLoggingToFile()) {
			if (stringBuilder.length() != 0) {
				output.sendStringToFile(systemStepInfoDir + "/Step" + stepNumber + ".txt", stringBuilder.toString(),
						false);
			}
		}
		if (output.isWritingModelDataToFile()) {
			// Empty out document storage and turn into string
			for (String v : documentStorage.keySet()) {
				doc.append(v, documentStorage.get(v));
			}

			if (doc != null) {

				// output.sendStringToFile(systemStepInfoDir + "/Step" + stepNumber + ".json",
				// Utilities.compressString(doc.toJson()), false);
				output.sendStringToCompressedFile(systemStepInfoDir + "/Step" + stepNumber + ".json", doc.toJson());
			}
		}
	}

	// This can overwrite things since the SYSTEM has power
	// TODO
	public void systemLog(String str) {

	}

	// Prints to file
	public void log(String str) {
		if (output.isLoggingToFile()) {
			logToFile(str + "\n");
		}
		if (output.isLoggingToConsole()) {
			logToConsole(str);
		}
	}

	public void logWithOptionalWrite(String str) {
		if (output.isLoggingToFile()) {
			logToFile(str + "\n");
		}

		logToConsole(str);

	}

	public void logToConsole(String str) {
		output.sendLogToConsole(str.toString());
	}

	public void log(StringBuffer str) {
		if (output.isLoggingToFile()) {
			logToFile(str);
		}
		if (output.isLoggingToConsole()) {
			logToConsole(str.toString());
		}
	}

	public void logToFile(StringBuffer sb) {
		if (sb.length() <= 0) {
			System.out.println("I HAVE NOT LEN");
		}
		stringBuilder.append(sb.toString() + "\n");
	}

	public void logToFile(String str) {
		stringBuilder.append(str + "\n");
	}

	public void logToFileFromDocument(String key, Document d) {
		if (d == null) {
			System.err.println("d is null");
			System.exit(0);
		}
		List<Document> docs = documentStorage.get(key);
		if (docs == null) {
			docs = Collections.synchronizedList(new ArrayList<Document>());
			documentStorage.put(key, docs);
			docs = documentStorage.get(key);
		}
		docs.add(d);
	}

	public void writeModelData(StringBuffer sb) {
		if (output.isWritingModelDataToConsole()) {
			logToConsole(sb.toString());
		}
		if (output.isWritingModelDataToFile()) {
			logToFile(sb);
		}
	}

	// Sets up the log path (should be fully automated)
	public void setUpLog(String str) {
		systemLogPath = str;
		systemOutputDirPath = systemLogPath + "/" + simInfo.getExecutionID().replaceAll("\\s+", "")
				+ Utilities.generateRandomChar() + Utilities.generateRandomChar();
		systemSpecPath = systemOutputDirPath + "/systemInitialization.json";
		systemLogDirPath = systemOutputDirPath + "/systemLog.txt";
		systemStepInfoDir = systemOutputDirPath + "/steps";
		systemTerminationInfoPath = systemOutputDirPath + "/termination-statistics.json";

		if (output.isLoggingToFile() || output.isWritingModelDataToFile()) {
			// Create directories and initial files
			System.out.println("Create directories and files for logging");
			System.out.println("Log directory at " + systemLogPath);
			// Create main log dir if doesn't exist
			output.initialiseLoggingPath(systemLogPath, true);

			output.initialiseLoggingPath(systemOutputDirPath, true);
			output.initialiseLoggingPath(systemStepInfoDir, true);

			output.initialiseLoggingPath(systemSpecPath, false);
			output.initialiseLoggingPath(systemTerminationInfoPath, false);

			output.initialiseLoggingPath(systemLogDirPath, false);
		}
	}

	public void endOfSimulation(int finalStep, long elapsedTime, int totalSteps) {
		Document dd = new Document("_id", "termination-statistics").append("termination-step", finalStep)
				.append("%-of-execution-finished", (((double) finalStep) / ((double) totalSteps) * 100))
				.append("elapsed-time", elapsedTime);
		output.sendStringToCompressedFile(systemTerminationInfoPath, dd.toJson());
		// output.sendStringToFile(systemTerminationInfoPath,
		// Utilities.compressString(dd.toJson()), false);

	}

	public void writeSystemSpecs(String sysName, String sysDescription, List<Parameter<?>> params) {
		String out = "Simulation Initialization Details: \n";
		out += "Name: " + sysName + "\n";
		out += "Description: " + sysDescription + "\n";
		out += "Execution Start Time: " + Utilities.generateNiceTimeStamp() + "\n";
		out += "Initialization Parameters:\n";
		for (Parameter<?> p : params) {
			out += "\t" + p.toString() + "\n";
		}
		out += "----------------------------------------";

		output.sendLogToConsole(out);
		output.sendStringToFile(systemSpecPath, out, false);
	}

	public void writeSystemSpecsToFile(SimulationInfo si, List<Parameter<?>> params) {
		Document dd = new Document("_id", "system-initialisation");
		dd.append("name", si.getSystemName());
		dd.append("description", si.getDescription());
		dd.append("execution-start-time", si.getTimeStamp());

		ArrayList<Document> pdList = new ArrayList<Document>();
		for (Parameter<?> p : params) {
			String name = p.getName();
			String value = p.getCurrentValue();
			String type = p.getType();
			Document pdd = new Document("parameter-name", name).append("parameter-value", value)
					.append("parameter-type", type);
			pdList.add(pdd);
		}

		dd.append("initialisation-parameters", pdList);
		output.sendStringToFile(systemSpecPath, dd.toJson(), false);

	}

	public String getSystemTerminationInfoPath() {
		return systemTerminationInfoPath;
	}
}

class BSONFileWriter {

	private final String path;
	private final BasicBSONEncoder encoder;

	public BSONFileWriter(String path) {
		this.path = path;
		this.encoder = new BasicBSONEncoder();
	}

	public void write(DBObject dbo) throws IOException {

		Files.write(Paths.get(path), encoder.encode(dbo), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

	}

}
