package interLib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import castleComponents.objects.Vector2;


/*
 * A bunch of static utilities that could be useful in
 * the running of a simulation.
 *	//TODO: Document these.
 */
public class Utilities {
	
	/*Static constants*/
	public static String NODE_STRING_NAME = "*node";
	public static String EDGE_STRING_NAME = "*edges";
	
	public static double NEW_AGENT_START = -Double.MAX_VALUE;
	
	public static String GENERATE_NAME = "DEFAULT";
	
	public static long uid = -1;

	/*Useful methods for metrics and simulations*/
	
	private static Random random = new XSRandom(System.currentTimeMillis());
	private static final double FIFTY_PERCENT = 0.5;
	
	
	public static double generateRandomRangeDouble(double min, double max){
		if (min > max){
			double tmp = min;
			min = max;
			max = tmp;
		} else if (min == max){
			return min;
		}
		
		return (min + (random.nextDouble() * ((max - min) + 1))); 
	}
	
	public static int generateRandomRangeInteger(int min, int max){
		if (min > max){
			int tmp = min;
			min = max;
			max = tmp;
		} else if (min == max){
			return min;
		}
//		return CASRandom.random((max - min) + 1) + min;
		return random.nextInt((max - min) + 1) + min;
	}
	
	public static boolean generateWithProbabilty(double probability){
		return (random.nextDouble() < probability);
	}
	
	public static boolean generateCoinFlip(){
		return generateWithProbabilty(FIFTY_PERCENT);
	}
	
	public static float generateRandomFloat(){
		return random.nextFloat();
	}
	
	public static Vector2 randomiseVectorDouble(double xMin, double xMax, double yMin, double yMax){	
		double X = generateRandomRangeDouble(xMin, xMax);
		double Y = generateRandomRangeDouble(yMin, yMax);
		
		return new Vector2(X,Y);
	}
	
	public static Vector2 randomiseVectorDouble(double min, double max){	
		return randomiseVectorDouble(min,max,min,max);
	}
	
	//TODO: This is probably more useless than originally thought.
	public static Vector2 randomiseVectorInteger(int xMin, int xMax, int yMin, int yMax){	
		double X = generateRandomRangeInteger(xMin, xMax);
		double Y = generateRandomRangeInteger(yMin, yMax);
		
		return new Vector2(X,Y);
	}
	
	//TODO: Explain this one well.
	public static Vector2 randomiseBooleanVector(double xMin, double xMax, double yMin, double yMax){
		double X, Y;
		if (generateCoinFlip()){
			X = xMin;
		} else {
			X = xMax;			
		}
		
		if (generateCoinFlip()){
			Y = yMin;
		} else {
			Y = yMax;
		}
		
		return new Vector2(X,Y);			
	}
	
	public static Vector2 randomiseVectorInteger(int min, int max){			
		return randomiseVectorInteger(min,max,min,max);
	}
	
	public static String generateTimeStamp(){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public static double calculateDistance2D(Vector2 vectorA, Vector2 vectorB){
		return Math.sqrt(Math.pow(vectorA.getX() - vectorB.getX(), 2) + Math.pow(vectorA.getX() - vectorB.getX(),2));
	}
	
	public static void writeToFile(String fileContents, String absoluteFilePath){
		File outputFile = new File(absoluteFilePath);	
		System.out.println("Writing file to: " + absoluteFilePath);
		
		try{			
			PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputFile.getAbsoluteFile(), false)));
			printWriter.print(fileContents);
			printWriter.close();
			
		} catch (IOException exc){
			exc.printStackTrace();
		}
	}
	
	public static long generateUID(){
		uid++;
		return uid;
	}
	
	public static String generateID(){
		Long l = new Long(System.currentTimeMillis());
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(l.byteValue());
			String id = new String(byteArrayToHexString(md.digest()));
			return id;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "ERROR: FAILED ID GENERATION";
		
	}
	
	public static String generateTimeID(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(formatter)+Character.toChars(generateRandomRangeInteger(97, 122))[0]+Character.toChars(generateRandomRangeInteger(97, 122))[0]; 		
	}
	
	public static String byteArrayToHexString(byte[] b) {
		String result = "";
		for (int i=0; i < b.length; i++) {
			result +=
	      	Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}
	
	public static BufferedReader getFileAsBufferedReader(String filePath){
		try {
			return new BufferedReader(new FileReader(filePath));
		} catch (FileNotFoundException e) {			
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static double calculateMin(List<Double> list){
		if (list.size() <= 0){
			return 0;
		}
		Collections.sort(list);
		return list.get(0);
	}
	
	public static double calculateMax(List<Double> list){
		if (list.size() <= 0){
			return 0;
		}
		Collections.sort(list);
		
		return list.get(list.size()-1);
	}
	
	public static double calculateMean(List<Double> list){
		double sum = 0.0;
		for (Double d : list){
			sum += d;
		}
		return (sum/(double)list.size());
	}
	
	public static double calculateSTDDev(List<Double> list){
		double mean = calculateMean(list);
		double temp = 0.0;
		for (Double d : list){
			temp += (mean - d)*(mean - d);
		}
		double variance = temp/list.size();		
		return Math.sqrt(variance);
	}
	
	public static double calculateMedian(List<Double> list){
		double med = 0.0;
		Collections.sort(list);
		if (list.size() % 2 == 0){
			double x = list.get(list.size()/2);
			double y = list.get(list.size()/2 - 1);
			med = (x+y)/2.0;
		} else { 
			med = list.get(list.size()/2);
		}
		
		return med;		
	}
	
	public static double calculateMin(double[] list){
		Arrays.sort(list);
		return list[0];
	}
	
	public static double calculateMax(double[] list){
		Arrays.sort(list);
		return list[list.length-1];
	}
	
	public static double calculateMean(double[] list){
		double sum = 0.0;
		for (Double d : list){
			sum += d;		
		}
		return (sum/list.length);
	}
	
	public static double calculateSTDDev(double[] list){
		double mean = calculateMean(list);
		double temp = 0.0;
		for (Double d : list){
			temp += (mean - d)*(mean - d);
		}
		double variance = temp/list.length;		
		return Math.sqrt(variance);
	}
	
}
