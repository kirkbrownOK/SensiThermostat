/**
 *  Copyright 2016 Kirk Brown
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *	Emerson Sensi Community Created Service Manager
 *
 *	Author: Kirk Brown
 *	Date: 2016-12-24
 *
 */
 
definition(
		name: "Sensi (Connect)",
		namespace: "kirkbrownOK/SensiThermostat",
		author: "Kirk Brown",
		description: "Connect your Sensi thermostats to SmartThings.",
		category: "SmartThings Labs",
		iconUrl: "http://i.imgur.com/QVbsCpu.jpg",
		iconX2Url: "http://i.imgur.com/4BfQn6I.jpg",
		singleInstance: true
)

preferences {
	page(name: "auth", title: "Sensi", nextPage:"", content:"authPage", uninstall: true)
	page(name: "getDevicesPage", title: "Sensi Devices", nextPage:"", content:"getDevicesPage", uninstall: true, install: true)
}

def authPage() {
	//log.debug "authPage()"

	def description
	def uninstallAllowed = false
	if(state.connectionToken) {
		description = "You are connected."
		uninstallAllowed = true
	} else {
		description = "Click to enter Sensi Credentials"
	}

		return dynamicPage(name: "auth", title: "Login", nextPage: "getDevicesPage", uninstall:uninstallAllowed) {
			section() {
				paragraph "Enter your Username and Password for Sensi Connect. Your username and password will be saved in SmartThings in whatever secure/insecure manner SmartThings saves them."
				input("userName", "string", title:"Sensi Email Address", required:true, displayDuringSetup: true)
    			input("userPassword", "password", title:"Sensi account password", required:true, displayDuringSetup:true)		
    			input("testButton", "capability.button", title: "Test button")
			}
		}
        getAuthorized()
        getToken()

}
def getDevicesPage() {
	//log.debug "getDevicesPage"
         
    def stats = getSensiThermostats()
    //log.debug "thermostat list: $stats"
    return dynamicPage(name: "getDevicesPage", title: "Select Your Thermostats", uninstall: true) {
        section("") {
            paragraph "Tap below to see the list of sensi thermostats available in your sensi account and select the ones you want to connect to SmartThings."
            input(name: "thermostats", title:"", type: "enum", required:true, multiple:true, description: "Tap to choose", metadata:[values:stats])
        }
    }
}


def getThermostatDisplayName(stat) {
    if(stat?.DeviceName) {
        return stat.DeviceName.toString()
    }
    return "Unknown"
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}
def buttonHandler(evt) {
	state.connected = false
    log.debug "Button Press"
    getConnected()
    log.debug "Connected"
    def child1 = getChildDevices()[0].device.deviceNetworkId
    def child2 = getChildDevices()[1].device.deviceNetworkId
    log.debug "Child Dev1: ${child1} Dev2: ${child2}"
    pollDNI(child1)
    
    
}
def initialize() {

	log.debug "initialize"
    subscribe(testButton,"button",buttonHandler)
    getAuthorized()
    getToken()
    
	def devices = thermostats.collect { dni ->
		def d = getChildDevice(dni)
		if(!d) {
        	log.debug "addChildDevice($app.namespace, ${getChildName()}, $dni, null, [\"label\":\"${state.thermostats[dni]}\" ?: \"Sensi Thermostat\"])"
			d = addChildDevice(app.namespace, getChildName(), dni, null, ["label":"${state.thermostats[dni]}" ?: "Sensi Thermostat"])
			log.debug "created ${d.displayName} with id $dni"
		} else {
			log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}

	log.debug "created ${devices.size()} thermostats."

	def delete  // Delete any that are no longer in settings
	if(!thermostats) {
		log.debug "delete thermostats ands sensors"
		delete = getAllChildDevices() //inherits from SmartApp (data-management)
	} else { //delete only thermostat
		log.debug "delete individual thermostat"
		delete = getChildDevices().findAll { !thermostats.contains(it.deviceNetworkId) }		
	}
	log.warn "delete: ${delete}, deleting ${delete.size()} thermostats"
	delete.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)

	//send activity feeds to tell that device is connected
	def notificationMessage = "is connected to SmartThings"
	sendActivityFeeds(notificationMessage)
	state.timeSendPush = null
	state.reAttempt = 0

	try{
		pollHandler() //first time polling data data from thermostat
	} catch (e) {
    	log.warn "Error in first time polling. Could mean something is wrong."
    }
	//automatically update devices status every 5 mins
	//runEvery5Minutes("poll")

}

