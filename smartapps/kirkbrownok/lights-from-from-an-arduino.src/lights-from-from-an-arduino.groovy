/**
 *  Light Follows Me and restores state before motion
 *
 *  Author: SmartThings and OKpowerman
 */

definition(
    name: "Lights From from an Arduino",
    namespace: "kirkbrownOK",
    author: "Kirk Brown",
    description: "Turn your lights on when contact sensors Open then off after they close and some period of time later.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Turn on when the contact sensor opens..."){
		input "contact1", "capability.contactSensor", title: "Where?", multiple: true
	}
    section("Which Sensor number should I listen for?") {
    	input "sensorNum", "number",required: false
    }
    
	section("How long should the light stay on after the sensor is closed?"){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Turn on/off light(s)..."){
		input "switches", "capability.switch", multiple: true
	}
    section("Using either on this light sensor (optional) or the local sunrise and sunset"){
		input "lightSensor", "capability.illuminanceMeasurement", required: false
	}
    section("Perform light operations no matter the time of day?") {
    	input "ignoreTOD", "enum", required: true, options: ["Yes", "No"]
    }
	section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Zip code (optional, defaults to location coordinates when location services are enabled)...") {
		input "zipCode", "text", title: "Zip code", required: false
	}
    
}

def installed() {
	initialize()

    
}

def updated() {
	unsubscribe()
	unschedule()
    getSunriseOffset()
    getSunsetOffset()
	initialize()

    
}

def contactHandlerOpen(evt) {
	TRACE("Opened")
    if( ignoreTOD == "Yes") {
    	switches.on()
    	switches.setLevel(99)
    	TRACE( "Ignored TOD Open Event: ${evt.name}: ${evt.value}")
        state.switches=switches.currentSwitch
        state.levels= switches.currentLevel
    	TRACE("switches.currentState ${switches.currentSwitch} state: ${state.switches} level ${state.levels}") 
    }
    else if(enabled()) {
    	
    	switches.on()
    	switches.setLevel(99)
    	TRACE( "Open Event: $evt.name: $evt.value")
        state.switches=switches.currentSwitch
        state.levels= switches.currentLevel
    	TRACE("switches.currentState ${switches.currentSwitch} state: ${state.switches} level ${state.levels}")      
    }    

}
def contactHandlerClose(evt) {
	TRACE("Door Closed, Timer started")
    TRACE("Close Event: ")
	runIn(minutes1 * 60, returnLightsToNormal)
    
}
def returnLightsToNormal() {
	TRACE("Turning lights off")
	//switches.setLevel(10)
    switches.off()

	for (it in (switches)) {
    	//TRACE("state sw: ${state.switches[it]}")
//    	switches.${state.switches[it]}
        
    }

}


def initialize() {
	if (sensorNum > 0) {
    	TRACE("SUBSCRIBING TO Sensornum: ${sensorNum}")
		subscribe(contact1, "contact.Sensor${sensorNum}:open", contactHandlerOpen)
    	subscribe(contact1, "contact.Sensor${sensorNum}:close", contactHandlerClose)
    } else {
    	TRACE("Subscribing to ${contact1} contact.open")
    	subscribe(contact1, "contact.open", contactHandlerOpen)
        subscribe(contact1, "contact.closed", contactHandlerClose)
    }
    //subscribe(switches, "switch", switchHandler)
    state.controlState = "off"
    
    state.controlControl = false
	if (ignoreTOD == "Yes") {
    	TRACE("Ignore TOD")
    }else if (lightSensor) {
		subscribe(lightSensor, "illuminance", illuminanceHandler, [filterEvents: false])
	}
	else {
		//subscribe(location, "position", locationPositionChange)
        //subscribe(switch, "on", sunriseSunsetTimeHandler)
		//subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
		//subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)
		astroCheck()
        //state.lastAstroCheck = now() - 86400000
	}
}
def switchHandler(evt) {
	//Disable any motion control even because the switch was manually controlled
    updated()
    TRACE("Motion Control Restart")
    

}
/*
def sunriseSunsetTimeHandler(evt) {
	state.lastSunriseSunsetEvent = now()
	log.debug "SmartNightlight.sunriseSunsetTimeHandler($app.id)"
	astroCheck()
}
*/
def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
    state.lastAstroCheck = now()
	TRACE( "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime) lastAstro ${state.lastAstroCheck}")

}

private enabled() {
	def result
    if (now() - state.lastAstroCheck > 3600000) {
    	//Its been 1 hours since last sunset/sunrise time
    	astroCheck()    
    } else {
    	TRACE( "Astro not needed, perform in ${(86400000-(now() - state.lastAstroCheck))/(1000*60*60)} hours")
    }
	if (lightSensor) {
		result = lightSensor.currentIlluminance < 30
	}
	else {
		def t = now()
        TRACE("now is ${t} rising Time: ${state.riseTime} setTime: ${state.setTime}")
		result = t < state.riseTime || t > state.setTime
	}
    TRACE("Finish enabled: ${result}")
	result
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}

private def TRACE(message) {
    log.debug message
}