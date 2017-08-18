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

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import javax.naming.Context;

/**
 * @author Yann Lambret
 * 
 */
public class WLSProxy {

    private static final String JNDI_NAME = "/jndi/weblogic.management.mbeanservers.runtime";
    private static final ObjectName SERVICE;

    private Map<String,String> params;        // HTTP request params
    private JMXConnector connector;           // JMX connector
    private MBeanServerConnection connection; // JMX connection to our WebLogic instance
    private ObjectName serverRuntimeMBean;    // WebLogic server MBean

    static {
        try {
            SERVICE = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
        } catch (MalformedObjectNameException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Default constructor.
     * 
     * @param params HTTP query parameters
     */
    public WLSProxy(Map<String,String> params) {
        this.params = params;
    }

    /**
     * Proxy initialization. Gets a JMX connection first,
     * and then the server runtime MBean for the target WLS instance.
     * 
     * @throws Exception
     */
    public void init() throws Exception {
        Map<String, String> map = new HashMap<String,String>();

        // User credentials
        map.put(Context.SECURITY_PRINCIPAL, params.get("username"));
        map.put(Context.SECURITY_CREDENTIALS, params.get("password"));

        // We use a t3 connector with a 20 seconds timeout
        map.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
        map.put("jmx.remote.x.request.waiting.timeout", "5000");

        // We build the URL with the 'hostname' and 'port' params
        //JMXServiceURL url = new JMXServiceURL("service:jmx:" + params.get("protocol") + "://" + params.get("hostname") + ":" + params.get("port") + JNDI_NAME);
        //JMXServiceURL serviceURL = new JMXServiceURL(params.get("protocol"),  params.get("hostname"), params.get("port"), "/jndi/weblogic.management.mbeanservers.domainruntime");
        JMXServiceURL url = new JMXServiceURL(params.get("protocol"),  params.get("hostname"), Integer.parseInt(params.get("port")), JNDI_NAME);
        connector = JMXConnectorFactory.connect(url, map);
        connection = connector.getMBeanServerConnection();
        serverRuntimeMBean = (ObjectName)connection.getAttribute(SERVICE, "ServerRuntime");
    }

    /**
     * Clean up resources.
     */
    public void clean() {
        try {
            connector.close();
        } catch (IOException ignored) {        
        }
    }

    /**
     * Gets the target application server logical name.
     * 
     * @return the instance name
     * @throws Exception
     */
    public String getServerName() throws Exception {
        return (String)connection.getAttribute(serverRuntimeMBean, "Name");
    }

    /**
     * Gets the target application server state.
     * 
     * @return the server state
     * @throws Exception
     */
    public String getServerState() throws Exception {
        return (String)connection.getAttribute(serverRuntimeMBean, "State");
    }

    /**
     * Gets an array of MBeans.
     * 
     * @param  mbean
     * @param  query
     * @return the target MBeans
     * @throws Exception
     */
    public ObjectName[] getMBeans(ObjectName mbean, String query) throws Exception {
        return (ObjectName[])connection.getAttribute(mbean, query);
    }

    /**
     * Gets an array of child MBeans from serverRuntimeMBean.
     * 
     * @param  query
     * @return the target MBeans
     * @throws Exception
     */
    public ObjectName[] getMBeans(String query) throws Exception {
        return (ObjectName[])connection.getAttribute(serverRuntimeMBean, query);
    }

    /**
     * Gets a single MBean.
     * 
     * @param  query
     * @param  mbean
     * @return the target MBean
     * @throws Exception
     */
    public ObjectName getMBean(ObjectName mbean, String query) throws Exception {
        return (ObjectName)connection.getAttribute(mbean, query);
    }

    /**
     * Gets a single MBean from serverRuntimeMBean.
     * 
     * @param  query
     * @return the target MBean
     * @throws Exception
     */
    public ObjectName getMBean(String query) throws Exception {
        return (ObjectName)connection.getAttribute(serverRuntimeMBean, query);
    }

    /**
     * Gets one attribute of the given MBean.
     * 
     * @param  mbean
     * @param  attribute
     * @return the target MBean attribute
     * @throws Exception
     */
    public Object getAttribute(ObjectName mbean, String attribute) throws Exception {
        return connection.getAttribute(mbean, attribute);
    }

}
