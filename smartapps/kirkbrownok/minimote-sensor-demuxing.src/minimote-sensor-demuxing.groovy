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
 *  Minimote Sensor DEMUXING
 *
 *  Author: Kirk Brown
 *
 *  Date: 2015-10-1
 */
definition(
	name: "MiniMote Sensor DEMUXING",
	namespace: "kirkbrownOK",
	author: "Kirk Brown",
	description: "Takes minimote buttons and maps them to individual button tiles",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_outlet@2x.png"
    

) 


preferences {
	section("When this sensor has events: (MUX-ed button input)") {
		input "master", "capability.button", title: "Which minimote?"
	}
	section("Button 1 controls this virtual button:") {
		input "button1", "capability.button", multiple: false, required: false
	}
	section("Button 2 controls this virtual sensor:") {
		input "button2", "capability.button", multiple: false, required: false
	}
	section("Button 3 controls this virtual sensor") {
		input "button3", "capability.button", multiple: false, required: false
	}
	section("Button 4 controls this virtual sensor") {
		input "button4", "capability.button", multiple: false, required: false
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
	subscribe(master, "button", buttonParser) 
    log.debug "Subscribed to ${master}"
    /*
    if(button1) {
    	log.debug "Subscribed to ${button1}"
    	subscribe(button1, "button.pushed", button1pushed)
        subscribe(button1, "button.held", button1held)
        
    }
    if(button2) {
    	log.debug "subscribed to ${button2}"
    	subscribe(button2, "button.pushed", button2pushed)
        subscribe(button2, "button.held", button2held)
    }
    if(button3) {
    	subscribe(button3, "button.pushed", button3pushed)
        subscribe(button3, "button.held", button3held)
    }
    if(button4) {
    	subscribe(button4, "button.pushed", button4pushed)
        subscribe(button4, "button.held", button4held)
    }
    */

}
def logHandler(evt) {
	log.debug evt.value
}

def buttonParser(evt) {
	log.debug "EVT value: ${evt.value} ${evt.data}"
	def buttonNumber = evt.data
    def buttonState = evt.value
    
    log.debug "Button: ${buttonNumber} : ${buttonState}"
    
    if (buttonNumber == '1' && button1) {
    	log.debug "B1: calling ${buttonState}"
    	if(buttonState == 'pushed') {
        	button1.push1()
        } else if (buttonState == 'held') {
        	button1.hold1()
        }        
    }  else if (buttonNumber == '2' && button2) {
    	log.debug "B2: calling ${buttonState}"
    	if(buttonState == 'pushed') {
        	button2.push1()
        } else if (buttonState == 'held') {
        	button2.hold1()
        }
     }
        else if (buttonNumber == '3' && button3) {
    	log.debug "B3: calling ${buttonState}"
    	if(buttonState == 'pushed') {
        	button3.push1()
        } else if (buttonState == 'held') {
        	button3.hold1()
        }        
    } else if (buttonNumber == '4' && button4) {
    	log.debug "B4: calling ${buttonState}"
    	if(buttonState == 'pushed') {
        	button4.push1()
        } else if (buttonState == 'held') {
        	button4.hold1()
        }       
    } 
    
}