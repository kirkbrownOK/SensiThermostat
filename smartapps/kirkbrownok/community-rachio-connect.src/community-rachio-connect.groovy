/**
 *  Copyright 2017 Kirk Brown
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
 *	Community Driven Rachio IRO Controller -> Because SmartThings won't approve the Official One
 *
 *	Author: Kirk Brown
 *	Date: 2017- January
 *
 *	Place the Community Rachio (Connect) code under the My SmartApps section. Be certain you publish the app for you.
 *  Place the Rachio Zone Device Type Handler under My Device Handlers section.
 *
 *  Be careful that if you change the Name and Namespace you that additionally change it in the addDevice() function
 *
 *
 *  The Program Flow is as follows:
 *  1.	SmartApp gets user credentials in the install process. The app uses the credentials to get the Api Access Token
 * There are a large number of debug statements that will turn on if you uncomment the statement inside the TRACE function at the bottom of the code
 */
 
definition(
		name: "Community Rachio (Connect)",
		namespace: "kirkbrownOK",
		author: "Kirk Brown",
		description: "Connect your Rachio SmartThings. (Unofficial)",
		category: "SmartThings Labs",
		iconUrl: "http://i.imgur.com/QVbsCpu.jpg",
		iconX2Url: "http://i.imgur.com/4BfQn6I.jpg",
		singleInstance: true
)

preferences {
	page(name: "auth", title: "Rachio", nextPage:"", content:"authPage", uninstall: true)
	page(name: "getDevicesPage", title: "Rachio Zones", nextPage:"", content:"getDevicesPage", uninstall: true, install: true)
}

def authPage() {

	def description
	def uninstallAllowed = false
	if(state.connectionToken) {
		description = "You are connected."
		uninstallAllowed = true
	} else {
		description = "Click to enter Rachio Credentials"
	}

		return dynamicPage(name: "auth", title: "Login", nextPage: "getDevicesPage", uninstall:uninstallAllowed) {
			section() {
				paragraph "Enter your API Access Token. Log in to the Rachio Client. Click on the user options and find the API Access Token. " +
                	"Your token will be saved in SmartThings in whatever secure/insecure manner SmartThings saves them."
				input("userAPIToken", "string", title:"Rachio API Token", required:true, displayDuringSetup: true)
                //input("userName", "string", title:"Rachio Email Address", required:true, displayDuringSetup: true)
    			//input("userPassword", "password", title:"Rachio account password", required:true, displayDuringSetup:true)	
                input("pollInput", "number", title: "How often should ST poll Rachio? (in minutes)", required: false, displayDureingSetup: true, defaultValue: 5)
			}
		}

}
def getDevicesPage() {
    getRachioInfo()       
    def _zones = getRachioDevices()
    return dynamicPage(name: "getDevicesPage", title: "Select Your Zones", uninstall: true) {
        section("") {
            paragraph "Tap below to see the list of Rachio Zones available in your Rachio account and select the ones you want to connect to SmartThings."
            input(name: "myZones", title:"", type: "enum", required:false, multiple:true, description: "Tap to choose", metadata:[values:_zones])
            
        }
    }
}


def getDeviceDisplayName(dev) {
    if(dev?.DeviceName) {
        return dev.DeviceName.toString()
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
    unschedule()
	initialize()
}

def initialize() {

	def devices = myZones.collect { dni ->
		def d = getChildDevice(dni)
        
        //TRACE("dni: $dni device: $d $state.rachioDevices")
		if(!d) {
        	if(state.rachioControllers[dni] != null) {        	
            	//TRACE("Its a controller device: ${state.rachioDevices[dni]}")
                
                //TRACE( "addChildDevice($app.namespace, ${getControllerName()}, $dni, null, [\"name\": ${state.rachioDevices[dni]},\"label\":\"Rachio Controller:$state.rachioDevices[dni]\"])")
				//d = addChildDevice(app.namespace, getControllerName(), dni, null, ["name":"Rachio Zone: $state.rachioDevices[dni],"label":"$state.rachioDevices[dni]"])
         	} else {   
            	//TRACE("Its a zone device: ${state.rachioDevices[dni]}")
                //TRACE( "addChildDevice($app.namespace, ${getChildName()}, $dni, null, [\"name\": ${state.rachioDevices[dni]},\"label\":\"Rachio Zone:$state.rachioDevices[dni]\"])")
				d = addChildDevice(app.namespace, getChildName(), dni, null, ["name":"Rachio Zone: ${state.rachioDevices[dni]}","label":"${state.rachioDevices[dni]}"])
            }
            log.debug "created ${d.name} with id $dni"
		} else {
			//log.debug "found ${d.displayName} with id $dni already exists"
		}
		return d
	}

	TRACE( "created ${devices.size()} zones.")

	def delete  // Delete any that are no longer in settings
	if(!myZones) {
		log.debug "delete zones"
		delete = getAllChildDevices() //inherits from SmartApp (data-management)
	} else { //delete only thermostat
		log.debug "delete individual zone"
		delete = getChildDevices().findAll { !myZones.contains(it.deviceNetworkId) }		
	}
	log.warn "delete: ${delete}, deleting ${delete.size()} zones"
	delete.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)

	//send activity feeds to tell that device is connected
	def notificationMessage = "is connected to SmartThings"
	sendActivityFeeds(notificationMessage)

	try{
		//poll() //first time polling data data from thermostat
	} catch (e) {
    	log.warn "Error in first time polling. Could mean something is wrong."
    }
	//automatically update devices status every 5 mins
    def pollRate = pollInput == null ? 5 : pollInput
    if(pollRate > 59) {
    	pollRate = 5
        log.warn "You picked an invalid pollRate: $pollInput minutes. Changed to 5 minutes."
    }    
	//schedule("0 0/${pollRate} * * * ?","poll")
    

}

