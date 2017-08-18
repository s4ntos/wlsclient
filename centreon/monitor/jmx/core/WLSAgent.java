/**
 * This file is part of Wlsagent.
 *
 * Wlsagent is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wlsagent is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wlsagent. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package centreon.monitor.jmx.core;

import java.net.InetSocketAddress;


import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author Yann Lambret
 * 
 */
public class WLSAgent {

    private WLSAgent() {}

    public static void main(String[] args) throws Exception {
        // Jetty server connector is created from plugin arguments
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        // Jetty server setup
        InetSocketAddress address = new InetSocketAddress(host, port);
        Server server = new Server(address);

        // Timout for incoming HTTP requests
        for (Connector connector : server.getConnectors()) {
            connector.setMaxIdleTime(25000);
        }

        // We add the 'WLSServlet' as a unique entry point
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/wlsagent");
        server.setHandler(handler);

        handler.addServlet(new ServletHolder(new WLSServlet()), "/*");

        server.start();
        server.join();
    }

}
