package centreon.monitor.jmx.core;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnectorFactory;
import javax.management.MalformedObjectNameException;
import javax.naming.Context;
import java.util.Hashtable;
import java.util.Set;
public class JMXClient {
public static void main(String[] args) throws Exception {
        JMXConnector jmxCon = null;
	ObjectName SERVICE;
	ObjectName serverRuntimeMBean;

        try {
            SERVICE = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
        } catch (MalformedObjectNameException e) {
            throw new AssertionError(e);
        }
	
        try {
            JMXServiceURL serviceUrl = 
                new JMXServiceURL(
                          "service:jmx:" + args[0] + "://" + args[1] + ":" + args[2] + "/jndi/weblogic.management.mbeanservers.runtime");
                          //"service:jmx:" + args[0] + "://" + args[1] + ":" + args[2] + "/jndi/weblogic.management.mbeanservers.domainruntime");
                          //"service:jmx:" + args[0] + "://" + args[1] + ":" + args[2] + "/jndi/weblogic.management.mbeanservers.edit");
            System.out.println("Connecting to: " + serviceUrl);
            Hashtable env = new Hashtable();
            env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, 
                         "weblogic.management.remote");
            env.put(javax.naming.Context.SECURITY_PRINCIPAL, "weblogic");
            env.put(javax.naming.Context.SECURITY_CREDENTIALS, "Manager1");
            jmxCon = JMXConnectorFactory.newJMXConnector(serviceUrl, env);
            jmxCon.connect();
            MBeanServerConnection con = jmxCon.getMBeanServerConnection();
            SERVICE = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
            serverRuntimeMBean = (ObjectName)con.getAttribute(SERVICE, "ServerRuntime");
            Set<ObjectName> mbeans = con.queryNames(null, null);
            for (ObjectName mbeanName : mbeans) {
                System.out.println(mbeanName);
            }
	    System.out.println(serverRuntimeMBean);
        }
        finally {
            if (jmxCon != null)
                jmxCon.close();
        }
    }
}