def getAuthorized() {
	TRACE("Get Authorized")
	def deviceListParams = [
		uri: getApiEndpoint(),
		path: "/login?",
		headers: ["Content-Type": "application/json", "Accept": "application/json; text/javascript, */*; q=0.01", "X-Requested-With":"XMLHttpRequest"],
		body: [ username: userName, password: userPassword]
	]
    TRACE("dlp: $deviceListParams")
	try {
		httpPostJson(deviceListParams) { resp ->
        	//log.debug "Resp Headers: ${resp.headers}"
            //log.debug "Resp $resp"
			if (resp.status == 200) {
				resp.headers.each {
            		log.debug "${it.name} : ${it.value}"
   
        		}
                resp.data.each {
                	log.debug "${it}"
                    
                }
			} else {
				log.debug "http status: ${resp.status}"
			}
		}
	} catch (e) {
        log.trace "Exception trying to authenticate $e"
    }	

}
//This function uses the API token to retrieve the User ID
def getRachioInfo() {
	state.apiToken = userAPIToken
	def deviceListParams = [
		uri: apiEndpoint,
		path: "/1/public/person/info",
        requestContentType: "application/json",
        //contentType: "application/json",
		headers: ["Authorization":"Bearer ${state.apiToken}"]        
	]
	//log.debug "getRachio Params: ${deviceListParams}"
	try {
		httpGet(deviceListParams) { resp ->
        	//TRACE("resp: $resp.data")
			if (resp.status == 200) {
            	
				resp.data.each { name, value ->
                	if(name =="id") {
                    	state.id = value
                    }
				}
			} else {
				log.debug "http status: ${resp.status}"
			}
		}
	} catch (e) {
        log.trace "Exception getting rachio id " + e
        state.connected = false
        return false
    }
    TRACE("User ID: $state.id")
	return true
}
//This function uses the User ID to retrieve the Device ID and details
def getRachioDevices() {
	state.rachioDevices = [:]
    state.rachioControllers =[:]
    def myDevices = [:]
	def deviceListParams = [
		uri: apiEndpoint,
		path: "/1/public/person/${state.id}",
        requestContentType: "application/json",
        //contentType: "application/json",
		headers: ["Authorization":"Bearer ${state.apiToken}"]        
	]
	log.debug "getRachioControllers: ${deviceListParams}"
	try {
		httpGet(deviceListParams) { resp ->
			if (resp.status == 200) {
                //This contains the summary of Controllers
                resp.data.devices.each { controllers ->
                	state.rachioDevices[controllers.id.toString()] = controllers.name.toString()
                    state.rachioControllers[controllers.id.toString()] = controllers.name.toString()
                    myDevices[controllers.id.toString()] = controllers.name.toString()
                	controllers.each { name, value ->                    	
                       	if(name =="zones") {                           
                            value.each{ zone ->
                            	zone.each{zname, zvalue ->
                                	if(zname == "name" && zone.enabled) {
                                    	//TRACE("name: $zname, value: $zvalue")
                                        state.rachioDevices[zone.id.toString()] = zvalue.toString()
                                        myDevices[zone.id.toString()] = zvalue.toString()
                                    }
                					
                                
                                }
                            }
                            
                            
                        }
                		
					}
				}
				//resp.data.devices.each { name, value ->
                //	TRACE("name: $name, value: $value")
				//}
			} else {
				log.debug "http status: ${resp.status}"
			}
		}
	} catch (e) {
        log.trace "Exception getting device ids " + e
        state.connected = false
    }
    //TRACE("Rachio Controllers: $state.rachioControllers")
    TRACE("Rachio Devices: ${myDevices}")
    return myDevices
}

