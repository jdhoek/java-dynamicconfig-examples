package org.example.dynamicconfig.fromfile;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.lable.oss.dynamicconfig.core.ConfigurationException;
import org.lable.oss.dynamicconfig.core.ConfigurationManager;
import org.lable.oss.dynamicconfig.core.InitializedConfiguration;
import org.lable.oss.dynamicconfig.serialization.yaml.YamlDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SystemProperties {
    private static final Logger logger = LoggerFactory.getLogger(SystemProperties.class);

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

            for (String arg : args) {
                try {
                    System.out.println(configuration.getString(arg, "NONE"));
                } catch (ConversionException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
}