def getAuthorized() {
    def bodyParams = [ Password: "${userPassword}", UserName: "${userName}" ]
    state.tid = 2
    state.sendCounter=0
    state.GroupsToken = null
	def deviceListParams = [
		uri: "https://bus-serv.sensicomfort.com/api/authorize",
		//path: "/api/authorize",
		headers: ["Content-Type": "application/json", "Accept": "application/json; version=1, */*; q=0.01", "X-Requested-With":"XMLHttpRequest"],
		body: [ Password: userPassword, UserName: userName ]
	]
	try {
		httpPostJson(deviceListParams) { resp ->
        	//log.debug "Resp Headers: ${resp.headers}"
        	
			if (resp.status == 200) {
				resp.headers.each {
            		//log.debug "${it.name} : ${it.value}"
                    if (it.name == "Set-Cookie") {
                    	//log.debug "Its SETCOOKIE ${it.value}"
                        //state.myCookie = it.value
                        def tempC = it.value.split(";")
                        tempC = tempC[0].trim()
                        if(tempC == state.myCookie) {
                        	log.debug "Cookie didn't change"
                        } else {
                        	state.myCookie = tempC
                        	//log.debug "My Cookie: ${state.myCookie}"
                        }
                    }
        		}
			} else {
				log.debug "http status: ${resp.status}"
			}
            //log.debug "Cookie: ${state.myCookie}"
		}
	} catch (e) {
        log.trace "Exception trying to authenticate $e"
    }	

}

def getToken() {
	//log.debug "GetToken"
    def params = [
        uri: 'https://bus-serv.sensicomfort.com',
    	path: '/realtime/negotiate',
        requestContentType: 'application/json',
        contentType: 'application/json',
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip']
	]
    try {
        httpGet(params) { resp ->
            //log.debug "response contentType: ${resp.contentType}"
            //log.debug "response data: ${resp.data}"
//            resp.data.each{
//            	log.debug "${it}"
//            }
            state.connectionToken = resp.data.ConnectionToken
            state.connectionId = resp.data.ConnectionId
            //log.debug "stateConnectionToken: ${state.connectionToken}"
            //log.debug "stateConnectionId: ${state.connectionId}"
        }
    } catch (e) {
        log.error "Connection Token error $e"
    }

}
def getConnected() {
	getAuthorized()
    getToken()
	log.debug "GetConnected"
    
    def params = [
    	
        uri: getApiEndpoint(),
    	path: '/realtime/connect',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId,tid:state.tid,"_":now()],
        //requestContentType: 'application/json',
        contentType: 'application/json',
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip']
	]
	//log.debug "Sending Connect"
    try {
        httpGet(params) { resp ->
        	//log.debug "Conected Resp: $resp.data"
            //log.debug "resp.c ${resp.data.C}"
            if(resp.data.C) {
            	state.messageId= resp.data.C
            	log.debug "MessageID: ${state.messageId}"
            }    
            //state.connectionToken = resp.data.ConnectionToken
            //log.debug "stateConnectionToken: ${state.connectionToken}"
            state.connected = true
            state.tid = state.tid+1
        }
    } catch (e) {
        log.error "Get Connected went wrong: $e"
    }    
}
def getSensiThermostats() {
	log.debug "getting device list"
	state.sensiSensors = []
	def deviceListParams = [
		uri: apiEndpoint,
		path: "/api/thermostats",
        requestContentType: 'application/json',
        contentType: 'application/json',
		headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip']        
	]
	//log.debug "Get Stats: ${deviceListParams}"
	def stats = [:]
	try {
		httpGet(deviceListParams) { resp ->
        	
			if (resp.status == 200) {
            	//log.debug "resp.data.DeviceName: ${resp.data.DeviceName}"
				resp.data.each { stat ->
                	
					state.sensiSensors = state.sensiSensors == null ? stat.DeviceName : state.sensiSensors <<  stat.DeviceName
					def dni = stat.ICD
					stats[dni] = getThermostatDisplayName(stat)
				}
			} else {
				log.debug "http status: ${resp.status}"
			}
		}
	} catch (e) {
        log.trace "Exception getting thermostats: " + e
        state.connected = false
    }
	state.thermostats = stats
    state.thermostatResponse = stats
    log.debug "State Thermostats: ${state.thermostats}"
	return stats
}
def pollHandler() {
	log.debug "pollHandler()"
	pollChildren(null) // Hit the sensi API for update on all thermostats

}

