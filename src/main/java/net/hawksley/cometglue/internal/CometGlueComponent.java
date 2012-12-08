package net.hawksley.cometglue.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.Servlet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.CometdServlet;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

@Component
public class CometGlueComponent
{

    private static final String COMETD_ALIAS = "/cometd";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HttpService httpService;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL_UNARY)
    protected LogService log;

    private BayeuxServerImpl bayeux;
    private ServiceRegistration serviceReg;

    protected void activate(ComponentContext ctxt) throws Exception
    {
        Servlet servlet = new CometdServlet();
        Dictionary<String, String> opts = new Hashtable<String, String>();

        putEnvironmentalOpts(opts);

        if (log != null)
            log.log(LogService.LOG_DEBUG, "Using Bayeux options: " + opts);

        // Register the cometd service with the OSGi web service.
        // In jetty.xml, the following conenctor must be used:
        // <New class="org.eclipse.jetty.server.nio.SelectChannelConnector">
        httpService.registerServlet(COMETD_ALIAS, servlet, opts, null);

        bayeux = ((CometdServlet) servlet).getBayeux();

        // Add our test channel listener
        // bayeux.addListener(new CometGlueChannelListener(log));

        serviceReg = ctxt.getBundleContext().registerService(
                BayeuxServer.class.getName(), bayeux, null);

        if (log != null)
            log.log(LogService.LOG_DEBUG, "Component active.");
    }

    protected void deactivate(ComponentContext ctx) throws Exception
    {
        httpService.unregister(COMETD_ALIAS);
        bayeux.stop();
        serviceReg.unregister();

        if( log != null )
            log.log(LogService.LOG_DEBUG, "Component deactivated.");
    }

    private void putEnvironmentalOpts(Dictionary<String, String> opts)
    {
        Properties sysprops = System.getProperties();

        String optPrefix = "bayeux.";

        for (Object raw : sysprops.keySet())
        {
            String key = (String) raw;

            if (key.startsWith(optPrefix))
            {
                opts.put(key.substring(optPrefix.length()),
                        sysprops.getProperty(key));
            }
        }
    }
}
