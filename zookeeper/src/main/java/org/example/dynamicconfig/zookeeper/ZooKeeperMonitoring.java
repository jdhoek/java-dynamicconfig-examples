package org.example.dynamicconfig.zookeeper;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.lable.oss.dynamicconfig.Precomputed;
import org.lable.oss.dynamicconfig.core.ConfigurationException;
import org.lable.oss.dynamicconfig.core.ConfigurationManager;
import org.lable.oss.dynamicconfig.core.InitializedConfiguration;
import org.lable.oss.dynamicconfig.serialization.yaml.YamlDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrate how Dynamic Config uses ZooKeeper watches to keep its configuration up-to-date.
 */
public class ZooKeeperMonitoring {
    private static final Logger logger = LoggerFactory.getLogger(UsingZooKeeper.class);

    public static void main(String[] args) {
        new Thread(() -> {
            // Prepare Dynamic Config.
            try (InitializedConfiguration ic = ConfigurationManager.configureFromProperties(
                    new YamlDeserializer()
            )) {
                long iterations = 0;
                Configuration configuration = ic.getConfiguration();
                Map<String, Precomputed<String>> precomputedMap = new HashMap<>();

                for (String arg : args) {
                    // Set up a Precomputed object that will recompute a value (here a String, but it can be anything)
                    // whenever it is first called after an update in the configuration that it monitors.
                    // The `monitorByKeys` constructor lets you create an object that monitors a specific subtree (or
                    // often, just one parameter) in the configuration tree, so changes to unrelated parameters will be
                    // ignored.
                    Precomputed<String> precomputed = Precomputed.monitorByKeys(
                            configuration,
                            // The Precomputer. This is executed on the first call to `#get()` after a relevant update.
                            conf -> {
                                logger.warn("Parameter changed: {}", arg);
                                return "(" + conf.getString(arg) + ")";
                            },
                            // List for chances to this parameter.
                            arg
                    );
                    precomputedMap.put(arg, precomputed);
                }

                // The main loop. Show the current state of the monitored configuration parameters once every 5 seconds.
                while (true) {
                    if (iterations % 50 == 0) {
                        System.out.println("--- CURRENT VALUES ---");
                        precomputedMap.forEach((s, stringPrecomputed) -> {
                            // If a configuration parameter changed since the last invocation of `#get()`, the
                            // Precomputed object will recompute its value. Otherwise, the cached value is returned.
                            System.out.println(s + ": " + stringPrecomputed.get());
                        });
                        System.out.println("----------------------");
                    }

                    iterations++;
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

            } catch (ConfigurationException | IOException e) {
                e.printStackTrace();
            }
        }).run();
    }
}