def pollChildren(child = null) {
    def myDevices = getChildDevices()
    myDevices.each { individualDevice ->
    	log.debug "In mydevices"
        def thermostatIdsString = individualDevice.device.deviceNetworkId
        log.debug "polling children: $thermostatIdsString"
        def requestBody = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"Subscribe\",\"A\":[\"${thermostatIdsString}\"],\"I\":0}"]
        def requestBody2 = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"Subscribe\",\"A\":[\"${thermostatIdsString}\"],\"I\":1}"]
        def requestBody3 = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"Unsubscribe\",\"A\":[\"${thermostatIdsString}\"],\"I\":2}"]

        def result = false
        if(!state.connected || (state.messageId == null)) {
            getConnected()
        }    
        def params = [    	
            uri: getApiEndpoint(),
            path: '/realtime/send',
            query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
            headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
            body: requestBody
        ]

        try {

            httpPost(params) { resp ->
                log.debug "resp 1: ${resp.data}"
            }
            params.body = requestBody2
            httpPost(params) { resp ->
                log.debug "resp 2: ${resp.data}"
            }
            state.tid = state.tid+1
        } catch (e) {
            log.error "Poll Subscribe went wrong: $e"
            state.connected = false
        }
        params = [
            uri: getApiEndpoint(),
            path: '/realtime/poll',
            query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]"
                ,connectionId:state.connectionId,messageId:state.messageId,tid:state.tid,'_':now()],
            headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"]
        ]
        if(state.GroupsToken) {
            params.query = [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]"
                ,connectionId:state.connectionId,messageId:state.messageId,GroupsToken:state.GroupsToken,tid:state.tid,'_':now()]
        }

        try{
            httpGet(params) { resp ->
                if(resp.data.M[0].A[1]) {
                    //state.connected = false           	
                    log.info "Final Poll ${resp.data.M[0].A[1]}"
                    //log.info "state.thermostats[${thermostatIdsString}] = data"
                    state.thermostatResponse[thermostatIdsString] = resp.data.M[0].A[1]
                }
                if(resp.data.C) {            	
                    state.messageId = resp.data.C
                }
                if(resp.data.G) {
                    state.GroupsToken = resp.data.G
                }
                if(resp.status == 200) {
                    result = true
                }
            }
            state.tid = state.tid+1
        } catch (e) {
            log.trace "Exception polling children: " + e
            state.connected = false        
        }
        params = [    	
            uri: getApiEndpoint(),
            path: '/realtime/send',
            query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
            headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
            body: requestBody3
        ]

        try {

            httpPost(params) { resp ->
                log.debug "resp 3: ${resp.data}"
            }
        } 
        catch (e) {
        	log.trace "Exception unsubscribing " + e
        	state.connected = false
    	}
    }    
	return result
}

// Poll Child is invoked from the Child Device itself as part of the Poll Capability
def pollChild() {
	def devices = getChildDevices()
	
	if (pollChildren()) {
		devices.each { child ->
        	return null
            if(state.thermostatResponse[child.device.deviceNetworkId] != null) {
                def tData = state.thermostatResponse[child.device.deviceNetworkId]
                log.info "pollChild(child)>> data for ${child.device.deviceNetworkId} : ${tData.data}"
                child.generateEvent(tData.data) //parse received message from parent
            } else if(state.thermostatResponse[child.device.deviceNetworkId] == null) {
                log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId}"
                return null
            }
			
		}
	} else {
		log.info "ERROR: pollChildren()"
		return null
	}

}

void poll() {
	pollChild()
}

def availableModes(child) {
	debugEvent ("atomicState.thermostats = ${state.thermostatResponse}")
	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = state.thermostatResponse[child.device.deviceNetworkId]

	debugEvent("Data = ${tData}")

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"
		return null
	}

	def modes = ["off"]

    if (tData.data.heatMode) {
        modes.add("heat")
    }
    if (tData.data.coolMode) {
        modes.add("cool")
    }
    if (tData.data.autoMode) {
        modes.add("auto")
    }
    if (tData.data.auxHeatMode) {
        modes.add("auxHeatOnly")
    }

    return modes
}

def currentMode(child) {
	debugEvent ("atomicState.Thermos = ${state.thermostats}")
	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = state.thermostatResponse[child.device.deviceNetworkId]

	debugEvent("Data = ${tData}")

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"
		return null
	}

	def mode = tData.data.thermostatMode
	return mode
}

def getChildDeviceIdsString() {
	return thermostats.collect { it.split(/\./).last() }.join(',')
}

def toJson(Map m) {
    return groovy.json.JsonOutput.toJson(m)
}

def toQueryString(Map m) {
	return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}

