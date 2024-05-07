import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class TripPoint {

	private double lat;	// latitude
	private double lon;	// longitude
	private int time;	// time in minutes
	
	private static ArrayList<TripPoint> trip;	// ArrayList of every point in a trip
	private static ArrayList<TripPoint> movingTrip;

	// default constructor
	public TripPoint() {
		time = 0;
		lat = 0.0;
		lon = 0.0;
	}
	
	// constructor given time, latitude, and longitude
	public TripPoint(int time, double lat, double lon) {
		this.time = time;
		this.lat = lat;
		this.lon = lon;
	}
	
	// returns time
	public int getTime() {
		return time;
	}
	
	// returns latitude
	public double getLat() {
		return lat;
	}
	
	// returns longitude
	public double getLon() {
		return lon;
	}
	
	// returns a copy of trip ArrayList
	public static ArrayList<TripPoint> getTrip() {
		return new ArrayList<>(trip);
	}

	public static ArrayList<TripPoint> getMovingTrip(){
		return new ArrayList<>(movingTrip);
	}
	
	// uses the haversine formula for great sphere distance between two points
	public static double haversineDistance(TripPoint first, TripPoint second) {
		// distance between latitudes and longitudes
		double lat1 = first.getLat();
		double lat2 = second.getLat();
		double lon1 = first.getLon();
		double lon2 = second.getLon();
		
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) *
                   Math.cos(lat1) *
                   Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
	}
	
	// finds the average speed between two TripPoints in km/hr
	public static double avgSpeed(TripPoint a, TripPoint b) {
		
		int timeInMin = Math.abs(a.getTime() - b.getTime());
		
		double dis = haversineDistance(a, b);
		
		double kmpmin = dis / timeInMin;
		
		return kmpmin*60;
	}

	public static double avgMovingSpeed(){
		double distance = 0.0;

		for (int i = 1; i < movingTrip.size(); i++){
			distance = distance + haversineDistance(movingTrip.get(i - 1), movingTrip.get(i));
		}

		return distance / movingTime();
	}
	
	// returns the total time of trip in hours
	public static double totalTime() {
		int minutes = trip.get(trip.size()-1).getTime();
		double hours = minutes / 60.0;
		return hours;
	}

	public static double movingTime(){
		double minutes = 0;
		/* 
		for (int i = 1; i < movingTrip.size(); i++){
			if (movingTrip.get(i).getTime() - movingTrip.get(i - 1).getTime() == 5){
				minutes = minutes + (movingTrip.get(i).getTime() - movingTrip.get(i - 1).getTime());
			}
			if ((i < movingTrip.size() - 1) && (movingTrip.get(i + 1).getTime() - movingTrip.get(i).getTime() != 5)){
				minutes = minutes + 5;
			}

		}
		*/
		minutes = movingTrip.size() * 5.0;

		return (minutes - 5) / 60.0;
	}

	public static double stoppedTime(){
		return totalTime() - movingTime();
	}
	
	// finds the total distance traveled over the trip
	public static double totalDistance() throws FileNotFoundException, IOException {
		
		double distance = 0.0;
		
		if (trip.isEmpty()) {
			readFile("triplog.csv");
		}
		
		for (int i = 1; i < trip.size(); ++i) {
			distance += haversineDistance(trip.get(i-1), trip.get(i));
		}
		
		return distance;
	}
	
	public String toString() {
		
		return null;
	}

	public static void readFile(String filename) throws FileNotFoundException, IOException {

		// construct a file object for the file with the given name.
		File file = new File(filename);

		// construct a scanner to read the file.
		Scanner fileScanner = new Scanner(file);
		
		// initiliaze trip
		trip = new ArrayList<TripPoint>();

		// create the Array that will store each lines data so we can grab the time, lat, and lon
		String[] fileData = null;

		// grab the next line
		while (fileScanner.hasNextLine()) {
			String line = fileScanner.nextLine();

			// split each line along the commas
			fileData = line.split(",");

			// only write relevant lines
			if (!line.contains("Time")) {
				// fileData[0] corresponds to time, fileData[1] to lat, fileData[2] to lon
				trip.add(new TripPoint(Integer.parseInt(fileData[0]), Double.parseDouble(fileData[1]), Double.parseDouble(fileData[2])));
			}
		}

		// close scanner
		fileScanner.close();
	}

	public static int h1StopDetection() throws FileNotFoundException, IOException{
		// Read file if file is null or empty
		if (trip == null){
			readFile("triplog.csv");
		}

		// Initialize variables
		int numStops = 0;
		final double DIS_THRESHOLD = 0.6;
		double disBetween;

		// Initailize movingTrip
		movingTrip = new ArrayList<>();

		// Get information from trip ArrayList, starting with i = 1 since we check distance from the previous point
		for (int i = 1; i < trip.size(); i++){
			disBetween = haversineDistance(trip.get(i - 1), trip.get(i));
			
			// Checks for first point
			if (i == 1 && disBetween > DIS_THRESHOLD){
				movingTrip.add(trip.get(0));
			}

			// Checks remaining points
			if (i > 1 && disBetween <= DIS_THRESHOLD){
				numStops++;
			}
			else{
				movingTrip.add(trip.get(i));
			}
		}

		return numStops;
	}

	public static int h2StopDetection() throws FileNotFoundException, IOException{
		// Read file if file is null or empty
		if (trip == null){
			readFile("triplog.csv");
		}

		movingTrip = new ArrayList<TripPoint>();
        double d = 0.5;
        int count = 0;
        
        ArrayList<TripPoint> zone = new ArrayList<TripPoint>();
        // A nested loop to compute the check the stop zones
        for(TripPoint current: trip) {
            if(zone.size()==0) {
                zone.add(current);
                continue;
            }
            boolean inStopZone = false;
            for(TripPoint stop : zone) {
                if(haversineDistance(stop, current) < d) {
                    inStopZone = true;
                }
            }
            if(inStopZone) {
                zone.add(current);
            } else {
                if(zone.size()>=3) {
                    count += zone.size();
                }else {
                    for(TripPoint sp : zone) {
                        movingTrip.add(sp); 
                    }
                }
                zone.clear();
                zone.add(current);
            }
        }
        // calculate the stop zones one last time
        if(zone.size()>=3) {
            count += zone.size();
        }else {
            for(TripPoint stop : zone) {
                movingTrip.add(stop);
            }
        }
        return count;
	}

}
