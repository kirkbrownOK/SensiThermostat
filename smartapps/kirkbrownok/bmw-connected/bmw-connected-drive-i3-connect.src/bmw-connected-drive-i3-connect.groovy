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
 *	BMW Connected Drive service manager to create car device
 *
 *	Author: Kirk Brown
 *	Date: 2017-9-1
 *	Place the BMW (Connect) code under the My SmartApps section. Be certain you publish the app for you.
 *  Place the BMW Car Device Type Handler under My Device Handlers section.
 *  Be careful that if you change the Name and Namespace you that additionally change it in the addChildDevice() function
 *
 *
 *  The Program Flow is as follows:
 *  1.	SmartApp gets user credentials in the install process.
 *  2.	The SmartApp gets the user’s car and list it for subscription in the SmartApp. -> I can only afford 1 BMW so i'm writing for 1 car.
 *  	a.	The smartApp uses the user’s credentials to get authorized, get a connection token, and then list the car
 *  3.	The User then selects the desired car(s) to add to SmartThings -> Can't test multiple cars, don't intend to write for that.
 *  4.	The SmartApp schedules a refresh/poll of the car every so often. Default is 60 minutes for now but can be changed in the install configurations. The interface is not official, so polling to often could get noticed.
 *  5. The car can be polled by a pollster type SmartApp.
 * There are a large number of debug statements that will turn on if you uncomment the statement inside the TRACE function at the bottom of the code
 */
 
definition(
		name: "BMW Connected Drive i3 (Connect)",
		namespace: "kirkbrownOK/BMW_Connected",
		author: "Kirk Brown",
		description: "Connect your BMW i3 to SmartThings.",
		category: "SmartThings Labs",
		iconUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/BMW.svg/1200px-BMW.svg.png",
		iconX2Url: "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/BMW.svg/1200px-BMW.svg.png",
		singleInstance: true
)

preferences {
	page(name: "auth", title: "BMW Connected", nextPage:"", content:"authPage", uninstall: true)
	page(name: "getDevicesPage", title: "BMW Cars", nextPage:"", content:"getDevicesPage", uninstall: true, install: true)
}

def authPage() {

	def description
	def uninstallAllowed = false
	if(state.connectionToken) {
		description = "You are connected."
		uninstallAllowed = true
	} else {
		description = "Click to enter BMW Credentials"
	}

		return dynamicPage(name: "auth", title: "Login", nextPage: "getDevicesPage", uninstall:uninstallAllowed) {
			section() {
                paragraph "This connected app is not sanctioned by BMW. It requires api key, and secret from the Android/iPhone app in order to work. "+
                	"according to edent: \"You can get the i Remote details from either decompiling the Android App or from intercepting communications "+
                    "between your phone and the BMW server. This is left as an exercise for the reader. see: https://github.com/edent/BMW-i-Remote "+
                    "The key will look like -> Authorization: Basic APIKEY:APISECRET where APIKEY:APISECRET is a crazy encoded hex string. Use the encoded "
                    "hex string for the following input."
                input("encodedSecret", "string", title: " API Key and Secret", required: true, displayDuringSetup: true, defaultValue:"blF2NkNxdHhKdVhXUDc0eGYzQ0p3VUVQOjF6REh4NnVuNGNEanliTEVOTjNreWZ1bVgya0VZaWdXUGNRcGR2RFJwSUJrN3JPSg==")               	
				paragraph "Enter your Username and Password for BMW Connected Drive. Your username and password will be saved in SmartThings in whatever secure/insecure manner SmartThings saves them."
				input("userName", "string", title:"BMW Email Address", required:true, displayDuringSetup: true,defaultValue: "tokirk.brown@gmail.com")
    			input("userPassword", "password", title:"BMW account password", required:true, displayDuringSetup:true,defaultValue:"Transport3")	
                input("secretAnswer", "password", title:"Answer to your security question from BMW Connected Account", required: true, displayDuringSetup: true, defaultValue:"Iron Druid")
                input("pollInput", "number", title: "How often should ST poll BMW Connected Drive? (minutes)", required: false, defaultValue: 30, displayDuringSetup: true)
			}
		}

}
def getDevicesPage() {
    getAuthorized()
         
    def myCars = getBMWCars()
    return dynamicPage(name: "getDevicesPage", title: "Select Your Cars", uninstall: true) {
        section("") {
            paragraph "Tap below to see the list of cars available in your BMW account and select the ones you want to connect to SmartThings."
            input(name: "cars", title:"", type: "enum", required:false, multiple:true, description: "Tap to choose", metadata:[values:myCars])
            
        }
    }
}


