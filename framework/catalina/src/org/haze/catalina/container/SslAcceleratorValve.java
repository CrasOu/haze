package org.haze.catalina.container;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * To use add (or uncomment) the following line to the Tomcat/Catalina configuarion (ie in haze-containers.xml under the <property name="default-server" value="engine"> element)
 *    <property name="ssl-accelerator-port" value="8443"/>
 *
 * Once that is done just setup a connector just like the example http-connector and have it listen on the port you set in the ssl-accelerator-port value.
 */
public class SslAcceleratorValve extends ValveBase {

    protected Integer sslAcceleratorPort = null;

    public void setSslAcceleratorPort(Integer sslAcceleratorPort) {
        this.sslAcceleratorPort = sslAcceleratorPort;
    }

    public Integer getSslAcceleratorPort() {
        return sslAcceleratorPort;
    }

    public @Override void invoke(Request req, Response resp) throws IOException, ServletException {
        if (sslAcceleratorPort != null && req.getLocalPort() == sslAcceleratorPort.intValue()) {
            req.setSecure(true);
        }

        if (getNext() != null) {
            getNext().invoke(req, resp);
        }
    }
}