def pollHandler() {
	TRACE("pollHandler()")
	pollChildren(null) // Hit the sensi API for update on all thermostats

}

def pollChildren() {
	def devices = getChildDevices()
    TRACE("Update Zones")
    try{

    } catch (e) {
        log.error "Error $e in pollChildren() for $child.device.label"
    }
	devices.each { child ->
	
    
    }

    return true
}
def getSubscribed(thermostatIdsString) {
	if(state.lastSubscribedDNI == thermostatIdsString) {
    	TRACE("Thermostat already subscribed")
        return true
    } else if(state.lastSubscribedDNI != null) {
    	TRACE("Thermostat not subscribed")
    	getUnsubscribed(state.lastSubscribedDNI)
    }
	TRACE("Getting subscribed to $thermostatIdsString")
	if(!state.connected) { getConnected() }
    if( state.RBCounter > 50) {
    	state.RBCounter = 0
    }
    def requestBody = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"Subscribe\",\"A\":[\"${thermostatIdsString}\"],\"I\":$state.RBCounter}"]
    state.RBCounter = state.RBCounter + 1


    def params = [    	
        uri: getApiEndpoint(),
        path: '/realtime/send',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
        body: requestBody
    ]

    try {

        httpPost(params) { resp ->
            TRACE( "Subscribe response: ${resp.data} Expected Response: {I:${state.RBCounter - 1}")
            if(resp?.data.I?.toInteger() == (state.RBCounter - 1)) {
				state.lastSubscribedDNI = thermostatIdsString
                TRACE("Subscribe successfully")
            } else {
            	TRACE("Failed to subscribe")
                state.connected = false
            }
        }
    } catch (e) {
        log.error "getSubscribed failed: $e"
        state.connected = false
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite
    }
}

def getUnsubscribed(thermostatIdsString) {

    //Unsubscribe from this device
    def requestBody3 = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"Unsubscribe\",\"A\":[\"${thermostatIdsString}\"],\"I\":$state.RBCounter}"]
    def params = [    	
        uri: getApiEndpoint(),
        path: '/realtime/send',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
        body: requestBody3
    ]
	state.RBCounter = state.RBCounter + 1
    try {

        httpPost(params) { resp ->
            TRACE( "resp 3: ${resp.data}")
        }
    } 
    catch (e) {
        log.trace "Exception unsubscribing " + e
        state.connected = false
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite
    }
    state.lastSubscribedDNI = null
        
}	
def pollChildData(data) {
	def device = getChildDevice(data.value)
	TRACE("Scheduled re-poll of $device.deviceLabel")
    pollChild(data.value)
}
// Poll Child is invoked from the Child Device itself as part of the Poll Capability
// If no dni is passed it will call pollChildren and poll all devices
def pollChild(dni = null) {
	
	if(dni == null) {
    	TRACE("dni in pollChild is $dni")
    	pollChildren()
        return
    }
    def thermostatIdsString = dni

    def params = []
    def result = false
    if(!state.connected || (state.messageId == null)) {
        getConnected()
    }    
    getSubscribed(thermostatIdsString)
    params = [
        uri: getApiEndpoint(),
        path: '/realtime/poll',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]"
                ,connectionId:state.connectionId,messageId:state.messageId,tid:state.RBCounter,'_':now()],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"]
    ]
    if(state.GroupsToken) {
        params.query = [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]"
                        ,connectionId:state.connectionId,messageId:state.messageId,GroupsToken:state.GroupsToken,tid:state.RBCounter,'_':now()]
    }

    try{
        httpGet(params) { resp ->
        	def httpResp =  resp.data.M[0].A[1] == null ? " " : resp.data.M[0].A[1]
            if(httpResp && (httpResp != true)) {            	
                state.thermostatResponse[thermostatIdsString] = httpResp
                TRACE("child.generateEvent=$httpResp")
                def myChild = getChildDevice(dni)
                myChild.generateEvent(httpResp)
				result = true
            } else {
            	log.debug "Unexpected final resp: ${resp.data}"
            }
            if(resp.data.C) {            	
                state.messageId = resp.data.C

            }
            if(resp.data.G) {
                state.GroupsToken = resp.data.G
            }
        }
        state.RBCounter = state.RBCounter + 1
    } catch (e) {
        log.trace "Exception polling child $e repoll in 30 seconds"
        state.connected = false //This will trigger new authentication next time the poll occurs   
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite
    }
            
	return result
}

