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

package centreon.monitor.jmx.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import centreon.monitor.jmx.core.Result;
import centreon.monitor.jmx.core.Status;
import centreon.monitor.jmx.core.WLSProxy;

/**
 * Gets statistics for JDBC datasources.
 * 
 * The following metrics are available:
 * 
 *   - The datasource current pool size
 *   - The active connection count
 *   - The number of threads waiting for
 *     a connection from the pool
 * 
 * @author Yann Lambret
 * @author Kiril Dunn
 *
 */
public class JDBCTest extends TestUtils implements Test {

    /**
     * WebLogic JDBC datasources stats.
     * 
     * @param proxy   an applicative proxy for the target WLS instance
     * @param params  a pipe separated list of datasource names, or
     *                a wildcard character (*) for all datasources
     * @return result collected data and test status
     */
    public Result run(WLSProxy proxy, String params) {
        Result result = new Result();
        List<String> output = new ArrayList<String>();
        List<String> message = new ArrayList<String>();
        int code = 0;

        // Test thresholds
        long warning;
        long critical;
        String thresholds = "";
        Map<String,String> datasources = new HashMap<String,String>();

        // Test code for a specific datasource
        int testCode = 0;

        // Message prefix
        String prefix = "datasource active count: ";

        // Performance data
        String state;
        int capacity;
        int activeCount;
        int waitingCount;

        // Parses HTTP query params
        for (String s : Arrays.asList(params.split("\\|"))) {
            datasources.put(s.split(",", 2)[0], s.split(",", 2)[1]);
        }

        try {
            ObjectName jdbcServiceRuntimeMbean = proxy.getMBean("JDBCServiceRuntime");
            ObjectName[] jdbcDataSourceRuntimeMbeans = proxy.getMBeans(jdbcServiceRuntimeMbean, "JDBCDataSourceRuntimeMBeans");
            for (ObjectName datasourceRuntime : jdbcDataSourceRuntimeMbeans) {
                String datasourceName = (String)proxy.getAttribute(datasourceRuntime, "Name");
                if (datasources.containsKey("*") || datasources.containsKey(datasourceName)) {
                    state = (String)proxy.getAttribute(datasourceRuntime, "State");
                    capacity = (Integer)proxy.getAttribute(datasourceRuntime, "CurrCapacity");
                    activeCount = (Integer)proxy.getAttribute(datasourceRuntime, "ActiveConnectionsCurrentCount");
                    waitingCount = (Integer)proxy.getAttribute(datasourceRuntime, "WaitingForConnectionCurrentCount");
                    StringBuilder out = new StringBuilder();
                    out.append("jdbc-" + datasourceName + "-capacity=" + capacity + " ");
                    out.append("jdbc-" + datasourceName + "-active=" + activeCount + " ");
                    out.append("jdbc-" + datasourceName + "-waiting=" + waitingCount);
                    output.add(out.toString());
                    thresholds = datasources.get("*") != null ? datasources.get("*") : datasources.get(datasourceName);
                    warning = Long.parseLong(thresholds.split(",")[0]);
                    critical = Long.parseLong(thresholds.split(",")[1]);
                    testCode = checkResult(waitingCount, critical, warning);
                    if (testCode == Status.WARNING.getCode() || testCode == Status.CRITICAL.getCode()) {
                        message.add(datasourceName + " (" + waitingCount + ")");
                        code = (testCode > code) ? testCode : code;
                    }
                    if ( ! state.equals("Running") ) {
			message.add(datasourceName + " in CRITICAL State)");
			code = Status.CRITICAL.getCode();
		    } 
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setStatus(Status.UNKNOWN);
            result.setMessage(e.toString());
            return result;
        }

        for (Status status : Status.values()) {
            if (code == status.getCode()) {
                result.setStatus(status);           
                break;
            }
        }

        result.setOutput(formatOut(output));
        result.setMessage(formatMsg(prefix, message));

        return result;
    }

}

