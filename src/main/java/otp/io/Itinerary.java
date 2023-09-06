package otp.io;

import com.opencsv.bean.CsvBindByPosition;

public class Itinerary {
    @CsvBindByPosition(position = 0)
    public String idNumber;

    @CsvBindByPosition(position = 1)
    public double personNumber;

    @CsvBindByPosition(position = 2)
    public double tripNumber;

    @CsvBindByPosition(position = 3)
    public double itineraryNumber;

    @CsvBindByPosition(position = 4)
    public double duration;

    @CsvBindByPosition(position = 5)
    public double walkTime;

    @CsvBindByPosition(position = 6)
    public double transitTime;

    @CsvBindByPosition(position = 7)
    public double waitingTime;

    @CsvBindByPosition(position = 8)
    public double walkDistance;

    @CsvBindByPosition(position = 9)
    public int transfers;

    public Itinerary(String idNumber,
                     int personNumber,
                     int tripNumber,
                     int itineraryNumber,
                     double duration,
                     double walkTime,
                     double transitTime,
                     double waitingTime,
                     double walkDistance,
                     int transfers) {

        this.idNumber = idNumber;
        this.personNumber = personNumber;
        this.tripNumber = tripNumber;
        this.itineraryNumber = itineraryNumber;
        this.duration = duration;
        this.walkTime = walkTime;
        this.transitTime = transitTime;
        this.waitingTime = waitingTime;
        this.walkDistance = walkDistance;
        this.transfers = transfers;
    }
}
