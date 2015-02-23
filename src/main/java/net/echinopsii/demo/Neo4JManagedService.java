package net.echinopsii.demo;

import org.neo4j.kernel.logging.BufferingConsoleLogger;
import org.neo4j.kernel.logging.DefaultLogging;
import org.neo4j.kernel.logging.Logging;
import org.neo4j.server.Bootstrapper;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.NeoServer;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.PropertyFileConfigurator;
import org.neo4j.server.configuration.validation.DatabaseLocationMustBeSpecifiedRule;
import org.neo4j.server.configuration.validation.Validator;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;

public class Neo4JManagedService implements ManagedService {

    private final static Logger log = LoggerFactory.getLogger(Neo4JManagedService.class);
    private static final String NEO4J_CONFIG_FILE_PATH_PROPS = "neo4j.configfile";

    private Bootstrapper bootstrapper = Bootstrapper.loadMostDerivedBootstrapper();
    private Configurator configurator ;
    private NeoServer    server ;
    private Thread       shutdownHook ;

    public void stop() {
        if ( server != null )
            server.stop();
    }

    @Override
    public void updated(Dictionary dictionary) throws ConfigurationException {
        log.debug("updated : {}", new Object[]{(dictionary==null)?"null conf":dictionary.toString()});
        if (dictionary!=null) {
            String configFilePath = (String) dictionary.get(NEO4J_CONFIG_FILE_PATH_PROPS);
            log.debug("Neo4J server config file path: {}", new Object[]{configFilePath});
            File configFile = new File(configFilePath);
            log.debug("Create configuration from {}", configFilePath);
            BufferingConsoleLogger console = new BufferingConsoleLogger();
            configurator = new PropertyFileConfigurator(new Validator(new DatabaseLocationMustBeSpecifiedRule()), configFile, console);
            Logging logging = DefaultLogging.createDefaultLogging(configurator.getDatabaseTuningProperties());
            log.debug("Create neo4j server");
            server = new CommunityNeoServer(configurator, logging);
            log.debug("Start neo4j server");
            server.start();

            shutdownHook = new Thread() {
                @Override
                public void run() {
                    log.info("Neo4j Server shutdown initiated by request");
                    if (server != null)
                        server.stop();
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }
}