private void saveTokenAndResumeAction(json) {
    log.debug "token response json: $json"
    if (json) {
        debugEvent("Response = $json")
        atomicState.refreshToken = json?.refresh_token
        atomicState.authToken = json?.access_token
        if (atomicState.action) {
            log.debug "got refresh token, executing next action: ${atomicState.action}"
            "${atomicState.action}"()
        }
    } else {
        log.warn "did not get response body from refresh token response"
    }
    atomicState.action = ""
}

/**
 * Executes the resume program command on the Ecobee thermostat
 * @param deviceId - the ID of the device
 *
 * @retrun true if the command was successful, false otherwise.
 */
boolean resumeProgram(deviceId) {
    def payload = [
        selection: [
            selectionType: "thermostats",
            selectionMatch: deviceId,
            includeRuntime: true
        ],
        functions: [
            [
                type: "resumeProgram"
            ]
        ]
    ]
    return sendCommandToEcobee(payload)
}

/**
 * Executes the set hold command on the Ecobee thermostat
 * @param heating - The heating temperature to set in fahrenheit
 * @param cooling - the cooling temperature to set in fahrenheit
 * @param deviceId - the ID of the device
 * @param sendHoldType - the hold type to execute
 *
 * @return true if the command was successful, false otherwise
 */
boolean setHold(heating, cooling, deviceId, sendHoldType) {
    // Ecobee requires that temp values be in fahrenheit multiplied by 10.
    int h = heating * 10
    int c = cooling * 10

    def payload = [
        selection: [
            selectionType: "thermostats",
            selectionMatch: deviceId,
            includeRuntime: true
        ],
        functions: [
            [
                type: "setHold",
                params: [
                    coolHoldTemp: c,
                    heatHoldTemp: h,
                    holdType: sendHoldType
                ]
            ]
        ]
    ]

    return sendCommandToEcobee(payload)
}

/**
 * Executes the set fan mode command on the Ecobee thermostat
 * @param heating - The heating temperature to set in fahrenheit
 * @param cooling - the cooling temperature to set in fahrenheit
 * @param deviceId - the ID of the device
 * @param sendHoldType - the hold type to execute
 * @param fanMode - the fan mode to set to
 *
 * @return true if the command was successful, false otherwise
 */
boolean setFanMode(heating, cooling, deviceId, sendHoldType, fanMode) {
    // Ecobee requires that temp values be in fahrenheit multiplied by 10.
    int h = heating * 10
    int c = cooling * 10

    def payload = [
        selection: [
            selectionType: "thermostats",
            selectionMatch: deviceId,
            includeRuntime: true
        ],
        functions: [
            [
                type: "setHold",
                params: [
                    coolHoldTemp: c,
                    heatHoldTemp: h,
                    holdType: sendHoldType,
                    fan: fanMode
                ]
            ]
        ]
    ]

	return sendCommandToEcobee(payload)
}

/**
 * Sets the mode of the Ecobee thermostat
 * @param mode - the mode to set to
 * @param deviceId - the ID of the device
 *
 * @return true if the command was successful, false otherwise
 */
boolean setMode(mode, deviceId) {
    def payload = [
        selection: [
            selectionType: "thermostats",
            selectionMatch: deviceId,
            includeRuntime: true
        ],
        thermostat: [
            settings: [
                hvacMode: mode
            ]
        ]
    ]
	return sendCommandToEcobee(payload)
}

/**
 * Makes a request to the Ecobee API to actuate the thermostat.
 * Used by command methods to send commands to Ecobee.
 *
 * @param bodyParams - a map of request parameters to send to Ecobee.
 *
 * @return true if the command was accepted by Ecobee without error, false otherwise.
 */
private boolean sendCommandToEcobee(Map bodyParams) {
	def isSuccess = false
	def cmdParams = [
		uri: apiEndpoint,
		path: "/1/thermostat",
		headers: ["Content-Type": "application/json", "Authorization": "Bearer ${atomicState.authToken}"],
		body: toJson(bodyParams)
	]

	try{
        httpPost(cmdParams) { resp ->
            if(resp.status == 200) {
                log.debug "updated ${resp.data}"
                def returnStatus = resp.data.status.code
                if (returnStatus == 0) {
                    log.debug "Successful call to ecobee API."
                    isSuccess = true
                } else {
                    log.debug "Error return code = ${returnStatus}"
                    debugEvent("Error return code = ${returnStatus}")
                }
            }
        }
	} catch (groovyx.net.http.HttpResponseException e) {
        log.trace "Exception Sending Json: " + e.response.data.status
        debugEvent ("sent Json & got http status ${e.statusCode} - ${e.response.data.status.code}")
        if (e.response.data.status.code == 14) {
            // TODO - figure out why we're setting the next action to be pollChildren
            // after refreshing auth token. Is it to keep UI in sync, or just copy/paste error?
            atomicState.action = "pollChildren"
            log.debug "Refreshing your auth_token!"
            refreshAuthToken()
        } else {
            debugEvent("Authentication error, invalid authentication method, lack of credentials, etc.")
            log.error "Authentication error, invalid authentication method, lack of credentials, etc."
        }
    }

    return isSuccess
}

