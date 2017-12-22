package visualisation;
//
//import java.util.Scanner;
public class VisualisationClient {}
//	static double timeGap = 500;
//	
//	final static String PULSAR = "game_of_life_20160731143812bv";
//	final static String GLIDER = "game_of_life_20160731143838pk";
//	final static String GLIDER_GUN = "game_of_life_20160731144112ma";
//	final static String RANDOM1 = "game_of_life_20160731143852qq";
//	final static String RPENT = "game_of_life_20160731144018iu";
//	final static String RANDOM2 = "game_of_life_20160731144143zd";
//	final static String RANDOM3 = "game_of_life_20160731144233pq";
//	final static String RANDOM671 = "game_of_life_20160801101601ha";
//	
//	
//	public static void main(String[] args){
//		DataSetSimulator dss = new DataSetSimulator();
//		DSSCycler cycler = new DSSCycler(dss);
//		cycler.setTimeGap(timeGap);
//		
//		dss.newSimulation(RANDOM3);
//		dss.begin();
//		Scanner keyboard = new Scanner(System.in);
//		char keyGet = 'x';
//		while (keyGet != 'q'){
//			String nextLine = keyboard.nextLine();
//			if (nextLine.length() <= 0){
//				continue;
//			}
//			keyGet = nextLine.charAt(0);
//		
//			if (keyGet == 'd'){
//				dss.stepForward();
//			} else if (keyGet == 'a'){
//				dss.stepBack();
//			} else if (keyGet == 'p'){
//				cycler = new DSSCycler(dss);
//				cycler.setTimeGap(timeGap);
//				cycler.startCycling();				
//				cycler.start();
//			} else if (keyGet == 'o'){
//				cycler.stopCycling();				
//			} else if (keyGet == 'r'){
//				cycler.stopCycling();				
//				dss.restart();
//			} else if (keyGet == 't'){
//				String[] split = nextLine.split(" ");
//				dss.stepToTime(Integer.parseInt(split[1]));
//			} else if (nextLine.compareToIgnoreCase("faster") == 0){
//				timeGap -= 50;
//				if (timeGap <= 0){
//					timeGap = 0;
//				}
//				System.out.println("Time gap is now "+timeGap +" millis");
//				cycler.setTimeGap(timeGap);
//			} else if (nextLine.compareToIgnoreCase("slower") == 0){
//				timeGap += 50;
//				if (timeGap >= 5000){
//					timeGap = 5000;
//				}
//				System.out.println("Time gap is now "+timeGap +" millis");
//				cycler.setTimeGap(timeGap);
//			} else if (nextLine.compareToIgnoreCase("fasterr") == 0){
//				timeGap = 0;
//				System.out.println("Time gap is now "+timeGap +" millis");
//				cycler.setTimeGap(timeGap);
//			}
//		}
//		
//	}
//}
//
//class DSSCycler extends Thread{
//	double timeGap;
//	boolean active = false;
//	DataSetSimulator dss;
//	public DSSCycler(DataSetSimulator dss){
//		this.dss = dss;
//	}
//	
//	public void setTimeGap(double tg){
//		this.timeGap = tg;
//	}
//	
//	public void run(){
//		while (active){
//			cycle();
//		}
//	}
//	
//	public void cycle(){		
//		try {
//			dss.stepForward();
//			sleep((long)timeGap);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public void startCycling(){
//		active = true;
//	}
//	
//	public void stopCycling(){
//		active = false;
//	}
//}
