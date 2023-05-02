package org.example;

import org.matsim.api.core.v01.network.Network;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt2matsim.mapping.PTMapper;
import org.matsim.pt2matsim.run.*;
import org.matsim.pt2matsim.config.PublicTransitMappingConfigGroup;
import org.matsim.pt2matsim.run.gis.Network2Geojson;
import org.matsim.pt2matsim.run.gis.Schedule2Geojson;
import org.matsim.pt2matsim.tools.NetworkTools;
import org.matsim.pt2matsim.tools.ScheduleTools;
import org.matsim.pt2matsim.run.PublicTransitMapper;

 import org.matsim.pt.transitSchedule.api.TransitSchedule;
 import  org.matsim.api.core.v01.network.Network;

public class CreateMappedTransitSchedule {

    public static void main(String[] args){

        TransitSchedule schedule=ScheduleTools.readTransitSchedule("C:/Users/saadi/Desktop/pt/data/transit/UnmappedTransitSchedule.xml");
        Network network = NetworkTools.readNetwork("C:/Users/saadi/Desktop/pt/data/osm/belgium-latest.xml.gz");

        // setup public transit mapper
        PublicTransitMappingConfigGroup mapperConfig = PublicTransitMappingConfigGroup.createDefaultConfig();
        mapperConfig.setInputNetworkFile("C:/Users/saadi/Desktop/pt/data/osm/belgium-latest.xml.gz");
        mapperConfig.setInputScheduleFile("C:/Users/saadi/Desktop/pt/data/transit/UnmappedTransitSchedule.xml");
        //mapperConfig.setOutputNetworkFile("C:/Users/saadi/Desktop/pt/data/osm/belgium-latest-mapped.xml");
        //mapperConfig.setOutputScheduleFile("C:/Users/saadi/Desktop/pt/data/transit/MappedTransitSchedule.xml");
        mapperConfig.setNumOfThreads(12);

        PTMapper.mapScheduleToNetwork(schedule, network, mapperConfig);

        // write mapping results
        NetworkTools.writeNetwork(network, "C:/Users/saadi/Desktop/pt/data/osm/belgium-latest-mapped.xml.gz");
        ScheduleTools.writeTransitSchedule(schedule, "C:/Users/saadi/Desktop/pt/data/transit/MappedTransitSchedule.xml.gz");

        // Write geojson result
        // Network2Geojson.run(osmConfig.getOutputCoordinateSystem(), network, "output/network.geojson");
        // Schedule2Geojson.run(osmConfig.getOutputCoordinateSystem(), schedule, "output/schedule.geojson");

        // check schedule
        // CheckMappedSchedulePlausibility.run("output/schedule.xml.gz", "output/network.xml.gz", osmConfig.getOutputCoordinateSystem(), "output/check/");
    }
}
