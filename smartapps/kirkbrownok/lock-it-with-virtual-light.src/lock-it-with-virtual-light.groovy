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
 *  Lock It When I Leave
 *
 *  Author: Kirk Brown
 *  Date: 2016-11-06
 */

definition(
    name: "Lock It With Virtual Light",
    namespace: "kirkbrownOK",
    author: "Kirk Brown",
    description: "Locks a deadbolt or lever lock when a Light turns on and Unlocks when light turns off.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	section("Match with this Virtual Light") {
		input "light1", "capability.switch", title: "Which Virtual Light?"
        input "lockedOn", "enum", title: "Should the lock be locked when the light is ON or OFF?", options: ["ON", "OFF"]
	}
	section("Lock the lock...") {
		input "lock1","capability.lock", multiple: true
		input "unlock", "enum", title: "Allow the Tile to unlock the door?", options: ["YES","NO"]
	}
}

def installed()
{
	initialize()
}

def updated()
{
	unsubscribe()
	initialize()
}
def initialize() 
{
	
	subscribe(light1, "switch.on", lightOn)
    if( unlock == "YES") {
    	subscribe(light1, "switch.off", lightOff)
    }
    subscribe(lock1, "lock.locked", doorLocked)
    subscribe(lock1, "lock.unlocked", doorUnlocked)
    if(lock1.currentLock == "locked") {
    	
    	//send lock sync to light
        if(lockedOn == "ON") {
        	TRACE("LOCKED SYNCON")
        	light1.syncON()
        } else {
        	TRACE("LOCKED SYNCOFF")
        	light1.syncOFF()
        }
    } else if(lock1.currentLock == "unlocked") {
    	//send unlock sync to light
        if(lockedOn == "ON") {
        	TRACE("UNLOCKED SYNCOFF")
        	light1.syncOFF()
        } else {
        	TRACE("UNLOCKED SYNCON")
        	light1.syncON()
        }
    }
}
def lightOn(evt)
{
	log.info "$evt.name $evt.value $evt.descriptionText"
	if(descriptionText == "Virtual") {
    	TRACE("light ON Virtual")
    	//The light on event is from the lock smart app. For syncronization purposes.
    	return
    }
    //Received real ON from something -> Alexa
    if( lockedOn == "ON") {	
    	TRACE("LON LOCKING")
    	lock1.lock()
    } else {
    	if(unlock == "YES") {
        	TRACE("LON UNLOCKING")
    		lock1.unlock()
        }
    }
    
}
def lightOff(evt)
{
	log.info "$evt.name $evt.value $evt.descriptionText"
	if(descriptionText == "Virtual") {
    	TRACE("Virtual Light OFF")
    	//The light off event is from the lock smart app. For syncronization purposes.
    	return
    }
    //Received real OFF from something -> Alexa
    if(lockedOn == "ON") {
    	if(unlock == "YES") {
        	TRACE("LOFF UNLOCK")
    		lock1.unlock()
        }    
    } else {
    	TRACE("LOFF LOCK")
    	lock1.lock()
    }    
}
def doorLocked(evt) {
	log.info "$evt.name $evt.value $evt.descriptionText"
	//The lock was manually changed so update the virtual tile
    if(lockedOn == "ON") {
    	light1.syncOn()
        
    } else {
    	light1.syncOff()
    }

}

def doorUnlocked(evt) {
	log.info "$evt.name $evt.value $evt.descriptionText"
	//The lock was manually changed so update the virtual tile
    if(lockedOn == "ON") {
    	light1.syncOff()
        
    } else {
    	light1.syncOn()
    }
}
def TRACE (msg) {

	log.debug "$msg"
}