void poll() {
	pollChildren()
}

def availableModes(child) {


	def modes = ["off", "heat", "cool", "aux", "auto"]

    return modes
}

def currentMode(child) {
	debugEvent ("state.Thermos = ${state.thermostats}")
	debugEvent ("Child DNI = ${child.device.deviceNetworkId}")

	def tData = state.thermostatResponse[child.device.deviceNetworkId]?.EnvironmentControls

	//debugEvent("Data = ${tData}")

	if(!tData) {
		log.error "ERROR: Device connection removed? no data for ${child.device.deviceNetworkId} after polling"
		return null
	}

	def mode = tData?.SystemMode
	return mode
}

/**
 * Executes the cmdString and cmdVal
 * @param deviceId - the ID of the device
 * @cmdString is passed directly to Sensi Web
 * @cmdVal is the value to send on.
 *
 * @retrun true if the command was successful, false otherwise.
 */

boolean setStringCmd(deviceId, cmdString, cmdVal) {
	//getConnected()
    getSubscribed(deviceId)
    def result = sendDniStringCmd(deviceId,cmdString,cmdVal)
    log.debug "Setstring ${result}"
    //The sensi web app immediately polls the thermostat for updates after send before unsubscribe
    pollChild(deviceId)
    //getUnsubscribed(deviceId)
    return result
}
boolean setTempCmd(deviceId, cmdString, cmdVal) {
	//getConnected()
    getSubscribed(deviceId)
    def result = sendDniValue(deviceId,cmdString,cmdVal)
    log.debug "Setstring ${result}"
    //The sensi web app immediately polls the thermostat for updates after send before unsubscribe
    pollChild(deviceId)
    //getUnsubscribed
    return result
}
boolean sendDniValue(thermostatIdsString,cmdString,cmdVal) {
	def result = false
    def requestBody = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"$cmdString\",\"A\":[\"${thermostatIdsString}\",$cmdVal,\"$location.temperatureScale\"],\"I\":$state.RBCounter}"]
    
	TRACE( "sendDNIValue body: ${requestBody}")
    def params = [    	
        uri: getApiEndpoint(),
        path: '/realtime/send',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
        body: requestBody
    ]

    try {

        httpPost(params) { resp ->
            
            if (resp.data.I.toInteger() == state.RBCounter.toInteger()) {
            	result = true
            }
            state.RBCounter = state.RBCounter + 1
        }
    } catch (e) {
        log.error "Send DNI Command went wrong: $e"
        state.connected = false
        state.RBCounter = state.RBCounter + 1

    }
    
    return result
}
boolean sendDniStringCmd(thermostatIdsString,cmdString,cmdVal) {
	def result = false
    def requestBody = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"$cmdString\",\"A\":[\"${thermostatIdsString}\",\"$cmdVal\"],\"I\":$state.RBCounter}"]
    
    def params = [    	
        uri: getApiEndpoint(),
        path: '/realtime/send',
        query: [transport:'longPolling',connectionToken:state.connectionToken,connectionData:"[{\"name\": \"thermostat-v1\"}]",connectionId:state.connectionId],
        headers: ['Cookie':state.myCookie,'Accept':'application/json; version=1, */*; q=0.01', 'Accept-Encoding':'gzip','Content-Type':'application/x-www-form-urlencoded',"X-Requested-With":"XMLHttpRequest"],
        body: requestBody
    ]

    try {

        httpPost(params) { resp ->
            
            if (resp.data.I.toInteger() == state.RBCounter.toInteger()) {
            	result = true
            }
            state.RBCounter = state.RBCounter + 1
        }
    } catch (e) {
        log.error "Send DNI Command went wrong: $e"
        state.connected = false
        state.RBCounter = state.RBCounter + 1
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite

    }
    TRACE( "Send Function : $result")
    return result
}


def getChildName()           { return "Unofficial Rachio Zone" }
def getControllerName()      { return "Unofficial Rachio Controller" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getApiEndpoint()		 { return "https://api.rach.io" }


def sendActivityFeeds(notificationMessage) {
	def devices = getChildDevices()
	devices.each { child ->
		child.generateActivityFeedsEvent(notificationMessage) //parse received message from parent
	}
}

private def TRACE(message) {
    log.debug message
}