def getCarDisplayName(myCar) {
    if(myCar?.model) {
    	def nameString = "${myCar.yearOfConstruction} BMW ${myCar.model}"
        return nameString
    }
    return "Unknown"
}

def installed() {
	log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	state.refresh_token = null
	log.info "Updated with settings: ${settings}"
	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {

    getAuthorized()
    
	def devices = cars.collect { vin ->
		def d = getChildDevice(vin)
		if(!d) {
        	TRACE( "addChildDevice($app.namespace, ${getChildName()}, $vin, null, [\"label\":\"${state.bmwCars[vin]}\" : \"BMW Car\"])")
			d = addChildDevice(app.namespace, getChildName(), vin, null, ["label":"${state.bmwCars[vin]}" ?: "BMW Car"])
			log.info "created ${d.displayName} with id $vin"
		} else {
			log.info "found ${d.displayName} with id $vin already exists"
		}
		return d
	}

	TRACE( "created ${devices.size()} car(s).")

	def delete  // Delete any that are no longer in settings
	if(!cars) {
		log.info "delete cars"
		delete = getAllChildDevices() //inherits from SmartApp (data-management)
	} else { //delete only thermostat
		log.info "delete individual car"
		delete = getChildDevices().findAll { !cars.contains(it.deviceNetworkId) }		
	}
	log.warn "delete: ${delete}, deleting ${delete.size()} cars"
	delete.each { deleteChildDevice(it.deviceNetworkId) } //inherits from SmartApp (data-management)

	//send activity feeds to tell that device is connected
	def notificationMessage = "is connected to SmartThings"
	sendActivityFeeds(notificationMessage)
	state.timeSendPush = null
	state.reAttempt = 0

	try{
		poll() //first time polling data from device
	} catch (e) {
    	log.warn "Error in first time polling. Could mean something is wrong."
    }
	//automatically update devices status every 5 mins
    def pollRate = pollInput == null ? 5 : pollInput
    if(pollRate > 59 || pollRate < 1) {
    	pollRate = 58
        log.warn "You picked an invalid pollRate: $pollInput minutes. Changed to 58 minutes."
    }    
	schedule("0 0/${pollRate} * * * ?","poll")
    

}

def getAuthorized() {
	if ( (state.expiresAt > now() )&&( state.refresh_token != null)) {
    	TRACE("Still authorized-> not refreshing auth\n $now() expires at $state.expiresAt")
        return
    }
    def deviceListParams = [:]
    if( state.refresh_token == null) {
    	TRACE("Authorizing using passwords")
        deviceListParams = [
            uri: getApiEndpoint(),
            path: "/webapi/oauth/token/",
            headers: ["Content-Type": "application/x-www-form-urlencoded","Authorization": "Basic ${encodedSecret}"],
            body: [grant_type: "password", password: userPassword, username: userName, scope: "remote_services vehicle_data" ]
        ]
    } else {
		TRACE("Authorizing using Refresh Token")
    	deviceListParams = [
            uri: getApiEndpoint(),
            path: "/webapi/oauth/token/",
            headers: ["Content-Type": "application/x-www-form-urlencoded","Authorization": "Basic ${encodedSecret}"],
            body: [grant_type: "refresh_token", refresh_token: "${state.refresh_token}"]
        ]
    }
	try {
		httpPost(deviceListParams) { resp ->       	
			if (resp.status == 200) {
            	TRACE("Status 200")
            	try{
                	TRACE("Headers:")
                    resp.headers.each {
                        //TRACE( "${it.name} : ${it.value}")

                    }
                } catch(e) {
                	TRACE("error $e in headers")
                }
                try{
                    resp.data.each { name, value ->
                        //TRACE("name:${name} , value: ${value}")
                        if(name == "access_token") {
                            state.access_token = value
                        } else if( name == "token_type") {
                            state.token_type = value
                        }  else if( name == "expires_in") {
                            state.expires_in = value
                            state.expiresAt = now() + value*1000
                            def tempNow = now()
                           
                        }  else if( name == "refresh_token") {
                            state.refresh_token = value
                        }  else if( name == "scope") {
                            state.scope = value
                        }
                  } 
            	}catch(e) {
          			log.info "No new data"
        		}
                printState()
			} else {
				TRACE( "http status: ${resp.status}")
			}
		}
	} catch (e) {
        log.warn "Exception trying to authenticate $e"
    }	

}
def printState() {
	TRACE("State:\naccess_token: $state.access_token\ntoken_type: $state.token_type\nexpires_in: $state.expires_in\nrefresh_token: $state.refresh_token\nscope: $state.scope")
}

def getBMWCars() {
	TRACE("Getting Cars")
	state.bmwCars = []
	def deviceListParams = [
		uri: apiEndpoint,
		path: "/webapi/v1/user/vehicles/",
        contentType: 'application/json',
		headers: ["Authorization":"$state.token_type $state.access_token"]        
	]
	def myCars = [:]
	try {
		httpGet(deviceListParams) { resp ->        	
			if (resp.status == 200) {
            	
				resp.data.vehicles.each { myCar ->
                	TRACE("myCar:\n$myCar")
					state.bmwCars = state.bmwCars == null ? myCar.model : state.bmwCars <<  myCar.model
					def vin = myCar.vin
					myCars[vin] = getCarDisplayName(myCar)
				}
			} else {
            	state.refresh_token = null
				log.warn "Failed to get car list in getBMWCars: ${resp.status}"
			}
		}
	} catch (e) {
        log.trace "Exception getting cars: " + e
        state.refresh_token = null
    }
	state.bmwCars = myCars
    state.bmwResponse = myCars
	return myCars
}
def pollHandler() {
	//log.debug "pollHandler()"
	pollChildren(null) // Hit the sensi API for update on all thermostats

}

def pollChildren() {
	def devices = getChildDevices()
	devices.each { child ->
    	TRACE("pollChild($child.device.deviceNetworkId)")
        try{
            if(pollChild(child.device.deviceNetworkId)) {
                TRACE("pollChildren successful")

            } else {
                log.warn "pollChildren FAILED for $child.device.label"
                state.connected = false
                runIn(30, poll)
            }
        } catch (e) {
        	state.refresh_token = null
        	log.error "Error $e in pollChildren() for $child.device.label"
        }
    }
    return true
}
def getSubscribed(thermostatIdsString) {
	/*
	if(state.lastSubscribedDNI == thermostatIdsString) {
    	TRACE("Thermostat already subscribed")
        return true
    } else */
    if(state.lastSubscribedDNI != null) {
    	TRACE("Unsubscribing from: $state.lastSubscribedDNI")
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
            TRACE( "Subscribe response: ${resp.data} Expected Response: [I:${state.RBCounter - 1}]")
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

def pollChildData(data) {
	def device = getChildDevice(data.value)
	log.info "Scheduled re-poll of $device.deviceLabel $data.value $device.label"
    pollChild(data.value)
}
public deviceTimeDateFormat() { "yyyy-MM-dd'T'HH:mm:ss" }
// Poll Child is invoked from the Child Device itself as part of the Poll Capability
// If no dni is passed it will call pollChildren and poll all devices
def pollChild(vin = null) {
	TRACE("poll child called for ${vin}")
	if(vin == null) {
    	TRACE("No vin calling all cars")
    	pollChildren()
        return
    }
    def vinString = vin

    def params = []
    def result = false
	getAuthorized()
    
    def timeString = new Date(now()+ location.timeZone.rawOffset + location.timeZone.dstSavings  ).format(deviceTimeDateFormat())
    TRACE("devTime: ${timeString}")
    
    params = [
        uri: getApiEndpoint(),
        path: "/webapi/v1/user/vehicles/${vinString}/status",
        contentType: 'application/json',
		headers: ["Authorization":"$state.token_type $state.access_token"],
        query: [deviceTime: "${timeString}"]
    ]
    TRACE("Status Params:\n $params")

    try{
        httpGet(params) { resp ->
        	TRACE("Status GET:\n$resp.data")
        	def httpResp =  resp.data.vehicleStatus == null ? " " : resp.data.vehicleStatus
            if(httpResp && (httpResp != true)) {            	
                state.bmwResponse[vinString] = httpResp
                TRACE("child.generateEvent=$httpResp")
                def myChild = getChildDevice(vin)
                myChild.generateEvent(httpResp)
				result = true
            } else {
            	httpResp = resp.data.vehicleStatus == null ? " " : resp.data.vehicleStatus
            	log.warn "Unexpected final resp in pollChild: ${resp.data} likely offline: $httpResp"
            }
        }
        
    } catch (e) {
    	state.refresh_token = null
        log.error "Exception in pollChild: $e "
        log.error "repoll in 30 seconds. Re-poll: $vinString"
        
        //runIn(30, pollChildData,[data: [value: vinString], overwrite: true]) //when user click button this runIn will be overwrite
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
boolean sendExecuteService(deviceId, serviceType, useKey = 0) {
	state.lastServiceType = serviceType
    state.lastVIN = deviceId
    def bodyParams = []
    if (useKey == 1) {
    	bodyParams = ["serviceType" : "$serviceType",extendedStatusUpdates: "false",bmwSkAnswer:"$secretAnswer"]
    } else {
    	bodyParams = ["serviceType" : "$serviceType",extendedStatusUpdates: "false"]
    }
    def params = [    	
        uri: getApiEndpoint(),
        path: "/webapi/v1/user/vehicles/${deviceId}/executeService",
        headers: ['Content-Type':'application/x-www-form-urlencoded',"Authorization":"$state.token_type $state.access_token"],
        body: bodyParams
    ]
	TRACE("SES:\n$params")
    try {

        httpPost(params) { resp ->
           TRACE("sendCmd: $resp.data")
            
        }
    } catch (e) {
        log.warn "Send executeService Command went wrong: $e"

    }
    runIn(60,checkCommand)
    return true	
}
def checkCommand() {
	def data = null
    def params = [    	
        uri: getApiEndpoint(),
        path: "/webapi/v1/user/vehicles/${state.lastVIN}/serviceExecutionStatus",
        headers: ['Content-Type':'application/x-www-form-urlencoded',"Authorization":"$state.token_type $state.access_token"],
        query: ["serviceType" : "$state.lastServiceType"]
    ]
	TRACE("serviceStatus:\n$params")
    try {

        httpGet(params) { resp ->
           TRACE("serviceStatus: $resp.data")
            data = resp
        }
    } catch (e) {
        log.warn "Get executeService status went wrong: $e"

    }	
    return data
}
boolean setStringCmd(deviceId, cmdString, cmdVal) {
	//getConnected()
    getSubscribed(deviceId)
    def result = sendDniStringCmd(deviceId,cmdString,cmdVal)
    TRACE( "Setstring ${result}")
    //The sensi web app immediately polls the thermostat for updates after send before unsubscribe
    pollChild(deviceId)
    getUnsubscribed(deviceId)
    return result
}
boolean setSettingsStringCmd(deviceId,cmdSettings, cmdString, cmdVal) {
	//getConnected()
    getSubscribed(deviceId)
    def result = sendDniSettingsStringCmd(deviceId,cmdSettings,cmdString,cmdVal)
    TRACE( "Setstring ${result}")
    //The sensi web app immediately polls the thermostat for updates after send before unsubscribe
    pollChild(deviceId)
    getUnsubscribed(deviceId)
    return result
}
boolean setTempCmd(deviceId, cmdString, cmdVal) {
	//getConnected()
    getSubscribed(deviceId)
    def result = sendDniValue(deviceId,cmdString,cmdVal)
    TRACE( "Setstring ${result}")
    //The sensi web app immediately polls the thermostat for updates after send before unsubscribe
    pollChild(deviceId)
    getUnsubscribed
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
        log.warn "Send DNI Command went wrong: $e"
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
        log.warn "Send DNI String Command went wrong: $e"
        state.connected = false
        state.RBCounter = state.RBCounter + 1
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite

    }
    TRACE( "Send Function : $result")
    return result
}
/* {"H":"thermostat-v1","M":"ChangeSetting","A":["thermostatid is here","KeypadLockout","Off"],"I":8} */
boolean sendDniSettingsStringCmd(thermostatIdsString,cmdSettings,cmdString,cmdVal) {
	def result = false
    def requestBody = ['data':"{\"H\":\"thermostat-v1\",\"M\":\"$cmdSettings\",\"A\":[\"${thermostatIdsString}\",\"$cmdString\",\"$cmdVal\"],\"I\":$state.RBCounter}"]
    
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
        log.warn "Send DNI Setting String Command went wrong: $e"
        state.connected = false
        state.RBCounter = state.RBCounter + 1
        runIn(30, pollChildData,[data: [value: thermostatIdsString], overwrite: true]) //when user click button this runIn will be overwrite

    }
    TRACE( "Send Function : $result")
    return result
}

def getChildName()           { return "BMW Connected Car" }
def getServerUrl()           { return "https://graph.api.smartthings.com" }
def getApiEndpoint()		 { return "https://b2vapi.bmwgroup.us" }
def getApiEndpoint2()		 { return "https://tnrtkrucm4ig.runscope.net" }
//tnrtkrucm4ig.runscope.net

def debugEvent(message, displayEvent = false) {
	def results = [
		name: "appdebug",
		descriptionText: message,
		displayed: displayEvent
	]
	log.debug "Generating AppDebug Event: ${results}"
	sendEvent (results)
}

def sendActivityFeeds(notificationMessage) {
	def devices = getChildDevices()
	devices.each { child ->
		child.generateActivityFeedsEvent(notificationMessage) //parse received message from parent
	}
}

private def TRACE(message) {
    log.trace message
}