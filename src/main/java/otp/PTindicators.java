package otp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.lang.Double;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

// import otp.io.TripSurveyAttributes;
import otp.io.Itinerary;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.geometry.coordinate.PointArray;
import org.opengis.referencing.FactoryException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.Locale;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class PTindicators {
    private static final String file = "/Users/ismailsaadi/IdeaProjects/matsim-jibe/TfGM/tripsWithXY.csv";
    private static Logger logger = LogManager.getLogger(PTindicators.class);
    private static List<String[]> allData;


    public static void main(String[] args) {
        Locale usLocale = new Locale("en", "US");

        DecimalFormat decimalFormat = new DecimalFormat("0.#####", new DecimalFormatSymbols(usLocale));

        double StartTime = 0.;

        double Xorigin = 0.;
        double Yorigin = 0.;

        double Xdestination = 0.;
        double Ydestination = 0.;

        String redColor = "\u001B[31m";
        String resetColor = "\u001B[0m";
        String startTime = "00:00:00";

        int cpt = 1;

        List<Itinerary> listAllItineraries = new ArrayList<>();
        List<String> listIDs = new ArrayList<>();

        // Read trip data
        try {
            FileReader filereader = new FileReader(file);

            CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
            CSVReader csvReader = new CSVReaderBuilder(filereader).withCSVParser(parser).withSkipLines(1).build();

            logger.info("Reading input file");
            allData = csvReader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // loop over the trips
        // Prepare variables to make requests from OTP
        for (String[] row : allData) {
            if ("NA".equals(row[13]) || "NA".equals(row[14]) || "NA".equals(row[15]) || "NA".equals(row[16])) {
                //System.out.print("\n");
                logger.info(redColor + "Trip with ID " + row[0] + row[1] + row[2] + " is ignored because it has coordinates with NA values" + resetColor);
            } else {
                String tripID = row[0] + row[1] + row[2];

                StartTime = Double.parseDouble(row[3]);
                startTime = formatTime((int) Math.round(StartTime));
                //System.out.println("Hey : " + startTime);

                Xorigin = Double.parseDouble(row[13]);
                Yorigin = Double.parseDouble(row[14]);

                Xdestination = Double.parseDouble(row[15]);
                Ydestination = Double.parseDouble(row[16]);

                //
                double[] newCoordinates = transformUKToWGS84(Xorigin, Yorigin);
                String origin = decimalFormat.format(newCoordinates[0]) + "," + decimalFormat.format(newCoordinates[1]);
                //String or=String.format("%.5f,%.5f", newCoordinates[0], newCoordinates[1]);

                newCoordinates = transformUKToWGS84(Xdestination, Ydestination);
                String destination = decimalFormat.format(newCoordinates[0]) + "," + decimalFormat.format(newCoordinates[1]);

                List<Map<String, Double>> listSpecificItineraries;
                listSpecificItineraries = getItineraries(origin, destination, startTime, "2023-03-15");

                // Add to all list of itineraries
                if (listSpecificItineraries.size() > 0) {
                    for (Map<String, Double> map : listSpecificItineraries) {
                        Itinerary it = new Itinerary(
                                row[0],
                                Integer.parseInt(row[1]),
                                Integer.parseInt(row[2]),
                                0,
                                map.get("duration"),
                                map.get("walkTime"),
                                map.get("transitTime"),
                                map.get("waitingTime"),
                                map.get("walkDistance"),
                                (int) Math.round(map.get("transfers"))
                        );
                        listAllItineraries.add(it);
                        listIDs.add(tripID);
                    }
                    logger.info("Processing trip no. " + tripID + " / " + listSpecificItineraries.size() + " itineraries were added");
                }
                cpt++;
                listSpecificItineraries.clear();
            }
            if (cpt > 10) { //to test the 10 first trips
                break;
            }
        }

        // write csv
        String csvFilePath = "ptIndicatorsOTP.csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {
            String[] header = {"idNumber", "personNumber", "tripNumber", "itineraryNumber", "duration", "walkTime", "transitTime", "waitingTime", "walkDistance", "transfers"};
            writer.writeNext(header);

            // Write the data to the CSV file
            for (Itinerary itinerary : listAllItineraries) {
                writer.writeNext(new String[]{
                        itinerary.idNumber,
                        String.valueOf(itinerary.personNumber),
                        String.valueOf(itinerary.tripNumber),
                        String.valueOf(itinerary.itineraryNumber),
                        String.valueOf(itinerary.duration),
                        String.valueOf(itinerary.walkTime),
                        String.valueOf(itinerary.transitTime),
                        String.valueOf(itinerary.waitingTime),
                        String.valueOf(itinerary.walkDistance),
                        String.valueOf(itinerary.transfers)
                });
            }

            System.out.println("CSV file written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        listAllItineraries.clear();
        listIDs.clear();
    }

    public static double[] transformUKToWGS84(double x, double y) {
        try {
            // Define the UK coordinate system CRS (e.g., for British National Grid)
            CoordinateReferenceSystem ukCRS = CRS.decode("EPSG:27700"); // Replace with the appropriate EPSG code for your UK coordinate system

            // Create a GeometryFactory to create JTS Point objects
            GeometryFactory geometryFactory = new GeometryFactory();
            Point pointInUK = geometryFactory.createPoint(new Coordinate(x, y));

            // Define the target EPSG 4326 (WGS 84) CRS
            CoordinateReferenceSystem epsg4326 = CRS.decode("EPSG:4326");

            // Create a MathTransform to convert from the UK coordinate system to EPSG 4326
            MathTransform transform = CRS.findMathTransform(ukCRS, epsg4326);

            // Transform the point from the UK coordinate system to EPSG 4326
            Point pointInWGS84 = (Point) JTS.transform(pointInUK, transform);

            // Retrieve the transformed coordinates
            double transformedX = pointInWGS84.getX();
            double transformedY = pointInWGS84.getY();

            // Return the transformed coordinates as an array
            return new double[]{transformedX, transformedY};
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Handle the exception as needed and return an appropriate value
        }
    }

    public static String formatTime(Integer totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static List<Map<String, Double>> getItineraries(String fromLocation, String toLocation, String startTime, String date) {
        //logger.info("Running OTP ...");
        List<Map<String, Double>> listItineraries = new ArrayList<>();

        // Make requests from OTP
        try {
            // GET http://your-otp-instance-url/otp/routers/default/plan?fromPlace=37.7749,-122.4194&toPlace=37.7833,-122.4167&date=2023-09-05&time=08:00:00&mode=TRANSIT&maxWalkDistance=1000&arriveBy=false&numItineraries=1
            //logger.info("Preparing requests from OTP");

            int cpt = 0;
            HttpClient httpClient = HttpClients.createDefault();
            String otpServerUrl = "http://localhost:8080/otp";

            //String fromLocation = "53.57824,-2.43279";
            //String toLocation = "53.41772,-2.38609";

            // Build the OTP request URL
            String otpRequestUrl =
                    "http://localhost:8080/otp" +
                            "/routers/default/plan?" +
                            "fromPlace=" + fromLocation +
                            "&toPlace=" + toLocation +
                            "&date=" + date + // 2023-09-05
                            "&time=" + startTime + // 15:36:00
                            "&mode=TRANSIT,WALK"; // todo: add mode variable

            // Create HTTP GET request and send the GET request and get the response
            HttpGet get = new HttpGet(otpRequestUrl);
            HttpResponse response = httpClient.execute(get);
            String responseContent = EntityUtils.toString(response.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseContent);

            Iterator<JsonNode> itineraries = jsonNode.get("plan").get("itineraries").elements();
            while (itineraries.hasNext()) {
                JsonNode jsn = itineraries.next();

                Map<String, Double> hashMap = Map.of(
                        "duration", jsn.get("duration").asDouble(),
                        "walkTime", jsn.get("walkTime").asDouble(),
                        "transitTime", jsn.get("transitTime").asDouble(),
                        "waitingTime", jsn.get("waitingTime").asDouble(),
                        "walkDistance", jsn.get("walkDistance").asDouble(),
                        "transfers", jsn.get("transfers").asDouble());

                listItineraries.add(hashMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listItineraries;
    }
}
