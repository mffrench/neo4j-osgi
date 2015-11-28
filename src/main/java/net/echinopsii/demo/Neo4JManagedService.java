package net.echinopsii.demo;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;

public class Neo4JManagedService implements ManagedService {

    private final static Logger log = LoggerFactory.getLogger(Neo4JManagedService.class);
    private static final String NEO4J_DB_DIR_PATH_PROPS = "neo4j.dbdir";
    private static final String NEO4J_CONFIG_FILE_PATH_PROPS = "neo4j.configfile";

    private GraphDatabaseService graphDb;
    private WrappingNeoServerBootstrapper webServer;
    private Thread       shutdownHook ;

    public void stop() {
        if (graphDb != null)
            graphDb.shutdown();
    }

    public void updated(Dictionary dictionary) throws ConfigurationException {
        log.debug("updated : {}", new Object[]{(dictionary == null) ? "null conf" : dictionary.toString()});
        if (dictionary!=null) {
            String dirPath = (String) dictionary.get(NEO4J_DB_DIR_PATH_PROPS);
            String configFilePath = (String) dictionary.get(NEO4J_CONFIG_FILE_PATH_PROPS);
            log.debug("Neo4J server config file path: {}", new Object[]{configFilePath});
            File dir = new File(dirPath);
            log.debug("Create neo4j server");
            graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder( dir )
                    .loadPropertiesFromFile( configFilePath )
                    .newGraphDatabase();
            webServer = new WrappingNeoServerBootstrapper((GraphDatabaseAPI)graphDb);
            webServer.start();
            log.debug("Neo4j server started");

            shutdownHook = new Thread() {
                @Override
                public void run() {
                    log.info("Neo4j Server shutdown initiated by request");
                    if (webServer != null)
                        webServer.stop();
                    if (graphDb != null)
                        graphDb.shutdown();
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }
}
