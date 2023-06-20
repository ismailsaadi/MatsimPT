package org.example;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.pt2matsim.config.PublicTransitMappingConfigGroup;
import org.matsim.pt2matsim.run.PublicTransitMapper;
import org.matsim.pt2matsim.run.CreateDefaultPTMapperConfig;

public class CreateMappedTransitSchedule {

    public static void main(String[] args){

        //TransitSchedule schedule=ScheduleTools.readTransitSchedule("data/out/UnmappedTransitSchedule.xml");
        //Network network = NetworkTools.readNetwork("data/in/network.xml");

        createMapperConfigFile("data/config/MapperConfigAdjusted.xml");

        // Map the schedule using the config
        //PublicTransitMapper.main(new String[]{"data/config/MapperConfigAdjusted.xml"});

        // setup public transit mapper
        //PublicTransitMappingConfigGroup DefaultMapperConfig = new CreateDefaultPTMapperConfig().;
        // PublicTransitMappingConfigGroup mapperConfig = PublicTransitMappingConfigGroup.createDefaultConfig();
        //mapperConfig.setInputNetworkFile("C:/Users/saadi/Desktop/pt/data/osm/belgium-latest.xml.gz");
        //mapperConfig.setInputScheduleFile("C:/Users/saadi/Desktop/pt/data/transit/UnmappedTransitSchedule.xml");
        //mapperConfig.setOutputNetworkFile("C:/Users/saadi/Desktop/pt/data/osm/belgium-latest-mapped.xml");
        //mapperConfig.setOutputScheduleFile("C:/Users/saadi/Desktop/pt/data/transit/MappedTransitSchedule.xml");
        //mapperConfig.setNumOfThreads(12);

        // PTMapper.mapScheduleToNetwork(schedule, network, mapperConfig);

        // write mapping results
        // NetworkTools.writeNetwork(network, "data/out/manchester-mapped.xml.gz");
        // ScheduleTools.writeTransitSchedule(schedule, "data/out/MappedTransitSchedule.xml.gz");

        // Write geojson result
        // Network2Geojson.run(osmConfig.getOutputCoordinateSystem(), network, "output/network.geojson");
        // Schedule2Geojson.run(osmConfig.getOutputCoordinateSystem(), schedule, "output/schedule.geojson");

        // check schedule
        // CheckMappedSchedulePlausibility.run("output/schedule.xml.gz", "output/network.xml.gz", osmConfig.getOutputCoordinateSystem(), "output/check/");
    }

    /**
     * 	The core of the PT2MATSim-package is the mapping process of the schedule to the network.
     * 	The unmapped schedule of GManchester (previously converted from GTFS) is mapped
     * 	to the converted OSM network.
     */
    public static void createMapperConfigFile(String configFile) {

        // Create a mapping config:
        CreateDefaultPTMapperConfig.main(new String[]{ "data/config/MapperConfigDefault.xml"});
        // Open the mapping config and set the parameters to the required values
        // (usually done manually by opening the config with a simple editor)
        Config config = ConfigUtils.loadConfig(
                "data/config/MapperConfigDefault.xml",
                PublicTransitMappingConfigGroup.createDefaultConfig());
        config.global().setCoordinateSystem("EPSG:27700");
        PublicTransitMappingConfigGroup ptmConfig = ConfigUtils.addOrGetModule(config, PublicTransitMappingConfigGroup.class);

        ptmConfig.setInputNetworkFile("data/in/network.xml");
        ptmConfig.setInputScheduleFile( "data/out/UnmappedTransitSchedule.xml");

        ptmConfig.setOutputNetworkFile("data/out/MappedNetwork.xml.gz");
        ptmConfig.setOutputScheduleFile("data/out/MappedTransitSchedule.xml.gz");
        ptmConfig.setOutputStreetNetworkFile( "data/out/MappedStreetNetwork.xml.gz");

        PublicTransitMappingConfigGroup.TransportModeAssignment mraBus = new PublicTransitMappingConfigGroup.TransportModeAssignment("bus");

        System.out.print(mraBus.getNetworkModes());
        //mraBus.setNetworkModesStr("car,bus");
        //config.addParameterSet(mraBus);

        //ptmConfig.setScheduleFreespeedModes(CollectionUtils.stringToSet("rail, light_rail"));
        // Save the mapping config
        // (usually done manually)
        new ConfigWriter(config).write(configFile);
    }
}
