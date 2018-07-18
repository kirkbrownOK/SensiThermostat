/**
 *  Camera Status Monitor
 *
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
 */
definition(
    name: "Camera Status Monitor",
    namespace: "kirkbrownOK",
    author: "Kirk Brown",
    description: "This app monitors the status of a camera device. When the camera device reports the DVR is no longer responsive, it will send a MAKER command to IFTTT to restart the WEMO switch the device is connected to. ",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Which Camera Device:") {
		input "camera", "capability.switch", multiple: false, required: true
    }
    section("Maker Key:") {
    	input "maker_key", "string", defaultValue: "sG20x6jo8kFWwxUsBVqVX", required: true
	
	}
    section("Which Wemo outlet:") {
		input "cameraOutlet", "capability.switch", multiple: false, required: true
    }
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

def initialize() {
	subscribe(camera, "switch.off", resetOutlet)
    subscribe(cameraOutlet, "switch.off", scheduleOn)
    
	// TODO: subscribe to attributes, devices, locations, etc.
}
def resetOutlet(evt) {
	log.debug "Resetting Outlet"
    cameraOutlet.reset()
    camera.beenReset()
}

def scheduleOn(evt) {
	log.debug "outlet is off scheduling on"
    cameraOutlet.on()
    log.debug "Outlet turned on"
    if (cameraOutlet.currentValue('switch') == "off") {
    
    	runIn(60,scheduleOn)
    }
}
/*
def resetOutlet(evt) {
	log.debug "Resetting Outlet"
    def params = [
        uri: "https://maker.ifttt.com",
        path: "/trigger/reset/with/key/${maker_key}"
    ]
	log.debug "${params}"
    try {
        httpGet(params) { resp ->
            resp.headers.each {
               log.debug "${it.name} : ${it.value}"
            }
            log.debug "response contentType: ${resp.contentType}"
            log.debug "response data: ${resp.data}"
        }
    } catch (e) {
        log.error "something went wrong: $e"
        return
    }	
    camera.beenReset()
}
*/
//https://maker.ifttt.com/trigger/{event}/with/key/sG20x6jo8kFWwxUsBVqVX
// TODO: implement event handlers