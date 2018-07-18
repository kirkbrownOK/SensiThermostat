/**
 *  Copyright Kirk Brown
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
 *  Severe Weather Alert controls LIGHTS 
 *
 *  Author: Kirk Brown based on Severe Weather Alert from SmartThings
 *  Date: 2013-03-04
 */
definition(
    name: "Send Sounds to Arduino Speaker",
    namespace: "kirkbrownOK/SendSoundsToArduinoSpeaker",
    author: "kirkbrown",
    description: "Monitor Doors, locks, people, and severe weather then play sounds on speaker",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-SevereWeather@2x.png"
)

preferences {
	section ("Make noise on these Door Sensors:") {
        input("doors", "capability.contactSensor", title: "Chime Only on these Doors", multiple:true, required: false)
        input("doors1","capability.contactSensor", title: "Specific Tone for this door:", multiple:false, required: false)
        input("noise1", "enum", title: "Tone # for door1:",options: attributeValues(),required: false) 
        input("doors2","capability.contactSensor", title: "Specific Tone for this door:", multiple:false, required: false)
        input("noise2", "enum", title: "Tone # for door2:",options: attributeValues(),required: false)
        input("doors3","capability.contactSensor", title: "Specific Tone for this door:", multiple:false, required: false)
        input("noise3", "enum", title: "Tone # for door3:",options: attributeValues(),required: false)
        input("doors4","capability.contactSensor", title: "Specific Tone for this door:", multiple:false, required: false)
        input("noise4", "enum", title: "Tone # for door4:",options: attributeValues(),required: false)
        input("doors5","capability.contactSensor", title: "Specific Tone for this door:", multiple:false, required: false)
        input("noise5", "enum", title: "Tone # for door5:",options: attributeValues(),required: false)
        input("doors6","capability.contactSensor", title: "Specific Tone for this door:", multiple:false, required: false)
        input("noise6", "enum", title: "Tone # for door6:",options: attributeValues(),required: false)
        input("doors7","capability.contactSensor", title: "Specific Tone for this door:", multiple:false, required: false)
        input("noise7", "enum", title: "Tone # for door7:",options: attributeValues(),required: false)
        input("doors8","capability.contactSensor", title: "Specific Tone for this door:", multiple:false, required: false)
        input("noise8", "enum", title: "Tone # for door8:",options: attributeValues(),required: false)
	}
	section ("Doorbell") {
        input("doorbell", "capability.contactSensor", title: "Doorbell Noise", multiple:true)
	}    
    section ("Tornado alarms:") {
    	input "watchSiren", "boolean", title: "Audible alarm for Tornado Watch? ", required: true
        input "warningSiren", "boolean", title: "Audible alarm for Tornado Warning", required: true
    }

	section ("Zip code (optional, defaults to location coordinates)...") {
		input "zipcode", "text", title: "Zip Code", required: false
	}
    section ("Update on this switch") {
    	input "updateSwitch", "capability.switch", title: "Switch that causes update", required: false, multiple: false
    }
    section ("Use this speaker") {
    	input "speaker", "capability.switch", title: "Speaker for sounds", required: false, multiple: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    initiliaze()

}

def updated() {
	log.debug "Updated with settings: ${settings}"
    unschedule()
    unsubscribe()
    initialize()    
}
def initialize() {
	state.alertKeys = ""
	scheduleJob()
    if(updateSwitch) {
    	subscribe(updateSwitch,"switch.on",checkForSevereWeather)
    }
    if(doors) {
    	subscribe(doors, "contact.open", doorChime)
    }
    if(doors1) {
        subscribe(doors1, "contact.open", doorChime1)
    }
    if(doors2) {
        subscribe(doors2, "contact.open", doorChime2)
    }
    if(doors3) {
        subscribe(doors3, "contact.open", doorChime3)
    }
    if(doors4) {
        subscribe(doors4, "contact.open", doorChime4)
    }
    if(doors5) {
        subscribe(doors5, "contact.open", doorChime5)
    }
    if(doors6) {
        subscribe(doors6, "contact.open", doorChime6)
    }
    if(doors7) {
        subscribe(doors7, "contact.open", doorChime7)
    }
    if(doors8) {
        subscribe(doors8, "contact.open", doorChime8)
    }
    if(doorbell) {
    	subscribe(doorbell, "contact.open", doorbellRing)        
    }
}

def scheduleJob() {
	def sec = Math.round(Math.floor(Math.random() * 60))
	def min = Math.round(Math.floor(Math.random() * 60))
	def cron = "$sec $min * * * ?"
    log.debug "chron: ${cron}"
	schedule(cron, "checkForSevereWeather")
}

def checkForSevereWeather(evt) {
	def alerts
	if(locationIsDefined()) {
		if(zipcodeIsValid()) {
			alerts = getWeatherFeature("alerts", zipcode)?.alerts
		} else {
			log.warn "Severe Weather Alert: Invalid zipcode entered, defaulting to location's zipcode"
			alerts = getWeatherFeature("alerts")?.alerts
		}
	} else {
		log.warn "Severe Weather Alert: Location is not defined"
	}
	log.debug "alerts: ${alerts}"
    if(alerts == []) {
    	state.tornadoWatch = false
        state.tornadoWarning = false
    }
	def newKeys = alerts?.collect{it.type + it.date_epoch} ?: []
	log.debug "Severe Weather Alert: newKeys: $newKeys"

	def oldKeys = state.alertKeys ?: []
	log.debug "Severe Weather Alert: oldKeys: $oldKeys"

	if (newKeys != oldKeys) {

		state.alertKeys = newKeys

		alerts.each {alert ->
			if (!oldKeys.contains(alert.type + alert.date_epoch) && descriptionFilter(alert.description)) {
				def msg = "Weather Alert! ${alert.description} from ${alert.date} until ${alert.expires}"
				if (alert.description.contains("Tornado Warning") && !state.tornadoWarning){
                	log.debug "Sending Tornado Warning Siren"
                    state.tornadoWatch = false
                    state.tornadoWarning = true
                	if(warningSiren) {
                    	
                        speaker.smsenddoorbell(1,3)
                    }
                if (alert.description.contains("Tornado Watch") && !state.tornadoWatch) {
                	log.debug "Sending Tornado Watch Siren"
                    state.tornadoWarning = false
                    state.tornadoWatch = true
                	if(watchSiren && !state.tornadoWarning) {
                    	
                        speaker.smsenddoorbell(1,2)
                    }
                }

                }
			}
		}
	}
}

def descriptionFilter(String description) {
	def filterList = ["special", "statement","thunderstorm", "test"]
	def passesFilter = true
	filterList.each() { word ->
		if(description.toLowerCase().contains(word)) { passesFilter = false }
	}
    log.debug "Description ${description} filter: ${passesFilter}"
	passesFilter
}

def locationIsDefined() {
	zipcodeIsValid() || location.zipCode || ( location.latitude && location.longitude )
}

def zipcodeIsValid() {
	zipcode && zipcode.isNumber() && zipcode.size() == 5
}
def doorChime(evt) {
	log.debug "doorChime()"
	//speaker.setRepeat(2)
    speaker.smsenddoorbell(1,0)
}
def doorbellRing(evt) {
	log.debug "doorbellRing()"
	//speaker.setRepeat(4)
    speaker.smsenddoorbell(1,1)
    doorbell.close()
}
def stopSpeaker(evt) {
	speaker.smsenddoorbell(1,1)
}
def doorChime1(evt) {
	log.debug "doorChime(1,$noise1)"
	//speaker.setRepeat(2)
    speaker.smsenddoorbell(1,noise1)
}
def doorChime2(evt) {
	log.debug "doorChime(1,$noise2)"
	//speaker.setRepeat(2)
    speaker.smsenddoorbell(1,noise2)
}
def doorChime3(evt) {
	log.debug "doorChime(1,$noise3)"
	//speaker.setRepeat(2)
    speaker.smsenddoorbell(1,noise3)
}
def doorChime4(evt) {
	log.debug "doorChime(1,$noise4)"
	//speaker.setRepeat(2)
    speaker.smsenddoorbell(1,noise4)
}
def doorChime5(evt) {
	log.debug "doorChime(1,$noise5)"
	//speaker.setRepeat(2)
    speaker.smsenddoorbell(1,noise5)
}
def doorChime6(evt) {
	log.debug "doorChime(1,$noise6)"
	//speaker.setRepeat(2)
    speaker.smsenddoorbell(1,noise6)
}
def doorChime7(evt) {
	log.debug "doorChime(1,$noise7)"
	//speaker.setRepeat(2)
    speaker.smsenddoorbell(1,noise7)
}
def doorChime8(evt) {
	log.debug "doorChime(1,$noise8)"
	//speaker.setRepeat(2)
    speaker.smsenddoorbell(1,noise8)
}


private attributeValues() {
	return ["7" : "Back Door",
                    "8" : "Front Door",
                    "9" : "Garage Door",
                    "10" : "Garage Entry Door",
                    "11" : "Garage Hail Door",
                    "12" : "Garage Hall Door",
                    "13" : "Kitchen Door",
                    "14" : "Living Room Door",
                    "15" : "Side Door",
                    "16" : "Jokes Back Door",
                    "17" : "Jokes Front Door",
                    "18" : "Jokes Garage Entry Door",
                    "19" : "Jokes Garage Hall Door",
                    "20" : "Jokes Garage Door",
                    "21" : "Jokes Garage Side Door",
                    "22" : "Jokes Kitchen Door",
                    "23" : "Jokes Living Room Door",
                    "24" : "Jokes Side Door"]
}