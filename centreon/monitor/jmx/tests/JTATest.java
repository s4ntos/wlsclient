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
 * Gets the current transaction active count.
 * 
 * @author Yann Lambret
 * @author Kiril Dunn
 * 
 */
public class JTATest extends TestUtils implements Test {

    /**
     * WebLogic JTA stats.
     * 
     * @param  proxy  an applicative proxy for the target WLS instance
     * @param  params warning and critical thresholds
     * @return result collected data and test status
     */
    public Result run(WLSProxy proxy, String params) {
        Result result = new Result();
        int code = 0;

        // Test thresholds
        long warning;
        long critical;

        // Performance data
        int activeTransactionsTotalCount;

        // Parses HTTP query params
        String[] thresholds = params.split(",");
        warning = Long.parseLong(thresholds[1]);
        critical = Long.parseLong(thresholds[2]);

        try {
            ObjectName jtaRuntimeMbean = proxy.getMBean("JTARuntime");
            activeTransactionsTotalCount = (Integer)proxy.getAttribute(jtaRuntimeMbean, "ActiveTransactionsTotalCount");
            result.setOutput("ActiveTransactions=" + activeTransactionsTotalCount);
            code = checkResult(activeTransactionsTotalCount, critical, warning);
            if (code == Status.WARNING.getCode() || code == Status.CRITICAL.getCode()) {
                result.setMessage("transaction active count (" + activeTransactionsTotalCount + ")");
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
