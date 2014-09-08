package org.selliott.atm.server;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.selliott.atm.common.Config;

/**
 * Main entry point for the Jetty server.
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    /**
     * Main entry point.
     * 
     * @param args
     *            Command line arguments including the path to the configuration
     *            file.
     * @throws Exception
     *             Unhandled exception that needs to be passed to the Java and
     *             displayed to the user in a ugly manner.
     */
    public static void main(String[] args) throws Exception {
        log.info("Begin main.");

        // TODO: getInstance is called here not to get the instance, but so that
        // the configuration parameters can be accessed directly. Perhaps there
        // is a more encapsulated way of doing this.
        Config.getInstance(args);

        // TODO: Parse command line arguments.

        // TODO: The port should be configurable.
        Server server = new Server(8080);
        server.setHandler(new HTTPHandler());

        server.start();
        server.join();

        log.info("End main.");
    }
}
