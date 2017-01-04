/**
 *  Copyright 2016 Kirk Brown
 * 	This code was started from the ECOBEE Thermostat device type template.
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
 *	Sensi Thermostat
 *
 *	Author: Kirk Brown
 *	Date: 2016-12-26
 */
metadata {
	definition (name: "Sensi Thermostat", namespace: "kirkbrownOK/SensiThermostat", author: "Kirk Brown") {
		
		capability "Thermostat"
		capability "Temperature Measurement"
		capability "Sensor"
		capability "Refresh"
		capability "Relative Humidity Measurement"
		capability "Health Check"

		command "generateEvent"
		command "raiseSetpoint"
		command "lowerSetpoint"
		command "resumeProgram"
		command "switchMode"
		command "switchFanMode"
        command "stopSchedule"
        command "heatUp"
        command "heatDown"
        command "coolUp"
        command "coolDown"
        command "refresh"
        command "poll"

		attribute "thermostatSetpoint", "number"
		attribute "thermostatStatus", "string"
		attribute "maxHeatingSetpoint", "number"
		attribute "minHeatingSetpoint", "number"
		attribute "maxCoolingSetpoint", "number"
		attribute "minCoolingSetpoint", "number"
		attribute "deviceTemperatureUnit", "string"
		attribute "deviceAlive", "enum", ["true", "false"]
        attribute "OperationalStatus", "string"
        attribute "EnvironmentControls", "string"
        attribute "Capabilities", "string"
        
        
        attribute "sensiThermostatMode", "string"
        attribute "sensiBatteryVoltage", "number"
        attribute "thermostatHoldMode", "string"
        attribute "thermostatOperatingMode", "string"
        attribute "thermostatFanState", "string"
	}
/* 
//Original release Tiles
	tiles(scale:2) {
		valueTile("temperature", "device.temperature", width: 2, height: 3) {
			state("temperature", label:'${currentValue}°', unit:"F", icon:"st.Home.home1",
					backgroundColors:[
							// Celsius
							[value: 0, color: "#153591"],
							[value: 7, color: "#1e9cbb"],
							[value: 15, color: "#90d2a7"],
							[value: 23, color: "#44b621"],
							[value: 28, color: "#f1d801"],
							[value: 35, color: "#d04e00"],
							[value: 37, color: "#bc2323"],
							// Fahrenheit
							[value: 40, color: "#153591"],
							[value: 44, color: "#1e9cbb"],
							[value: 59, color: "#90d2a7"],
							[value: 74, color: "#44b621"],
							[value: 84, color: "#f1d801"],
							[value: 95, color: "#d04e00"],
							[value: 96, color: "#bc2323"]
					]
			)
		}
		standardTile("mode", "device.thermostatMode", inactiveLabel: false, decoration: "flat",width: 2, height: 1) {
			state "off", action:"switchMode", nextState: "updating", icon: "st.thermostat.heating-cooling-off"
			state "heat", action:"switchMode",  nextState: "updating", icon: "st.thermostat.heat"
			state "cool", action:"switchMode",  nextState: "updating", icon: "st.thermostat.cool"
			state "auto", action:"switchMode",  nextState: "updating", icon: "st.thermostat.auto"
			state "aux", action:"switchMode", icon: "st.thermostat.emergency-heat"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		standardTile("fanMode", "device.thermostatFanMode", inactiveLabel: false, decoration: "flat",width: 2, height: 1) {
			state "auto", action:"switchFanMode", nextState: "updating", icon: "st.thermostat.fan-auto"
			state "on", action:"switchFanMode", nextState: "updating", icon: "st.thermostat.fan-on"
			state "updating", label:"Working", icon: "st.secondary.secondary"
		}
		standardTile("upButtonControl", "device.thermostatSetpoint", inactiveLabel: false, decoration: "flat",width:2) {
			state "setpoint", action:"raiseSetpoint", icon:"st.thermostat.thermostat-up"
		}
		valueTile("thermostatSetpoint", "device.thermostatSetpoint", width: 2, height: 1, decoration: "flat") {
			state "thermostatSetpoint", label:'${currentValue}'
		}
		valueTile("currentStatus", "device.thermostatStatus", height: 1, width: 6, decoration: "flat") {
			state "thermostatStatus", label:'${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("downButtonControl", "device.thermostatSetpoint", inactiveLabel: false, decoration: "flat",width:2) {
			state "setpoint", action:"lowerSetpoint", icon:"st.thermostat.thermostat-down"
		}
		controlTile("heatSliderControl", "device.heatingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setHeatingSetpoint", action:"thermostat.setHeatingSetpoint", backgroundColor:"#d04e00"
		}
		valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "heat", label:'${currentValue}° heat', unit:"F"
		}
		controlTile("coolSliderControl", "device.coolingSetpoint", "slider", height: 1, width: 2, inactiveLabel: false) {
			state "setCoolingSetpoint", action:"thermostat.setCoolingSetpoint", backgroundColor: "#1e9cbb"
		}
		valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel: false, decoration: "flat") {
			state "cool", label:'${currentValue}° cool', unit:"F", backgroundColor:"#ffffff"
		}
		standardTile("refresh", "device.thermostatMode", inactiveLabel: false, decoration: "flat",width:2) {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("resumeProgram", "device.thermostatHoldMode", inactiveLabel: false, decoration: "flat",width: 2, height: 1) {
			state "off", action:"stopSchedule", nextState: "updating", label:'Schedule Running', icon:"st.Office.office6"
			state "temporary",  action:"resumeProgram", nextState: "updating", label:'Temp Hold', icon:"st.Office.office6"
            state "on",  action:"resumeProgram", nextState: "updating", label:'Schedule is Off', icon:"st.Office.office6"
		}
		valueTile("humidity", "device.humidity", decoration: "flat",width:2, height:1) {
			state "humidity", label:'${currentValue}%'
		}
		main ("temperature")
		details(["temperature", "upButtonControl", "mode", "thermostatSetpoint","fanMode","downButtonControl","resumeProgram", "humidity",  "refresh","heatingSetpoint","coolingSetpoint","currentStatus"])
	}
*/
tiles(scale:2) {
		multiAttributeTile(name:"thermostatFull", type:"thermostat", width:6, height:4) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
                attributeState("default", label:'${currentValue}', unit:"dF", defaultState: true)
			}
			tileAttribute("device.temperature", key: "VALUE_CONTROL") {
				attributeState("VALUE_UP", action: "raiseSetpoint")
				attributeState("VALUE_DOWN", action: "lowerSetpoint")
			}
            tileAttribute("device.humidity", key: "SECONDARY_CONTROL") {
        		attributeState("default", label:'${currentValue}%', unit:"%")
    		}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor:"#44b621")
				attributeState("heating", backgroundColor:"#ffa81e")
				attributeState("cooling", backgroundColor:"#269bd2")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label:'${name}')
				attributeState("heat", label:'${name}')
                attributeState("aux", label: '${name}')
				attributeState("cool", label:'${name}')
				attributeState("auto", label:'${name}')
			}
			tileAttribute("device.heatingSetpoint", key: "HEATING_SETPOINT") {
                attributeState("default", label:'${currentValue}', unit:"dF")
			}
			tileAttribute("device.coolingSetpoint", key: "COOLING_SETPOINT") {
				attributeState("default", label:'${currentValue}', unit:"dF")
			}
		}
        valueTile("temperature", "device.temperature", decoration: "flat") {
            state "temperature", label:'${currentValue}°', unit:"F",
                backgroundColors:[
                
            // Celsius
            	[value: 0, color: "#153591"],
                [value: 7, color: "#1e9cbb"],
                [value: 15, color: "#90d2a7"],
                [value: 23, color: "#44b621"],
                [value: 28, color: "#f1d801"],
                [value: 35, color: "#d04e00"],
                [value: 37, color: "#bc2323"],
                // Fahrenheit
                [value: 40, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 63, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 80, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }
        valueTile("temperatureIcon", "device.temperature", decoration: "flat") {
            state "temperature", label:'${currentValue}°', unit:"F",icon:"st.Home.home1",
            backgroundColors:[
                
            // Celsius
            	[value: 0, color: "#153591"],
                [value: 7, color: "#1e9cbb"],
                [value: 15, color: "#90d2a7"],
                [value: 23, color: "#44b621"],
                [value: 28, color: "#f1d801"],
                [value: 35, color: "#d04e00"],
                [value: 37, color: "#bc2323"],
                // Fahrenheit
                [value: 40, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 63, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 80, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }

        valueTile("heatingSetpoint", "device.heatingSetpoint", inactiveLabel:false) {
            state "default", label:'${currentValue}°', unit:"F",
                backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
        }

        valueTile("coolingSetpoint", "device.coolingSetpoint", inactiveLabel:false) {
            state "default", label:'${currentValue}°', unit:"F",
                backgroundColors:[
                    [value: 31, color: "#153591"],
                    [value: 44, color: "#1e9cbb"],
                    [value: 59, color: "#90d2a7"],
                    [value: 74, color: "#44b621"],
                    [value: 84, color: "#f1d801"],
                    [value: 95, color: "#d04e00"],
                    [value: 96, color: "#bc2323"]
                ]
        }
        
        standardTile("heatUp", "device.heatingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Heating', icon:"st.custom.buttons.add-icon", action:"heatUp"
        }

        standardTile("heatDown", "device.heatingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Heating', icon:"st.custom.buttons.subtract-icon", action:"heatDown"
        }

        standardTile("coolUp", "device.coolingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Cooling', icon:"st.custom.buttons.add-icon", action:"coolUp"
        }

        standardTile("coolDown", "device.coolingSetpoint", inactiveLabel:false, decoration:"flat") {
            state "default", label:'Cooling', icon:"st.custom.buttons.subtract-icon", action:"coolDown"
        }

        standardTile("operatingState", "device.thermostatOperatingState", inactiveLabel:false, decoration:"flat") {
            state "default", label:'[State]'
            state "idle", label:'', icon:"st.thermostat.heating-cooling-off"
            state "heating", label:'', icon:"st.thermostat.heating"
            state "cooling", label:'', icon:"st.thermostat.cooling"
        }

        standardTile("fanState", "device.thermostatFanState", inactiveLabel:false, decoration:"flat") {
            state "default", label:'[Fan State]'
            state "on", label:'', icon:"st.thermostat.fan-on"
            state "off", label:'', icon:"st.thermostat.fan-off"
        }

        standardTile("mode", "device.thermostatMode", inactiveLabel:false) {
            state "default", label:'[Mode]'
            state "off", label:'', icon:"st.thermostat.heating-cooling-off", backgroundColor:"#FFFFFF", action:"thermostat.heat"
            state "heat", label:'', icon:"st.thermostat.heat", backgroundColor:"#FFCC99", action:"thermostat.cool"
            state "aux", label:'', icon:"st.thermostat.heat", backgroundColor:"#FFCC99", action:"thermostat.cool"
            state "cool", label:'', icon:"st.thermostat.cool", backgroundColor:"#99CCFF", action:"thermostat.auto"
            state "auto", label:'', icon:"st.thermostat.auto", backgroundColor:"#99FF99", action:"thermostat.off"
        }

        standardTile("fanMode", "device.thermostatFanMode", inactiveLabel:false) {
            state "default", label:'[Fan Mode]'
            state "auto", label:'', icon:"st.thermostat.fan-auto", backgroundColor:"#A4FCA6", action:"thermostat.fanOn"
            state "on", label:'', icon:"st.thermostat.fan-on", backgroundColor:"#FAFCA4", action:"thermostat.fanAuto"
        }

        standardTile("hold", "device.thermostatHoldMode",decoration:"flat") {
            state "default", label:'[Hold]'
            state "on", label:'Permanent Hold', backgroundColor:"#FFDB94", action:"resumeProgram"
            state "temporary", label: 'Temp Hold', backgroundColor:"#FFDB94", action:"resumeProgram"
            state "off", label:'Sensi Schedule', backgroundColor:"#FFFFFF", action:"stopSchedule"
        }

        standardTile("refresh", "device.thermostatMode", inactiveLabel:false, decoration:"flat",width:6, height:2) {
            state "default", icon:"st.secondary.refresh", action:"refresh.refresh"
        }

        main(["temperatureIcon" ])

        details(["thermostatFull","temperature", "operatingState", "fanState",
        	"mode", "fanMode", "hold",
            "heatingSetpoint", "heatDown", "heatUp",
            "coolingSetpoint", "coolDown", "coolUp",
             
            "refresh"])
    }
	preferences {
		input "holdType", "enum", title: "Hold Type", description: "When changing temperature, use Temporary (Until next transition -> default) or Permanent hold-> TURNS OFF SENSI SCHEDULED CHANGES", required: false, options:["Temporary", "Permanent"]
	}
    

}

void installed() {
    // The device refreshes every 5 minutes by default so if we miss 2 refreshes we can consider it offline
    // Using 12 minutes because in testing, device health team found that there could be "jitter"
    sendEvent(name: "checkInterval", value: 60 * 12, data: [protocol: "cloud"], displayed: false)
    
    
}

// Device Watch will ping the device to proactively determine if the device has gone offline
// If the device was online the last time we refreshed, trigger another refresh as part of the ping.
def ping() {
    def isAlive = device.currentValue("deviceAlive") == "true" ? true : false
    if (isAlive) {
        refresh()
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def refresh() {

	poll()
	log.debug "refresh completed"
}

void poll() {	
	parent.pollChild()
}

def generateEvent(results) {
    def currentFanMode = device.currentValue("thermostatFanMode") == null? "auto" : device.currentValue("thermostatFanMode") 
    
//    results.each {name, value ->
//    	log.debug "$name : $value"
//    }
    
    def events = []
    try{
        if(results?.OperationalStatus?.OperatingMode) { 
            def currentMode = results.OperationalStatus.OperatingMode.toLowerCase()
            //log.debug "R contains opmode: ${results.OperationalStatus.OperatingMode} to $currentMode"
            //sendEvent([name:"sensiThermostatMode",value:currentMode, descriptionText: "${device.name} Sensi mode set to ${currentMode}"])  
            checkSendEvent("sensiThermostatMode",currentMode,"${device.name} Sensi mode set to ${currentMode}")
        }
        if(results?.OperationalStatus?.Running) { 
            def sendValue = results.OperationalStatus.Running.Mode.toLowerCase()
            if ((sendValue == "off") || (sendValue == "null")) {
                sendValue = "idle"
                if(currentFanMode == "auto") {
                    checkSendEvent("thermostatFanState","off")
                    //sendEvent([name:"thermostatFanState", value: "off"])
                }
            } else if (sendValue == "heat") {       	
                sendValue = "heating"
                checkSendEvent("thermostatFanState","on")
                //sendEvent([name:"thermostatFanState", value: "on"])
            } else if (sendValue == "cool") {
                sendValue = "cooling"
                checkSendEvent("thermostatFanState", "on")
                //sendEvent([name:"thermostatFanState", value: "on"])
            }
            log.debug "Running contains opmode: ${sendValue} and fan is ${currentFanMode} "
            checkSendEvent("thermostatOperatingState", sendValue, "${device.name} mode set to ${sendValue}")
            //->sendEvent([name:"thermostatOperatingState",value:sendValue, descriptionText: "${device.name} mode set to ${sendValue}"])  
            //sendEvent([name:"thermostatOperatingMode"
            //sendEvent([name:"thermostatStatus", value: sendValue, descriptionText: "${device.name} mode set to ${sendValue}"])        
        }
        if(results?.OperationalStatus?.Temperature) { 
            def sendValue = location.temperatureScale == "C"? results.OperationalStatus.Temperature.C.toInteger() : results.OperationalStatus.Temperature.F.toInteger()
            //log.debug "Temperature: ${sendValue}"
            checkSendEvent("temperature", sendValue, "${device.name} mode set to ${sendValue}",location.temperatureScale)
            //sendEvent([name:"temperature",value:sendValue, descriptionText: "${device.name} mode set to ${sendValue}",unit:location.temperatureScale ])      	        
        }
        if(results?.OperationalStatus?.Humidity) { 
            def sendValue = results.OperationalStatus.Humidity
            //log.debug "Humidity: ${sendValue}"
            checkSendEvent("humidity", sendValue, "${device.name} humidity ${sendValue}")
            //sendEvent([name:"humidity",value:sendValue, descriptionText: "${device.name} humidity ${sendValue}"])      	        
        } 
        if(results?.OperationalStatus?.BatteryVoltage) { 
            def sendValue = results.OperationalStatus.BatteryVoltage
            //log.debug "Battery Voltage: ${sendValue}"
            checkSendEvent("sensiBatteryVoltage", sendValue, "${device.name} BatteryVoltage ${sendValue}")
            //sendEvent([name:"sensiBatteryVoltage",value:sendValue, descriptionText: "${device.name} BatteryVoltage ${sendValue}"])      	        
        }
        
        if(results?.OperationalStatus) { sendEvent([name: "OperationalStatus", value:results.Capabilities, displayed: false, descriptionText:"For logging"])}
    } catch (e) {
    	log.debug "No OperationalStatus"
    }
    try{
        if(results?.EnvironmentControls?.CoolSetpoint) { 
            def cMode = device.currentValue("thermostatMode") == null ? "off" : device.currentValue("thermostatMode")
            def sensiMode = device.currentValue("sensiThermostatMode") == null ? "off" : device.currentValue("sensiThermostatMode")
            //log.debug "SCT mode ${cMode} sensiMode ${sensiMode}"
            def sendValue = 0
            sendValue = location.temperatureScale == "C"? results?.EnvironmentControls?.CoolSetpoint?.C : results?.EnvironmentControls?.CoolSetpoint?.F
            checkSendEvent("coolingSetpoint",sendValue, "${device.name} Cooling set to ${sendValue}",location.temperatureScale)
            //sendEvent([name:"coolingSetpoint",value:sendValue, descriptionText: "${device.name} Cooling set to ${sendValue}",unit:location.temperatureScale ])
            if( (sensiMode == "autocool") ||(cMode == "cool")) {
                checkSendEvent("thermostatSetpoint", sendValue, "${device.name} Cooling set to ${sendValue}",location.temperatureScale)
                //sendEvent([name:"thermostatSetpoint",value:sendValue, descriptionText: "${device.name} Cooling set to ${sendValue}",unit:location.temperatureScale ]) 
            }

        }
        if(results?.EnvironmentControls?.HeatSetpoint) { 
            def cMode = device.currentValue("thermostatMode") == null ? "off" : device.currentValue("thermostatMode")
            def sensiMode = device.currentValue("sensiThermostatMode") == null ? "off" : device.currentValue("sensiThermostatMode")
            //log.debug "SCT mode ${cMode} sensiMode ${sensiMode}"
            def sendValue = location.temperatureScale == "C"? results.EnvironmentControls.HeatSetpoint.C : results.EnvironmentControls.HeatSetpoint.F
            checkSendEvent("heatingSetpoint", sendValue,"${device.name} Heating set to ${sendValue}", location.temperatureScale)
            //sendEvent([name:"heatingSetpoint",value:sendValue, descriptionText: "${device.name} Heating set to ${sendValue}",unit:location.temperatureScale ])
            if( (sensiMode == "autoheat") || (cMode =="heat")) {
                checkSendEvent("thermostatSetpoint",sendValue,"${device.name} Heating set to ${sendValue}",location.temperatureScale)
                //sendEvent([name:"thermostatSetpoint",value:sendValue, descriptionText: "${device.name} Heating set to ${sendValue}",unit:location.temperatureScale ])
            }

        }
        if(results?.EnvironmentControls?.FanMode) { 
            def sendValue = results.EnvironmentControls.FanMode.toLowerCase()
            //log.debug "Fan Mode: ${sendValue}"
            checkSendEvent("thermostatFanMode",sendValue, "${device.name} Fan Mode ${sendValue}")
            //sendEvent([name:"thermostatFanMode",value:sendValue, descriptionText: "${device.name} Fan Mode ${sendValue}"])      	        
        }
        if(results?.EnvironmentControls?.HoldMode) { 
            //off: means Schedule is Running, Temporary means off schedule until next state on: means Hold indifinitely
            def sendValue = results.EnvironmentControls.HoldMode.toLowerCase()
            //log.debug "Hold Mode: ${sendValue}"
            if(results?.EnvironmentControls?.ScheduleMode?.toLowerCase() == "off") { sendValue = "on" }
            checkSendEvent("thermostatHoldMode", sendValue, "${device.name} Hold Mode ${sendValue}")
            //sendEvent([name:"thermostatHoldMode",value:sendValue, descriptionText: "${device.name} Hold Mode ${sendValue}"])      	        
        }
        if(results?.EnvironmentControls?.SystemMode) { 
            def currentMode = results.EnvironmentControls.SystemMode.toLowerCase()
            //log.debug "System Mode: ${currentMode}"
            checkSendEvent("thermostatMode", currentMode, "${device.name} mode set to ${currentMode}")
            //sendEvent([name:"thermostatMode", value: currentMode, descriptionText: "${device.name} mode set to ${currentMode}"])
        } 
        if(results?.EnvironmentControls) { sendEvent([name: "EnvironmentControls", value:results.Capabilities, displayed: false, descriptionText:"For logging"])}
    
    } catch (e) {
    	log.debug "No EnvironmentControls"
    }    

    try{
        if(results?.Capabilities) {
    	if( state.capabilities != results.Capabilities) {
        	state.capabilities =  results.Capabilities
            if( location.temperatureScale == "C") {
            	sendEvent([name:"maxHeatingSetpoint", value: results.Capabilities?.HeatLimits?.Max?.C, unit: "C"])
                sendEvent([name:"minHeatingSetpoint", value: results.Capabilities?.HeatLimits?.Min?.C, unit: "C"])
                sendEvent([name:"maxCoolingSetpoint", value: results.Capabilities?.CoolLimits?.Max?.C, unit: "C"])
                sendEvent([name:"minCoolingSetpoint", value: results.Capabilities?.CoolLimits?.Min?.C, unit: "C"])               
            } else {
            	sendEvent([name:"maxHeatingSetpoint", value: results.Capabilities?.HeatLimits?.Max?.F, unit: "F"])
                sendEvent([name:"minHeatingSetpoint", value: results.Capabilities?.HeatLimits?.Min?.F, unit: "F"])
                sendEvent([name:"maxCoolingSetpoint", value: results?.Capabilities?.CoolLimits?.Max?.F, unit: "F"])
                sendEvent([name:"minCoolingSetpoint", value: results?.Capabilities?.CoolLimits?.Min?.F, unit: "F"]) 
            }
    		log.debug "Updated capabilities"
        } else {
        	log.info "Capabilities the same"
        }
        
    }
    	if(results?.Capabilities) { sendEvent([name: "Capabilities", value:results.Capabilities, displayed: false, descriptionText:"For logging"])}
    
    } catch (e) {
    
    }
    generateSetpointEvent()
    generateStatusEvent()
    return null
    
}
def parseMode(cMode) {
	//Sensi Thermostat returns Auto as AutoHeat or AutoCool? -> I did this in winter. Not sure how AutoCool looks.

	if(cMode == "autoheat") return "auto"
    if(cMode == "autocool") return "auto"
    return cMode

}
def checkSendEvent(evtName,evtValue,evtDescription=null,evtUnit=null) {
	def checkVal = device.currentValue(evtName)
    def myMap = []
    if (checkVal != evtValue) {
    	if((evtDescription == null) && (evtUnit == null)) {
        	myMap = [name: evtName, value: evtValue]
        } else if (evtUnit == null) {
        	myMap = [name: evtName, value: evtValue, descriptionText: evtDescription]
        } else if (evtDescription == null) {
        	myMap = [name: evtName, value: evtValue, unit: evtUnit]
        } else {
        	myMap = [name: evtName, value: evtValue, descriptionText: evtDescription, unit: evtUnit]
        }
        //log.debug "Sending Check Event: ${myMap}"
    	sendEvent(myMap)
    } else {
    	//log.debug "${evtName}:${evtValue} is the same"
    }
}

void heatUp() {
	log.debug "Heat Up"
	def mode = device.currentValue("thermostatMode")
	if (mode == "off" ) {
		heat()
	}
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	
    def targetvalue = location.temperatureScale == "F"? heatingSetpoint + 1 : heatingSetpoint + 0.5

    sendEvent(name:"heatingSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)

    runIn(5, setDataHeatingSetpoint,[data: [value: targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
}

void heatDown() {
	log.debug "Heat Down"
	def mode = device.currentValue("thermostatMode")
	if (mode == "off" ) {
		//heat()
	}
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	
    def targetvalue = location.temperatureScale == "F"? heatingSetpoint - 1 : heatingSetpoint - 0.5

    sendEvent(name:"heatingSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)

    runIn(5, setDataHeatingSetpoint,[data: [value: targetvalue], overwrite: true]) //when user click button this runIn will be overwrite

}

void coolUp() {
	log.debug "Cool Up"
	def mode = device.currentValue("thermostatMode")
	if (mode == "off" ) {
		//cool()
	}
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	
    def targetvalue = location.temperatureScale == "F"? coolingSetpoint + 1 : heatingSetpoint + 0.5

    sendEvent(name:"coolingSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)

    runIn(5, setDataCoolingSetpoint,[data: [value: targetvalue], overwrite: true]) //when user click button this runIn will be overwrite

}

void coolDown() {
	log.debug "Cool Down"
	def mode = device.currentValue("thermostatMode")
	if (mode == "off" ) {
		cool()
	}
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	
    def targetvalue = location.temperatureScale == "F"? coolingSetpoint - 1 : heatingSetpoint - 0.5

    sendEvent(name:"coolingSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)

    runIn(5, setDataCoolingSetpoint,[data: [value: targetvalue], overwrite: true]) //when user click button this runIn will be overwrite

}
void setDataHeatingSetpoint(setpoint) {
	log.debug "Set heating setpoint $setpoint.value"
	setHeatingSetpoint(setpoint.value)
    //runIn(5, "poll")
}
void setDataCoolingSetpoint(setpoint) {
	log.debug "Set Cooling setpoint $setpoint.value"
	setCoolingSetpoint(setpoint.value)
    //runIn(5, "poll")
}
void setHeatingSetpoint(setpoint) {
	//log.debug "***heating setpoint $setpoint"
    def cmdString = "set"
	def heatingSetpoint = setpoint
    
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def deviceId = device.deviceNetworkId
	def maxHeatingSetpoint = device.currentValue("maxHeatingSetpoint")
	def minHeatingSetpoint = device.currentValue("minHeatingSetpoint")
    def thermostatMode = device.currentValue("thermostatMode")

	//enforce limits of heatingSetpoint
	if (heatingSetpoint > maxHeatingSetpoint) {
		heatingSetpoint = maxHeatingSetpoint
	} else if (heatingSetpoint < minHeatingSetpoint) {
		heatingSetpoint = minHeatingSetpoint
	}

	//enforce limits of heatingSetpoint vs coolingSetpoint
	if (heatingSetpoint >= coolingSetpoint) {
		coolingSetpoint = heatingSetpoint
	}
    //"on" means the schedule will not run
    //"temporary" means do nothing special"
    //"off" means do nothing special
	def currentHoldMode = getDataByName("thermostatHoldMode")
    def desiredHoldType = holdType == null ? "temporary" : holdType
    //log.debug "holdType is: ${holdType} des Hold type is: ${desiredHoldType}"
    if( (desiredHoldType == "Permanent") && (currentHoldMode != "on")) {
    	parent.setStringCmd(deviceId, "SetScheduleMode", "Off")
        sendEvent(name:"thermostatHoldMode", value: "on")
    } else {
    	sendEvent(name:"thermostatHoldMode", value: "temporary")
    }
    
    
    if ( thermostatMode == "auto") {
    	cmdString = "SetAutoHeat" 
        //log.debug "Is AUTHO heat ${cmdString}"
    } else if( (thermostatMode == "heat") || (thermostatMode == "aux") ) { 
    	cmdString = "SetHeat" 
        //log.debug "Is Reg Heat ${cmdString}"
    }
     //log.debug "Sending heatingSetpoint: ${heatingSetpoint} mode: ${thermostatMode} string: ${cmdString}"  
    if (parent.setTempCmd(deviceId, cmdString, heatingSetpoint)) {
		sendEvent("name":"heatingSetpoint", "value":heatingSetpoint, "unit":location.temperatureScale)
		
        
        runIn(5,"poll",[overwrite: true])
        //log.debug "Done setHeatingSetpoint: ${heatingSetpoint}"

	} else {
		log.error "Error setHeatingSetpoint(setpoint)"
	}
    
}

void setCoolingSetpoint(setpoint) {
	log.debug "***cooling setpoint $setpoint"
    def cmdString = "set"
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = setpoint
	def deviceId = device.deviceNetworkId
	def maxCoolingSetpoint = device.currentValue("maxCoolingSetpoint")
	def minCoolingSetpoint = device.currentValue("minCoolingSetpoint")
	def thermostatMode = device.currentValue("thermostatMode")
	if (coolingSetpoint > maxCoolingSetpoint) {
		coolingSetpoint = maxCoolingSetpoint
	} else if (coolingSetpoint < minCoolingSetpoint) {
		coolingSetpoint = minCoolingSetpoint
	}

	//enforce limits of heatingSetpoint vs coolingSetpoint
	if (heatingSetpoint >= coolingSetpoint) {
		heatingSetpoint = coolingSetpoint
	}
    //"on" means the schedule will not run
    //"temporary" means do nothing special"
    //"off" means do nothing special
	def currentHoldMode = getDataByName("thermostatHoldMode")
    def desiredHoldType = holdType == null ? "temporary" : holdType
    //log.debug "holdType is: ${holdType} des Hold type is: ${desiredHoldType}"
    if( (desiredHoldType == "Permanent") && (currentHoldMode != "on")) {
    	parent.setStringCmd(deviceId, "SetScheduleMode", "Off")
        sendEvent(name:"thermostatHoldMode", value: "on")
    } else {
    	sendEvent(name:"thermostatHoldMode", value: "temporary")
    }
	def coolingValue = location.temperatureScale == "C"? convertCtoF(coolingSetpoint) : coolingSetpoint
	def heatingValue = location.temperatureScale == "C"? convertCtoF(heatingSetpoint) : heatingSetpoint
	if ( thermostatMode == "auto") {
    	cmdString = "SetAutoCool" 
        //log.debug "Set Auto Cool"
    } else if( thermostatMode == "cool" ) { 
    	cmdString = "SetCool" 
        //log.debug "set Cool"
    }
	def sendHoldType = getDataByName("thermostatHoldMode")
	//log.debug "Sending CoolingSetpoint: ${coolingSetpoint} mode: ${thermostatMode} string: ${cmdString}"
	if (parent.setTempCmd(deviceId, cmdString, coolingSetpoint)) {
		sendEvent("name":"coolingSetpoint", "value":coolingSetpoint, "unit":location.temperatureScale)
		//log.debug "Done setCoolingSetpoint: ${coolingSetpoint}"
		runIn(5,"poll",[overwrite: true])
        log.debug "Past Runing"
	} else {
		log.error "Error setCoolingSetpoint(setpoint)"
	}
}

void resumeProgram() {
	log.debug "resumeProgram() is called"
	sendEvent("name":"thermostatStatus", "value":"resuming schedule", "description":statusText, displayed: false)
	def deviceId = device.deviceNetworkId
    def currentMode = getDataByName("thermostatHoldMode")
    if (currentMode == "temporary") {
    	parent.setStringCmd(deviceId, "SetHoldMode", "Off")       
    }
	if (parent.setStringCmd(deviceId,"SetScheduleMode","On")) {
		sendEvent("name":"thermostatStatus", "value":"setpoint is updating", "description":statusText, displayed: false)
		runIn(5, "poll")
		//log.debug "resumeProgram() is done"
	} else {
		sendEvent("name":"thermostatStatus", "value":"failed resume click refresh", "description":statusText, displayed: false)
		log.error "Error resumeProgram() check parent.resumeProgram(deviceId)"
	}

}
void stopSchedule() {
	//log.debug "stopSchedule() is called"
	sendEvent("name":"thermostatStatus", "value":"stopping schedule", "description":statusText, displayed: false)
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd(deviceId,"SetScheduleMode","Off")) {
		sendEvent("name":"thermostatStatus", "value":"setpoint is updating", "description":statusText, displayed: false)
        sendEvent(name:"thermostatHoldMode", value: "on")
		runIn(5, "poll")
	} else {
		sendEvent("name":"thermostatStatus", "value":"failed resume click refresh", "description":statusText, displayed: false)
		log.error "Error resumeProgram() check parent.resumeProgram(deviceId)"
	}

}
def modes() {
	
	if (state.modes) {
		//log.debug "Modes = ${state.modes}"
		return state.modes
	}
	else {
		state.modes = parent.availableModes(this)
		log.debug "Modes = ${state.modes}"
		return state.modes
	}
}

def fanModes() {
	["on", "auto"]
}

def switchMode() {
	//log.debug "in switchMode"
	def currentMode = device.currentState("thermostatMode")?.value
	def lastTriedMode = state.lastTriedMode ?: currentMode ?: "off"
	def modeOrder = modes()
	def next = { modeOrder[modeOrder.indexOf(it) + 1] ?: modeOrder[0] }
	def nextMode = next(lastTriedMode)
	switchToMode(nextMode)
}

def switchToMode(nextMode) {
	//log.debug "In switchToMode = ${nextMode}"
	if (nextMode in modes()) {
    	nextMode = nextMode.toLowerCase()
		state.lastTriedMode = nextMode
		"$nextMode"()
	} else {
		log.debug("no mode method '$nextMode'")
	}
}

def switchFanMode() {
	def currentFanMode = device.currentState("thermostatFanMode")?.value
	//log.debug "switching fan from current mode: $currentFanMode"
	def returnCommand

	switch (currentFanMode) {
		case "on":
			returnCommand = switchToFanMode("auto")
			break
		case "auto":
			returnCommand = switchToFanMode("on")
			break

	}
	if(!currentFanMode) { returnCommand = switchToFanMode("auto") }
	returnCommand
}

def switchToFanMode(nextMode) {
	//log.debug "switching to fan mode: $nextMode"
	def returnCommand

	if(nextMode == "auto") {
		returnCommand = fanAuto()

	} else if(nextMode == "on") {
		
		returnCommand = fanOn()
		
	}

	returnCommand
}

def getDataByName(String name) {
	state[name] ?: device.getDataValue(name)
}

def setThermostatMode(String mode) {
	//log.debug "setThermostatMode($mode)"
	mode = mode.toLowerCase()
	switchToMode(mode)
}

def setThermostatFanMode(String mode) {
	//log.debug "setThermostatFanMode($mode)"
	mode = mode.toLowerCase()
	switchToFanMode(mode)
}

def generateModeEvent(mode) {
	sendEvent(name: "thermostatMode", value: mode, descriptionText: "$device.displayName is in ${mode} mode", displayed: true)
}

def generateFanModeEvent(fanMode) {
	sendEvent(name: "thermostatFanMode", value: fanMode, descriptionText: "$device.displayName fan is in ${fanMode} mode", displayed: true)
}

def generateOperatingStateEvent(operatingState) {
	sendEvent(name: "thermostatOperatingState", value: operatingState, descriptionText: "$device.displayName is ${operatingState}", displayed: true)
}

def off() {
	//log.debug "off"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Off")) {
		generateModeEvent("off")
        runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def heat() {
	//log.debug "heat"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Heat")){
		generateModeEvent("heat")
        runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def emergencyHeat() {
	auxHeatOnly()
}
def aux() {
	auxHeatOnly()
}
def auxHeatOnly() {
	//log.debug "auxHeatOnly"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Aux")) {
		generateModeEvent("aux")
        runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def cool() {
	//log.debug "cool"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Cool")){
		generateModeEvent("cool")
        runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
	generateSetpointEvent()
	generateStatusEvent()
}

def auto() {
	//log.debug "auto"
	def deviceId = device.deviceNetworkId
	if (parent.setStringCmd (deviceId,"SetSystemMode","Auto")) {
		generateModeEvent("auto")
        runIn(5, "poll")
    }
	else {
		log.debug "Error setting new mode."
		def currentMode = device.currentState("thermostatMode")?.value
		generateModeEvent(currentMode) // reset the tile back
	}
    
	generateSetpointEvent()
	generateStatusEvent()
}

def fanOn() {
	//log.debug "fanOn"
	def cmdVal = "On"
	def deviceId = device.deviceNetworkId
	def cmdString = "SetFanMode"
	if (parent.setStringCmd( deviceId,cmdString,cmdVal)) {
		sendEvent([name: "thermostatFanMode", value: "on", descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])
        runIn(5, "poll")
	} else {
		log.debug "Error setting new mode."
		def currentFanMode = device.currentState("thermostatFanMode")?.value
		sendEvent([name: "thermostatFanMode", value: currentFanMode, descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])
	}

}

def fanAuto() {
	//log.debug "fanAuto"
	//log.debug "fanOn"
	def cmdVal = "Auto"
	def deviceId = device.deviceNetworkId
	def cmdString = "SetFanMode"
	if (parent.setStringCmd(deviceId,cmdString,cmdVal)) {
		sendEvent([name: "thermostatFanMode", value: "auto", descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])
        runIn(5, "poll")
	} else {
		log.debug "Error setting new mode."
		def currentFanMode = device.currentState("thermostatFanMode")?.value
		sendEvent([name: "thermostatFanMode", value: currentFanMode, descriptionText: "${device.name} sent ${cmdString} ${cmdVal}"])
	}

    
}

def generateSetpointEvent() {
	//log.debug "Generate SetPoint Event"

	def mode = device.currentValue("thermostatMode")
    def sensiMode = device.currentValue("sensiThermostatMode")
    def operatingState = device.currentValue("thermostatOperatingState")

	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def maxHeatingSetpoint = device.currentValue("maxHeatingSetpoint")
	def maxCoolingSetpoint = device.currentValue("maxCoolingSetpoint")
	def minHeatingSetpoint = device.currentValue("minHeatingSetpoint")
	def minCoolingSetpoint = device.currentValue("minCoolingSetpoint")

	if(location.temperatureScale == "C") {
		maxHeatingSetpoint = maxHeatingSetpoint > 40 ? roundC(convertFtoC(maxHeatingSetpoint)) : roundC(maxHeatingSetpoint)
		maxCoolingSetpoint = maxCoolingSetpoint > 40 ? roundC(convertFtoC(maxCoolingSetpoint)) : roundC(maxCoolingSetpoint)
		minHeatingSetpoint = minHeatingSetpoint > 40 ? roundC(convertFtoC(minHeatingSetpoint)) : roundC(minHeatingSetpoint)
		minCoolingSetpoint = minCoolingSetpoint > 40 ? roundC(convertFtoC(minCoolingSetpoint)) : roundC(minCoolingSetpoint)
		heatingSetpoint = heatingSetpoint > 40 ? roundC(convertFtoC(heatingSetpoint)) : roundC(heatingSetpoint)
		coolingSetpoint = coolingSetpoint > 40 ? roundC(convertFtoC(coolingSetpoint)) : roundC(coolingSetpoint)
	} else {
		maxHeatingSetpoint = maxHeatingSetpoint < 40 ? roundC(convertCtoF(maxHeatingSetpoint)) : maxHeatingSetpoint
		maxCoolingSetpoint = maxCoolingSetpoint < 40 ? roundC(convertCtoF(maxCoolingSetpoint)) : maxCoolingSetpoint
		minHeatingSetpoint = minHeatingSetpoint < 40 ? roundC(convertCtoF(minHeatingSetpoint)) : minHeatingSetpoint
		minCoolingSetpoint = minCoolingSetpoint < 40 ? roundC(convertCtoF(minCoolingSetpoint)) : minCoolingSetpoint
		heatingSetpoint = heatingSetpoint < 40 ? roundC(convertCtoF(heatingSetpoint)) : heatingSetpoint
		coolingSetpoint = coolingSetpoint < 40 ? roundC(convertCtoF(coolingSetpoint)) : coolingSetpoint
	}

	log.debug "Current Mode = ${mode} sensiMode: ${sensiMode}"
	log.debug "Heating Setpoint = ${heatingSetpoint}"
	log.debug "Cooling Setpoint = ${coolingSetpoint}"

	sendEvent("name":"maxHeatingSetpoint", "value":maxHeatingSetpoint, "unit":location.temperatureScale)
	sendEvent("name":"maxCoolingSetpoint", "value":maxCoolingSetpoint, "unit":location.temperatureScale)
	sendEvent("name":"minHeatingSetpoint", "value":minHeatingSetpoint, "unit":location.temperatureScale)
	sendEvent("name":"minCoolingSetpoint", "value":minCoolingSetpoint, "unit":location.temperatureScale)
	sendEvent("name":"heatingSetpoint", "value":heatingSetpoint, "unit":location.temperatureScale)
	sendEvent("name":"coolingSetpoint", "value":coolingSetpoint, "unit":location.temperatureScale)

	if (mode == "heat") {
		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint, "unit":location.temperatureScale)
	}
	else if (mode == "cool") {
		sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint, "unit":location.temperatureScale)
	} else if (mode == "auto") {
    	if(sensiMode =="AutoHeat") {
			sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint, "unit":location.temperatureScale)
        } else if(sensiMode =="AutoCool") {
        	sendEvent("name":"thermostatSetpoint", "value":coolingSetpoint, "unit":location.temperatureScale)
        }
	} else if (mode == "off") {
		sendEvent("name":"thermostatSetpoint", "value":"Off")
	} else if (mode == "aux") {
		sendEvent("name":"thermostatSetpoint", "value":heatingSetpoint, "unit":location.temperatureScale)
	}
}

void raiseSetpoint() {
	def mode = device.currentValue("thermostatMode")
    
	def targetvalue
	def maxHeatingSetpoint = device.currentValue("maxHeatingSetpoint")
	def maxCoolingSetpoint = device.currentValue("maxCoolingSetpoint")
	def sensiMode = device.currentValue("sensiThermostatMode")
	if (mode == "off" ) {
		log.warn "this mode: $mode does not allow raiseSetpoint"
	} else {
    	if(mode == "auto") {
        	//The Sensi thermostat shares if it is in Auto Heating or Auto Cooling, so a raise should be able to go off the of operating mode ? 
        	if (sensiMode =="autoheat") { mode = "heat" }
            else if (sensiMode =="autocool") { mode = "cool" }
        }

		def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")
		def thermostatSetpoint = device.currentValue("thermostatSetpoint")

		if (location.temperatureScale == "C") {
			maxHeatingSetpoint = maxHeatingSetpoint > 40 ? convertFtoC(maxHeatingSetpoint) : maxHeatingSetpoint
			maxCoolingSetpoint = maxCoolingSetpoint > 40 ? convertFtoC(maxCoolingSetpoint) : maxCoolingSetpoint
			heatingSetpoint = heatingSetpoint > 40 ? convertFtoC(heatingSetpoint) : heatingSetpoint
			coolingSetpoint = coolingSetpoint > 40 ? convertFtoC(coolingSetpoint) : coolingSetpoint
			thermostatSetpoint = thermostatSetpoint > 40 ? convertFtoC(thermostatSetpoint) : thermostatSetpoint
		} else {
			maxHeatingSetpoint = maxHeatingSetpoint < 40 ? convertCtoF(maxHeatingSetpoint) : maxHeatingSetpoint
			maxCoolingSetpoint = maxCoolingSetpoint < 40 ? convertCtoF(maxCoolingSetpoint) : maxCoolingSetpoint
			heatingSetpoint = heatingSetpoint < 40 ? convertCtoF(heatingSetpoint) : heatingSetpoint
			coolingSetpoint = coolingSetpoint < 40 ? convertCtoF(coolingSetpoint) : coolingSetpoint
			thermostatSetpoint = thermostatSetpoint < 40 ? convertCtoF(thermostatSetpoint) : thermostatSetpoint
		}

		//log.debug "raiseSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}"

		targetvalue = thermostatSetpoint ? thermostatSetpoint : 0
		targetvalue = location.temperatureScale == "F"? targetvalue + 1 : targetvalue + 0.5

		if ((mode == "heat" || mode == "aux") && targetvalue > maxHeatingSetpoint) {
			targetvalue = maxHeatingSetpoint
		} else if (mode == "cool" && targetvalue > maxCoolingSetpoint) {
			targetvalue = maxCoolingSetpoint
		}

		sendEvent("name":"thermostatSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)
		log.info "In mode $mode raiseSetpoint() to $targetvalue"

		runIn(3, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
	}
}

//called by tile when user hit raise temperature button on UI
void lowerSetpoint() {
	def mode = device.currentValue("thermostatMode")
	def targetvalue
	def minHeatingSetpoint = device.currentValue("minHeatingSetpoint")
	def minCoolingSetpoint = device.currentValue("minCoolingSetpoint")
    def sensiMode = device.currentValue("sensiThermostatMode")
	if (mode == "off" ) {
		log.warn "this mode: $mode does not allow lowerSetpoint()"
	} else {
    	if(mode == "auto") {
        	//The Sensi thermostat shares if it is in Auto Heating or Auto Cooling, so a raise should be able to go off the of operating mode ? 
        	if (sensiMode =="autoheat") { mode = "heat" }
            else if (sensiMode =="autocool") { mode = "cool" }
        }
		def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")
		def thermostatSetpoint = device.currentValue("thermostatSetpoint")

		if (location.temperatureScale == "C") {
			minHeatingSetpoint = minHeatingSetpoint > 40 ? convertFtoC(minHeatingSetpoint) : minHeatingSetpoint
			minCoolingSetpoint = minCoolingSetpoint > 40 ? convertFtoC(minCoolingSetpoint) : minCoolingSetpoint
			heatingSetpoint = heatingSetpoint > 40 ? convertFtoC(heatingSetpoint) : heatingSetpoint
			coolingSetpoint = coolingSetpoint > 40 ? convertFtoC(coolingSetpoint) : coolingSetpoint
			thermostatSetpoint = thermostatSetpoint > 40 ? convertFtoC(thermostatSetpoint) : thermostatSetpoint
		} else {
			minHeatingSetpoint = minHeatingSetpoint < 40 ? convertCtoF(minHeatingSetpoint) : minHeatingSetpoint
			minCoolingSetpoint = minCoolingSetpoint < 40 ? convertCtoF(minCoolingSetpoint) : minCoolingSetpoint
			heatingSetpoint = heatingSetpoint < 40 ? convertCtoF(heatingSetpoint) : heatingSetpoint
			coolingSetpoint = coolingSetpoint < 40 ? convertCtoF(coolingSetpoint) : coolingSetpoint
			thermostatSetpoint = thermostatSetpoint < 40 ? convertCtoF(thermostatSetpoint) : thermostatSetpoint
		}
		//log.debug "lowerSetpoint() mode = ${mode}, heatingSetpoint: ${heatingSetpoint}, coolingSetpoint:${coolingSetpoint}, thermostatSetpoint:${thermostatSetpoint}"

		targetvalue = thermostatSetpoint ? thermostatSetpoint : 0
		targetvalue = location.temperatureScale == "F"? targetvalue - 1 : targetvalue - 0.5

		if ((mode == "heat" || mode == "aux") && targetvalue < minHeatingSetpoint) {
			targetvalue = minHeatingSetpoint
		} else if (mode == "cool" && targetvalue < minCoolingSetpoint) {
			targetvalue = minCoolingSetpoint
		}

		sendEvent("name":"thermostatSetpoint", "value":targetvalue, "unit":location.temperatureScale, displayed: false)
		log.info "In mode $mode lowerSetpoint() to $targetvalue"

		runIn(3, "alterSetpoint", [data: [value:targetvalue], overwrite: true]) //when user click button this runIn will be overwrite
	}
}

//called by raiseSetpoint() and lowerSetpoint()
void alterSetpoint(temp) {
	def deviceId = device.deviceNetworkId
	def mode = device.currentValue("thermostatMode")
	def sensiMode = device.currentValue("sensiThermostatMode")
    //"on" means the schedule will not run
    //"temporary" means do nothing special"
    //"off" means do nothing special
	def currentHoldMode = getDataByName("thermostatHoldMode")
    def desiredHoldType = holdType == null ? "temporary" : holdType
   // log.debug "holdType is: ${holdType} des Hold type is: ${desiredHoldType}"
    if( (desiredHoldType == "Permanent") && (currentHoldMode != "on")) {
    	parent.setStringCmd(deviceId, "SetScheduleMode", "Off")
        sendEvent(name:"thermostatHoldMode", value: "on")
    } else {
    	sendEvent(name:"thermostatHoldMode", value: "temporary")
    }
	if (mode == "off" ) {
		log.warn "this mode: $mode does not allow alterSetpoint"
	} else {
		def heatingSetpoint = device.currentValue("heatingSetpoint")
		def coolingSetpoint = device.currentValue("coolingSetpoint")


		def targetHeatingSetpoint
		def targetCoolingSetpoint
		def thermostatSetpoint
		def modeNum = 0
		def temperatureScaleHasChanged = false

		if (location.temperatureScale == "C") {
			if ( heatingSetpoint > 40.0 || coolingSetpoint > 40.0 ) {
				temperatureScaleHasChanged = true
			}
		} else {
			if ( heatingSetpoint < 40.0 || coolingSetpoint < 40.0 ) {
				temperatureScaleHasChanged = true
			}
		}
		def cmdString 
		//step1: check thermostatMode, enforce limits before sending request to cloud
		if ((mode == "heat") || (mode == "aux") || (sensiMode == "autoheat")){
        	modeNum = 1
        	if((mode == "heat") || (mode == "aux")) { cmdString = "SetHeat"}
            else if(sensiMode == "autoheat") { cmdString = "SetAutoHeat" }
			if (temp.value > coolingSetpoint){
				targetHeatingSetpoint = temp.value
				//targetCoolingSetpoint = temp.value
			} else {
				targetHeatingSetpoint = temp.value
				//targetCoolingSetpoint = coolingSetpoint
			}
		} else if ((mode == "cool") || (sensiMode == "autocool") ) {
        	modeNum = 2
        	if(mode == "cool") { cmdString = "SetCool" }
            else if (sensiMode == "autocool") { cmdString = "SetAutoCool" }
			//enforce limits before sending request to cloud
			if (temp.value < heatingSetpoint){
				//targetHeatingSetpoint = temp.value
				targetCoolingSetpoint = temp.value
			} else {
				//targetHeatingSetpoint = heatingSetpoint
				targetCoolingSetpoint = temp.value
			}
		}

	//	log.debug "alterSetpoint >> in mode ${mode} trying to change heatingSetpoint to $targetHeatingSetpoint " +
	//			"coolingSetpoint to $targetCoolingSetpoint with holdType : ${holdType}"

		def sendHoldType = getDataByName("thermostatHoldMode")

		def coolingValue = location.temperatureScale == "C"? convertCtoF(targetCoolingSetpoint) : targetCoolingSetpoint
		def heatingValue = location.temperatureScale == "C"? convertCtoF(targetHeatingSetpoint) : targetHeatingSetpoint

		if (parent.setTempCmd(deviceId,cmdString, temp.value)) {
			sendEvent("name": "thermostatSetpoint", "value": temp.value, displayed: false)
            if(modeNum == 1) { sendEvent("name": "heatingSetpoint", "value": targetHeatingSetpoint, "unit": location.temperatureScale) }
            else if (modeNum == 2) { sendEvent("name": "coolingSetpoint", "value": targetCoolingSetpoint, "unit": location.temperatureScale) }
			log.debug "alterSetpoint in mode $mode succeed change setpoint to= ${temp.value}"
		} else {
			log.error "Error alterSetpoint()"
			if (mode == "heat" || mode == "aux" || sensiMode == "autoheat"){
				sendEvent("name": "thermostatSetpoint", "value": heatingSetpoint.toString(), displayed: false)
			} else if (mode == "cool" || sensiMode == "autocool") {
				sendEvent("name": "thermostatSetpoint", "value": coolingSetpoint.toString(), displayed: false)
			}
		}
    runIn(5, "poll")    
	
	if ( temperatureScaleHasChanged )
		generateSetpointEvent()
		generateStatusEvent()
	}
}

def generateStatusEvent() {
	def mode = device.currentValue("thermostatMode")
    def operatingMode = device.currentValue("thermostatOperatingState")
	def heatingSetpoint = device.currentValue("heatingSetpoint")
	def coolingSetpoint = device.currentValue("coolingSetpoint")
	def temperature = device.currentValue("temperature")
	def statusText

//	log.debug "Generate Status Event for Mode = ${mode} in state: ${operatingMode}"
//	log.debug "Temperature = ${temperature}"
//	log.debug "Heating set point = ${heatingSetpoint}"
//	log.debug "Cooling set point = ${coolingSetpoint}"
//	log.debug "HVAC Mode = ${mode}"
	
    if(operatingMode == "heat") {
    	statusText = "Heating to ${heatingSetpoint} ${location.temperatureScale}"
    }
    else if (operatingMode == "cool") {
    	statusText = "Cooling to ${coolingSetpoint} ${location.temperatureScale}"
    }
    else if (operatingMode == "aux") {
		statusText = "Emergency Heat"
	} 
    else if (operatingMode == "off") {
    	statusText = "Idle"
    }else {
		statusText = "?"
	}

//	log.debug "Generate Status Event = ${statusText}"
	sendEvent("name":"thermostatStatus", "value":statusText, "description":statusText, displayed: true)
}

def generateActivityFeedsEvent(notificationMessage) {
	sendEvent(name: "notificationMessage", value: "$device.displayName $notificationMessage", descriptionText: "$device.displayName $notificationMessage", displayed: true)
}

def roundC (tempC) {
	return (Math.round(tempC.toDouble() * 2))/2
}

def convertFtoC (tempF) {
	return ((Math.round(((tempF - 32)*(5/9)) * 2))/2).toDouble()
}

def convertCtoF (tempC) {
	return (Math.round(tempC * (9/5)) + 32).toInteger()
}