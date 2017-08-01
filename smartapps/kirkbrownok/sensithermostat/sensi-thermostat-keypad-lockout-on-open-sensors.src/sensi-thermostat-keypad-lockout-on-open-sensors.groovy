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
 *  engage keypad lockout when windows/doors open
 *
 *  Author: kirk brown
 *  Date: 2017-7-30
 */
definition(
    name: "Sensi Thermostat Keypad Lockout on Open Sensors",
    namespace: "kirkbrownOK/SensiThermostat",
    author: "Kirk Brown",
    description: "When contact sensors open then lock thermostat keypad",
    category: "Convenience",
    iconUrl: "http://i.imgur.com/QVbsCpu.jpg",
	iconX2Url: "http://i.imgur.com/4BfQn6I.jpg",
)

preferences {

  section("Monitor these contact sensors") {
    input "contact", "capability.contactSensor", multiple: true
  }

  section("Disable the keypad on which thermostats?") {
    input "thermostat", "capability.thermostat", description: "Select which thermostat to lock the keypad", required: false, multiple: true
  }
}

def installed() {
  log.trace "installed()"
  subscribe()
}

def updated() {
  log.trace "updated()"
  unsubscribe()
  subscribe()
}

def subscribe() {
  subscribe(contact, "contact.open", doorOpen)
  subscribe(contact, "contact.closed", doorClosed)
}

def doorOpen(evt) {
  log.trace "doorOpen($evt.name: $evt.value)"
  thermostat.setKeypadLockoutOn()
  
}

def doorClosed(evt) {
  log.trace "doorClosed($evt.name: $evt.value)"
  def anySensorStillOpen = false
  contact.each {
  	log.trace "contact: ${it.currentContact}"
    if( it.currentContact != "closed") {
    	anySensorStillOpen = true
    }
  }
  if( !anySensorStillOpen) {
  	thermostat.setKeypadLockoutOff()
  }

}

