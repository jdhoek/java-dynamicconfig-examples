package org.example.dynamicconfig.zookeeper;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.lable.oss.dynamicconfig.core.ConfigurationException;
import org.lable.oss.dynamicconfig.core.ConfigurationManager;
import org.lable.oss.dynamicconfig.core.InitializedConfiguration;
import org.lable.oss.dynamicconfig.serialization.yaml.YamlDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Simple static example of using Dynamic Config's ZooKeeper module.
 */
public class UsingZooKeeper {
    private static final Logger logger = LoggerFactory.getLogger(UsingZooKeeper.class);

    public static void main(String[] args) {
        try {
            test(args);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void test(String[] args) throws IOException, ConfigurationException {
        logger.info("Loading Dynamic Config using system properties.");

        try (InitializedConfiguration ic = ConfigurationManager.configureFromProperties(
                new YamlDeserializer()
        )) {
            Configuration configuration = ic.getConfiguration();

            System.out.println("-----------------------------------------------------");
            System.out.println("            SETTINGS READ FROM ZOOKEEPER             ");
            System.out.println("-----------------------------------------------------");

            // If configured to do so, Dynamic Config will store the addresses of the ZooKeeper quorum it
            // connects to in a predefined parameter: in case you want to connect to the ZooKeeper quorum
            // for another reason.
            String zk = Arrays
                    .stream(configuration.getStringArray("zookeeper-quorum"))
                    .collect(Collectors.joining(","));
            System.out.println("ZooKeeper quorum: " + zk);

            for (String arg : args) {
                try {
                    System.out.println(arg + ": " + configuration.getString(arg, "NONE"));
                } catch (ConversionException e) {
                    System.out.println(e.getMessage());
                }
            }

            System.out.println("-----------------------------------------------------");
        }
    }
}
