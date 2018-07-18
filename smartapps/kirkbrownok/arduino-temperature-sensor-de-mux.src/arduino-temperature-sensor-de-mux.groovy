/**
 *  Copyright 2015 Kirk Brown
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
 *  temperature sensor De-Mux
 *
 *  Author: Kirk Brown
 *
 *  Date: 2015-10-1
 */
definition(
	name: "Arduino Temperature Sensor De-Mux",
	namespace: "kirkbrownOK",
	author: "Kirk Brown",
	description: "Takes sensor events from arduino and applies them to individual virtual sensors for ease of use in other smartapps.",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
    

) 


preferences {
	section("Arduino Input Dev1: ") {
		input "inDev1", "capability.temperatureMeasurement", multiple: false, required: true
	}
	section("Temperature output Dev1") {
		input "outDev1", "capability.temperatureMeasurement", multiple: false, required: false
	}
    section("Thermostat out for Dev1") {
    	input "thermDev1", "capability.temperatureMeasurement", multiple: false, required: false
    }
	section("Arduino Input Dev2: ") {
		input "inDev2", "capability.temperatureMeasurement", multiple: false, required: false
	}
	section("Temperature output Dev2") {
		input "outDev2", "capability.temperatureMeasurement", multiple: false, required: false
	}
	section("Arduino Input Dev3: ") {
		input "inDev3", "capability.temperatureMeasurement", multiple: false, required: false
	}
	section("Temperature output Dev3") {
		input "outDev3", "capability.temperatureMeasurement", multiple: false, required: false
	}    
}

def installed()
{   
	subscribeToDevices()
    
}

def updated()
{
	unsubscribe()
	subscribeToDevices()
}
def subscribeToDevices() {
	log.debug "Subscribing to devices"
	subscribe(inDev1, "temperature", temperatureHandler1) 
    subscribe(outDev1, "switch.refresh", refreshHandler1)
    if (thermDev1) {
    	log.debug "Subscribed to ${thermDev1}"
        subscribe(thermDev1, "refresh", refreshHandler1)
    }
    log.debug "Subscribed to ${inDev1}"
    if(inDev2) {
    	log.debug "Subscribed to ${inDev2}"
    	subscribe(inDev2, "temperature1", temperatureHandler2)
        subscribe(outDev2, "switch.refresh", refreshHandler2)
    }
    if(inDev3) {
    	log.debug "subscribed to ${inDev3}"
    	subscribe(inDev3, "temperature", temperatureHandler3)
        subscribe(outDev3, "switch.refresh", refreshHandler3)
    }

}
def logHandler(evt) {
	log.debug evt.value
}
def temperatureHandler1(evt) {
	log.debug "Sending $evt.value to ${outDev1}"
	outDev1.setTemperature(evt.value)
    if(thermDev1) {
    	log.debug "Sending $evt.value to ${thermDev1}"
    	thermDev1.setTemperature(evt.value)
    }
}
def temperatureHandler2(evt) {
	log.debug "Sending $evt.value to ${outDev2}"
	outDev2.setTemperature(evt.value)
}
def temperatureHandler3(evt) {
	log.debug "Sending $evt.value to ${outDev3}"
	outDev3.setTemperature(evt.value)
}
def refreshHandler1(evt) {
	outDev1.setTemperature(inDev1.currentValue("temperature"))
    if(thermDev1) {
    	thermDev1.setTemperature(inDev1.currentValue("temperature"))
    }
	log.debug "Sending refresh to ${inDev1}"
    inDev1.refresh()
}
def refreshHandler2(evt) {
	outDev2.setTemperature(inDev2.currentValue("temperature1"))
	log.debug "Sending refresh to ${inDev2}"
    inDev2.refresh()
}
def refreshHandler3(evt) {
	outDev3.setTemperature(inDev3.currentValue("temperature"))
	log.debug "Sending refresh to ${inDev3}"
    inDev3.refresh()
}