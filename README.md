# wlsclient
Monitor JMX weblogic

Based on the wlsagent code, this code was made to allow this to run as a script without requiring the deployment of a server
This requires all the libraries for Weblogic that are not included here because of Copyright issues

Compile by using the normal java options

```
jar cfve WLSClient.jar centreon/monitor/jmx/core/WLSClient centreon/monitor/jmx/tests/*.class centreon/monitor/jmx/core/*.class
```

And then run using the a CLASPATH with all the required JARs.

Script created for ease of setting the environment:

```bash
#!/bin/bash

cd $(dirname "$0")

CLASSPATH=/usr/lib/centreon/plugins/wlsclient/lib/wljmxclient.jar:/usr/lib/centreon/plugins/wlsclient/lib/wlclient.jar:/usr/lib/centreon/plugins/wlsclient/lib/weblogic.jar:/usr/lib/centreon/plugins/wlsclient/lib/wlthint3client.jar:/usr/lib/centreon/plugins/wlsclient/lib/commons-cli-1.4.jar:/usr/lib/centreon/plugins/wlsclient/lib/WLSClient.jar

java -cp ${CLASSPATH}  centreon/monitor/jmx/core/WLSClient $@
```

The following jars need to be retrieved from the weblogic install
- weblogic.jar
- wlthint3client.jat

the current options are available.

> usage: WLSClient
>  -c,--critical <arg>   critical threshold
>  -d,--debug            Debug
>  -f,--filter <arg>     filter of weblogic parameters
>  -h,--help             Print the help
>  -H,--host <arg>       Host to connect to
>  -m,--monitor <arg>    monitor type options are: thread-pool (default),
>                        jta, jvm, jdbc, jms-runtime, component, jms-queue
>  -p,--port <arg>       Port to connect to
>  -P,--password <arg>   weblogic password
>     --protocol <arg>   protocol to use: t3 (default), t3s
>  -t,--protocol <arg>   Protocol
>  -U,--username <arg>   weblogic username
>  -w,--warning <arg>    warning threshold