def getChildName()           { return "Sensi Thermostat" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getShardUrl()            { return getApiServerUrl() }
def getApiTestEndpoint()         { return "https://tnrtkrucm4ig.runscope.net" }
def getApiEndpoint()		 { return "https://bus-serv.sensicomfort.com" }

def debugEvent(message, displayEvent = false) {
	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)
}


/**
 * Stores data about the thermostats in atomicState.
 * @param thermostats - a list of thermostats as returned from the Ecobee API
 */
private void storeThermostatData(thermostats) {
    log.trace "Storing thermostat data: $thermostats"
    def data
    atomicState.thermostats = thermostats.inject([:]) { collector, stat ->
        def dni = [ app.id, stat.identifier ].join('.')
        log.debug "updating dni $dni"

        data = [
            coolMode: (stat.settings.coolStages > 0),
            heatMode: (stat.settings.heatStages > 0),
            deviceTemperatureUnit: stat.settings.useCelsius,
            minHeatingSetpoint: (stat.settings.heatRangeLow / 10),
            maxHeatingSetpoint: (stat.settings.heatRangeHigh / 10),
            minCoolingSetpoint: (stat.settings.coolRangeLow / 10),
            maxCoolingSetpoint: (stat.settings.coolRangeHigh / 10),
            autoMode: stat.settings.autoHeatCoolFeatureEnabled,
            deviceAlive: stat.runtime.connected == true ? "true" : "false",
            auxHeatMode: (stat.settings.hasHeatPump) && (stat.settings.hasForcedAir || stat.settings.hasElectric || stat.settings.hasBoiler),
            temperature: (stat.runtime.actualTemperature / 10),
            heatingSetpoint: stat.runtime.desiredHeat / 10,
            coolingSetpoint: stat.runtime.desiredCool / 10,
            thermostatMode: stat.settings.hvacMode,
            humidity: stat.runtime.actualHumidity,
            thermostatFanMode: stat.runtime.desiredFanMode
        ]
        if (location.temperatureScale == "F") {
            data["temperature"] = data["temperature"] ? Math.round(data["temperature"].toDouble()) : data["temperature"]
            data["heatingSetpoint"] = data["heatingSetpoint"] ? Math.round(data["heatingSetpoint"].toDouble()) : data["heatingSetpoint"]
            data["coolingSetpoint"] = data["coolingSetpoint"] ? Math.round(data["coolingSetpoint"].toDouble()) : data["coolingSetpoint"]
            data["minHeatingSetpoint"] = data["minHeatingSetpoint"] ? Math.round(data["minHeatingSetpoint"].toDouble()) : data["minHeatingSetpoint"]
            data["maxHeatingSetpoint"] = data["maxHeatingSetpoint"] ? Math.round(data["maxHeatingSetpoint"].toDouble()) : data["maxHeatingSetpoint"]
            data["minCoolingSetpoint"] = data["minCoolingSetpoint"] ? Math.round(data["minCoolingSetpoint"].toDouble()) : data["minCoolingSetpoint"]
            data["maxCoolingSetpoint"] = data["maxCoolingSetpoint"] ? Math.round(data["maxCoolingSetpoint"].toDouble()) : data["maxCoolingSetpoint"]

        }

        if (data?.deviceTemperatureUnit == false && location.temperatureScale == "F") {
            data["deviceTemperatureUnit"] = "F"

        } else {
            data["deviceTemperatureUnit"] = "C"
        }

        collector[dni] = [data:data]
        return collector
    }
    log.debug "updated ${atomicState.thermostats?.size()} thermostats: ${atomicState.thermostats}"
}

def convertFtoC (tempF) {
	return String.format("%.1f", (Math.round(((tempF - 32)*(5/9)) * 2))/2)
}

def sendActivityFeeds(notificationMessage) {
	def devices = getChildDevices()
	devices.each { child ->
		child.generateActivityFeedsEvent(notificationMessage) //parse received message from parent
	}
}