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

import javax.management.ObjectName;

import centreon.monitor.jmx.core.Result;
import centreon.monitor.jmx.core.Status;
import centreon.monitor.jmx.core.WLSProxy;

/**
 * @author Yann Lambret
 * @author Kiril Dunn
 * 
 */
public class JMSRuntimeTest extends TestUtils implements Test {

    /**
     * WebLogic JMS connections stats.
     * 
     * @param proxy   an applicative proxy for the target WLS instance
     * @param params  warning and critical thresholds
     * @return result collected data and test status
     */
    public Result run(WLSProxy proxy, String params) {
        Result result = new Result();
        int code = 0;

        // Test thresholds
        long warning;
        long critical;

        // Performance data
        long connectionsCurrentCount;

        // Parses HTTP query params
        String[] paramsArray = params.split(",");
        warning = Long.parseLong(paramsArray[1]);
        critical = Long.parseLong(paramsArray[2]);

        try {
            ObjectName jmsRuntimeMbean = proxy.getMBean("JMSRuntime");
            connectionsCurrentCount = (Long)proxy.getAttribute(jmsRuntimeMbean, "ConnectionsCurrentCount");
            result.setOutput("JmsRuntime-current=" + connectionsCurrentCount);
            code = checkResult(connectionsCurrentCount, critical, warning);
            if (code == Status.WARNING.getCode() || code == Status.CRITICAL.getCode()) {
                result.setMessage("JMS runtime connection count (" + connectionsCurrentCount + ")");
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

        return result;
    }

}
