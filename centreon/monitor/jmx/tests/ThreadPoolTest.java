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

import java.text.DecimalFormat;

import javax.management.ObjectName;

import weblogic.management.runtime.ExecuteThread;

import centreon.monitor.jmx.core.Result;
import centreon.monitor.jmx.core.Status;
import centreon.monitor.jmx.core.WLSProxy;

/**
 * Gets statistics for the WebLogic thread pool.
 * 
 * The following metrics are available:
 * 
 *   - The thread pool current size
 *   - The active thread count
 *   - The hogging thread count
 *   - The stuck thread count
 *   - The thread pool throughput
 * 
 * @author Yann Lambret
 * @author Kiril Dunn
 * 
 */
public class ThreadPoolTest extends TestUtils implements Test {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    /**
     * WebLogic thread pool stats.
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
        ExecuteThread threadsArray[];
        int idleCount = 0;
        int hoggingCount = 0;
        int stuckCount = 0;
        int totalCount;
        int activeCount;
        double throughput;

        // Parses HTTP query params
        String[] paramsArray = params.split(",");
        warning = Long.parseLong(paramsArray[1]);
        critical = Long.parseLong(paramsArray[2]);

        try {
            ObjectName threadPoolRuntimeMbean = proxy.getMBean("ThreadPoolRuntime");
            throughput = (Double)proxy.getAttribute(threadPoolRuntimeMbean, "Throughput");
            threadsArray = (ExecuteThread[])proxy.getAttribute(threadPoolRuntimeMbean, "ExecuteThreads");
            for (ExecuteThread thread : threadsArray) { 
                if ((Boolean)thread.isIdle()) { idleCount += 1; }
                if ((Boolean)thread.isHogger()) { hoggingCount += 1; }
                if ((Boolean)thread.isStuck()) { stuckCount += 1; }
            }
            totalCount = threadsArray.length;
            activeCount = totalCount - idleCount;
            StringBuilder out = new StringBuilder();
            out.append("ThreadPoolSize=" + totalCount + " ");
            out.append("ThreadActiveCount=" + activeCount + ";;;0;" + totalCount + " ");
            out.append("ThreadHoggingCount=" + hoggingCount + ";;;0;" + totalCount + " ");
            out.append("ThreadStuckCount=" + stuckCount + ";;;0;" + totalCount + " ");
            out.append("Throughput=" + DF.format(throughput));
            result.setOutput(out.toString());
            code = checkResult(stuckCount, critical, warning);
            if (code == Status.WARNING.getCode() || code == Status.CRITICAL.getCode()) {
                result.setMessage("thread pool stuck count (" + stuckCount + "/" + totalCount + ")");
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
