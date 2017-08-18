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

import javax.management.AttributeNotFoundException;
import javax.management.ObjectName;

import centreon.monitor.jmx.core.Result;
import centreon.monitor.jmx.core.Status;
import centreon.monitor.jmx.core.WLSProxy;

/**
 * Gets statistics for the the target
 * WLS instance JVM.
 * 
 * The following metrics are available:
 * 
 *   - The JVM current heap size (MB)
 *   - The JVM maximum heap size (MB)
 *   - The current amount of memory used by the JVM (MB)
 *   - The amount of CPU resources used by the JVM (%), JRockit only
 * 
 * @author Yann Lambret
 * @author Kiril Dunn
 * 
 */
public class JVMTest extends TestUtils implements Test {

    /**
     * WebLogic JVM stats.
     * 
     * @param proxy   an applicative proxy for the target WLS instance
     * @param params  params warning and critical thresholds
     * @return result collected data and test status
     */
    public Result run(WLSProxy proxy, String params) {
        Result result = new Result();
        int code = 0;

        // Test thresholds
        long warning;
        long critical;

        // Performance data
        long heapSizeMax;
        long heapSizeCurrent;
        long heapFreeCurrent;
        long heapUsedCurrent;
        double jvmProcessorLoad;

        // Parses HTTP query params
        String[] paramsArray = params.split(",");
        warning = Long.parseLong(paramsArray[1]);
        critical = Long.parseLong(paramsArray[2]);

        try {
            ObjectName jvmRuntimeMbean = proxy.getMBean("JVMRuntime");
            heapSizeMax = format((Long)proxy.getAttribute(jvmRuntimeMbean, "HeapSizeMax"));
            heapSizeCurrent = format((Long)proxy.getAttribute(jvmRuntimeMbean, "HeapSizeCurrent"));
            heapFreeCurrent = format((Long)proxy.getAttribute(jvmRuntimeMbean, "HeapFreeCurrent"));
            heapUsedCurrent = heapSizeCurrent - heapFreeCurrent;
            StringBuilder out = new StringBuilder();
            out.append("HeapSize=" + heapSizeCurrent + "MB;;;0;" + heapSizeMax + " ");
            out.append("UsedMemory=" + heapUsedCurrent + "MB;;;0;" + heapSizeMax);
            try {
                jvmProcessorLoad = (Double)proxy.getAttribute(jvmRuntimeMbean, "JvmProcessorLoad");
                out.append(" JvmProcessorLoad=" + Math.round(jvmProcessorLoad * 100) + "%;;;0;100");
            } catch (AttributeNotFoundException ignored) {
                // Not dealing with a JRockitRuntimeMBean
            }
            result.setOutput(out.toString());
            code = checkResult(heapUsedCurrent, heapSizeMax, critical, warning);
            if (code == Status.WARNING.getCode() || code == Status.CRITICAL.getCode())
                result.setMessage("memory used (" + heapUsedCurrent + "/" + heapSizeMax + ")");
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
