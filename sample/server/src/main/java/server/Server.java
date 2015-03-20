package server;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 12/23/14.
 */
public class Server {
    private final static Logger logger = LoggerFactory.getLogger(Server.class);

    private org.eclipse.jetty.server.Server server;

    private AssertionError assertion;

    private Exception exception;



    public Server run(int port, ResourceConfig resourceConfig) {
        return run(port, () -> {
        }, false, resourceConfig);
    }

    private Server run(int port, Runnable afterStart, boolean cleanupAfterStartHandler, ResourceConfig resourceConfig) {
        checkArgument(port > 0);
        checkNotNull(resourceConfig);

        org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(port);
        server.setStopTimeout(1000);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/*");

        server.addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(LifeCycle lifeCycle) {
            }

            @Override
            public void lifeCycleStarted(LifeCycle lifeCycle) {
                if (cleanupAfterStartHandler) {
                    Thread daemon = new Thread(() -> {
                        try {
                            afterStart.run();
                        } catch (Exception e) {
                            exception = e;
                        } catch (AssertionError e) {
                            assertion = e;
                        }
                        try {
                            lifeCycle.stop();
                        } catch (Exception e) {
                        }
                    });
                    daemon.setDaemon(false);
                    daemon.start();
                }
            }

            @Override
            public void lifeCycleFailure(LifeCycle lifeCycle, Throwable throwable) {
            }

            @Override
            public void lifeCycleStopping(LifeCycle lifeCycle) {
            }

            @Override
            public void lifeCycleStopped(LifeCycle lifeCycle) {
            }
        });

        try {
            logger.info("running webserver on port " + port);
            this.server = server;
            server.start();
            return this;
        } catch (Exception e) {
            this.server = null;
            logger.error("execution of webserver failed", e);
            throw new ServerException(e);
        }
    }

    public Server run(int port, Runnable afterStart, ResourceConfig resourceConfig) {
        return run(port, afterStart, true, resourceConfig);
    }


    public Server join() throws Exception {
        if (server == null) {
            throw new IllegalStateException();
        }

        try {
            server.join();
            if (server != null) {
                server.destroy();
            }
            server = null;
        } catch (Exception e) {
        }

        if (assertion != null) {
            throw assertion;
        }
        if (exception != null) {
            throw exception;
        }
        return this;
    }

    public static int getPortFromEnv() throws Exception {
        String portStr = null;
        try {
            portStr = System.getenv("PORT");
            if (portStr.trim().equals("")) {
                throw new Exception("environment variable PORT is empty");
            }
        } catch (NullPointerException e) {
            throw new Exception("environment variable PORT is not set");
        }

        try {
            return Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("environment variable PORT could not be parse into integer");
        }
    }
}
