import java.util.concurrent.TimeUnit;

service {
	name "tomcat"
	icon "tomcat.gif"
	type "WEB_SERVER"
	numInstances 1
	
	compute {
		template "SMALL_LINUX"
	}
		
	lifecycle {
		install "tomcat_install.groovy"
		start "tomcat_start.groovy" 	
		postStart "loadbalancer_ssl_tomcat_poststart.groovy"	
		preStop   "loadbalancer_ssl_tomcat_prestop.groovy"
		startDetectionTimeoutSecs 240
		startDetection {
			ServiceUtils.isPortOccupied(8080)
		}
	}
	
	customCommands ([       	
		"updateWar" : {warUrl -> 
			println "tomcat-service.groovy(updateWar custom command): warUrl is ${warUrl}..."
			context.attributes.thisInstance["warUrl"] = "${warUrl}"
			println "tomcat-service.groovy(updateWar customCommand): invoking updateWarFile custom command ..."
			context.waitForService("tomcat", 60, TimeUnit.SECONDS).invoke("updateWarFile")
			println "tomcat-service.groovy(updateWar customCommand): End"
			return true
		} ,
		 
		"updateWarFile" : "updateWarFile.groovy"
    ])
	
	plugins([		
		plugin {
			name "jmx"
			className "org.cloudifysource.usm.jmx.JmxMonitor"			
			config([
				"Current Http Threads Busy": [
					"Catalina:type=ThreadPool,name=\"http-bio-8080\"",
					"currentThreadsBusy"
				],
				"Current Http Threads Count": [
					"Catalina:type=ThreadPool,name=\"http-bio-8080\"",
					"currentThreadCount"
				],
				"Backlog": [
					"Catalina:type=ProtocolHandler,port=8080",
					"backlog"
				],
				port: 11099
			])
		}
	])

	userInterface {

		metricGroups = ([
			metricGroup {

				name "process"

				metrics([
					"Process Cpu Usage",
					"Total Process Virtual Memory",
					"Num Of Active Threads"
				])
			} ,
			metricGroup {

				name "http"

				metrics([
					"Current Http Threads Busy",
					"Current Http Threads Count",
					"Backlog"
				])
			} ,

		]
		)

		widgetGroups = ([
			widgetGroup {
				name "Process Cpu Usage"
				widgets ([
					balanceGauge{metric = "Process Cpu Usage"},
					barLineChart{
						metric "Process Cpu Usage"
						axisYUnit Unit.PERCENTAGE
					}
				])
			},
			widgetGroup {
				name "Total Process Virtual Memory"
				widgets([
					balanceGauge{metric = "Total Process Virtual Memory"},
					barLineChart {
						metric "Total Process Virtual Memory"
						axisYUnit Unit.MEMORY
					}
				])
			},
			widgetGroup {
				name "Num Of Active Threads"
				widgets ([
					balanceGauge{metric = "Num Of Active Threads"},
					barLineChart{
						metric "Num Of Active Threads"
						axisYUnit Unit.REGULAR
					}
				])
			}     ,
			widgetGroup {

				name "Current Http Threads Busy"
				widgets([
					balanceGauge{metric = "Current Http Threads Busy"},
					barLineChart {
						metric "Current Http Threads Busy"
						axisYUnit Unit.REGULAR
					}
				])
			} ,
			widgetGroup {

				name "Current Http Threads Count"
				widgets([
					balanceGauge{metric = "Current Http Thread Count"},
					barLineChart {
						metric "Current Http Thread Count"
						axisYUnit Unit.REGULAR
					}
				])
			} ,
			widgetGroup {

				name "Request Backlog"
				widgets([
					balanceGauge{metric = "Backlog"},
					barLineChart {
						metric "Backlog"
						axisYUnit Unit.REGULAR
					}
				])
			}  ,
			widgetGroup {
				name "Active Sessions"
				widgets([
					balanceGauge{metric = "Active Sessions"},
					barLineChart {
						metric "Active Sessions"
						axisYUnit Unit.REGULAR
					}
				])
			}
		]
		)
	}
    
    network {
        port = 8080
        protocolDescription ="HTTP"
    }
    	
}
