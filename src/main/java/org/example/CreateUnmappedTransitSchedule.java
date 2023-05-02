package org.example;

import org.matsim.pt2matsim.run.Gtfs2TransitSchedule;
import org.example.CreateMappedTransitSchedule;
import org.matsim.pt2matsim.tools.ScheduleTools;
import org.matsim.pt2matsim.tools.NetworkTools;

public class CreateUnmappedTransitSchedule {
    public static void main(String [] args){
        if(args.length != 3) {
            throw new RuntimeException("Program requires 3 arguments: \n" +
                    "(0) GTFS directory \n" +
                    "(1) Unmapped schedule output file \n" +
                    "(2) Transit vehicles output file");
        }




        Gtfs2TransitSchedule.run(args[0],
                "dayWithMostTrips",
                "EPSG:31370",
                args[1],
                args[2]);

    }
}
