package net.echinopsii.demo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.Pair;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.GraphDatabaseDependencies;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.logging.FormattedLogProvider;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.server.CommunityBootstrapper;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.ServerConfigFactory;
import org.neo4j.server.configuration.ServerSettings;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.rmi.RMISecurityManager;
import java.util.ArrayList;
import java.util.Dictionary;

public class Neo4JManagedService implements ManagedService {

    private final static Logger log = LoggerFactory.getLogger(Neo4JManagedService.class);
    private static final String NEO4J_HOME = "neo4j.home";
    private static final String NEO4J_CONFIG_FILE_PATH_PROPS = "neo4j.configfile";

    private GraphDatabaseService graphDb;
    private WrappingNeoServerBootstrapper webServer;
    private CommunityBootstrapper communityBootstrapper;

    private Log neolog;
    private Config config;
    protected GraphDatabaseDependencies dependencies = GraphDatabaseDependencies.newDependencies();

    private Thread shutdownHook ;

    public void stop() {
        if (communityBootstrapper != null)
            communityBootstrapper.stop();
    }

    public void updated(Dictionary dictionary) throws ConfigurationException {
        log.debug("updated : {}", new Object[]{(dictionary == null) ? "null conf" : dictionary.toString()});
        if (dictionary!=null) {
            String neo4jHome = (String) dictionary.get(NEO4J_HOME);
            String configFilePath = dictionary.get(NEO4J_HOME) + File.separator + dictionary.get(NEO4J_CONFIG_FILE_PATH_PROPS);
            log.debug("Neo4J server config file path: {}", new Object[]{configFilePath});

            log.debug("Create neo4j server");
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new RMISecurityManager());
            }
            communityBootstrapper = new CommunityBootstrapper();
            communityBootstrapper.start(new File(configFilePath), (Pair<String, String>[]) new ArrayList().toArray(new Pair[0]));

            log.debug("Neo4j server started");

            shutdownHook = new Thread() {
                @Override
                public void run() {
                    log.info("Neo4j Server shutdown initiated by request");
                    if (communityBootstrapper != null)
                        communityBootstrapper.stop();
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }
}
