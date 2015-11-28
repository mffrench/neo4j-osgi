package net.echinopsii.demo;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class Neo4JActivator implements BundleActivator {

    private final static Logger log = LoggerFactory.getLogger(Neo4JManagedService.class);
    private ServiceRegistration neo4jServiceRegistration;
    private Neo4JManagedService neo4jManagedService = new Neo4JManagedService();

    public void start(BundleContext bundleContext) throws Exception {
        Dictionary props = new Hashtable();
        log.debug("Starting net.echinopsii.demo.Neo4JManagedService");
        props.put("service.pid", "net.echinopsii.demo.Neo4JManagedService");
        neo4jServiceRegistration = bundleContext.registerService(ManagedService.class.getName(), neo4jManagedService, props);
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (neo4jServiceRegistration !=null) {
            neo4jManagedService.stop();
            neo4jManagedService=null;
            neo4jServiceRegistration.unregister();
            neo4jServiceRegistration=null;
        }
    }
}
