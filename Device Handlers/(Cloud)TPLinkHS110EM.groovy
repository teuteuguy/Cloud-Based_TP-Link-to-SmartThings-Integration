/*
TP-Link HS110 with Energy Monitor Cloud-connect Device Handler

Copyright 2017 Dave Gutheinz

Licensed under the Apache License, Version 2.0 (the "License"); you may not 
use this  file except in compliance with the License. You may obtain a copy 
of the License at:

		http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
License for the specific language governing permissions and limitations 
under the License.

##### Discalimer:  This Service Manager and the associated Device Handlers 
are in no way sanctioned or supported by TP-Link.  All  development is based 
upon open-source data on the TP-Link devices; primarily various users on GitHub.com.

##### Notes #####
1.	This DH is a child device to 'TP-Link Connect'.
3.	This device handler supports the TP-Link HS110 with Energy Monitor functions.
4.	Please direct comments to the SmartThings community thread
	'Cloud TP-Link Device SmartThings Integration'.
##### History #####
07-26-2017 - Initial Prototype Release
07-28-2017 - Beta Release
*/

metadata {
	definition (name: "TP-LinkHS110 Emeter", namespace: "beta", author: "Dave Gutheinz") {
		capability "Switch"
		capability "refresh"
		capability "polling"
		capability "powerMeter"
		capability "Sensor"
		capability "Actuator"
		command "setCurrentDate"
		attribute "monthTotalE", "string"
		attribute "monthAvgE", "string"
		attribute "weekTotalE", "string"
		attribute "weekAvgE", "string"
		attribute "engrToday", "string"
		attribute "dateUpdate", "string"
	}
	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#00a0dc",
				nextState:"waiting"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff",
				nextState:"waiting"
				attributeState "waiting", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#15EE10",
				nextState:"off"
				attributeState "commsError", label:'Comms Error', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#e86d13",
				nextState:"off"
			}
 			tileAttribute ("deviceError", key: "SECONDARY_CONTROL") {
				attributeState "deviceError", label: '${currentValue}'
			}
		}
		standardTile("refresh", "capability.refresh", width: 2, height: 2,  decoration: "flat") {
			state ("default", label:"Refresh", action:"refresh.refresh", icon:"st.secondary.refresh")
		}		 
		standardTile("blankTile", "", width: 2, height: 2,  decoration: "flat") {
			state ("", label:"")
		}		 
		standardTile("refreshStats", "Refresh Statistics", width: 2, height: 2,  decoration: "flat") {
			state ("refreshStats", label:"Refresh Stats", action:"setCurrentDate", icon: "st.secondary.refresh")
		}		 
		valueTile("power", "device.power", decoration: "flat", height: 1, width: 2) {
			state "power", label: 'Current Power \n\r ${currentValue} W'
		}
		valueTile("engrToday", "device.engrToday", decoration: "flat", height: 1, width: 2) {
			state "engrToday", label: 'Todays Usage\n\r${currentValue} KWH'
		}
		valueTile("monthTotal", "device.monthTotalE", decoration: "flat", height: 1, width: 2) {
			state "monthTotalE", label: '30 Day Total\n\r ${currentValue} KWH'
		}
		valueTile("monthAverage", "device.monthAvgE", decoration: "flat", height: 1, width: 2) {
			state "monthAvgE", label: '30 Day Avg\n\r ${currentValue} KWH'
		}
		valueTile("weekTotal", "device.weekTotalE", decoration: "flat", height: 1, width: 2) {
			state "weekTotalE", label: '7 Day Total\n\r ${currentValue} KWH'
		}
		valueTile("weekAverage", "device.weekAvgE", decoration: "flat", height: 1, width: 2) {
			state "weekAvgE", label: '7 Day Avg\n\r ${currentValue} KWH'
		}
		
		main("switch")
		details("switch", "refresh" ,"blankTile", "refreshStats", "power", "weekTotal", "monthTotal", "engrToday", "weekAverage", "monthAverage")
	}
}

def installed() {
	updated()
}

def updated() {
	unschedule()
	runEvery15Minutes(refresh)
	runIn(2, refresh)
	schedule("0 30 0 * * ?", setCurrentDate)
	runIn(6, setCurrentDate)
}

void uninstalled() {
	def alias = device.label
	log.debug "Removing device ${alias} with DNI = ${device.deviceNetworkId}"
	parent.removeChildDevice(alias, device.deviceNetworkId)
}

//	----- BASIC PLUG COMMANDS ------------------------------------
def on() {
	sendCmdtoServer('{"system":{"set_relay_state":{"state": 1}}}', "onOffResponse")
}

def off() {
	sendCmdtoServer('{"system":{"set_relay_state":{"state": 0}}}', "onOffResponse")
}

def onOffResponse(cmdResponse){
	refresh()
}

//	----- REFRESH ------------------------------------------------
def refresh(){
	sendEvent(name: "switch", value: "waiting", isStateChange: true)
	sendCmdtoServer('{"system":{"get_sysinfo":{}}}', "refreshResponse")
}
def refreshResponse(cmdResponse){
	def status = cmdResponse.system.get_sysinfo.relay_state
	if (status == 1) {
		status = "on"
	} else {
		status = "off"
	}
	log.info "${device.name} ${device.label}: Power: ${status}"
	sendEvent(name: "switch", value: status, isStateChange: true)
	getEngeryMeter()
}

//	----- Get Current Energy Use Rate ----------------------------
def getEngeryMeter(){
	sendCmdtoServer('{"emeter":{"get_realtime":{}}}', "energyMeterResponse")
}

def energyMeterResponse(cmdResponse) {
	if (cmdResponse["emeter"].err_code == -1) {
		log.error "This DH Only Supports the HS110 plug"
		sendEvent(name: "power", value: powerConsumption, descriptionText: "Bulb is not a HS110", isStateChange: true)
	} else {
		def state = cmdResponse["emeter"]["get_realtime"]
		def powerConsumption = state.power
		sendEvent(name: "power", value: powerConsumption, isStateChange: true)
		log.info "$device.name $device.label: Updated CurrentPower to $power"
		getUseToday()
	}
}

//	----- Get Today's Consumption --------------------------------
def getUseToday(){
	getDateData()
	sendCmdtoServer("""{"emeter":{"get_daystat":{"month": ${state.monthToday}, "year": ${state.yearToday}}}}""", "useTodayResponse")
}

def useTodayResponse(cmdResponse) {
	def engrToday
	def dayList = cmdResponse["emeter"]["get_daystat"].day_list
	for (int i = 0; i < dayList.size(); i++) {
		def engrData = dayList[i]
		if(engrData.day == state.dayToday) {
			engrToday = Math.round(1000*engrData.energy) / 1000
		}
   }
	sendEvent(name: "engrToday", value: engrToday, isStateChange: true)
	log.info "$device.name $device.label: Updated Today's Usage to $engrToday"
}

//	----- Get Weekly and Monthly Stats ---------------------------
def getWkMonStats() {
	state.monTotEnergy = 0
	state.monTotDays = 0
	state.wkTotEnergy = 0
	getDateData()
	sendCmdtoServer("""{"emeter":{"get_daystat":{"month": ${state.monthToday}, "year": ${state.yearToday}}}}""", "engrStatsResponse")
	runIn(4, getPrevMonth)
}

def getPrevMonth() {
	getDateData()
	if (state.dayToday < 31) {
		def month = state.monthToday
		def year = state.yearToday
		if (month == 1) {
			year -= 1
			month = 12
			sendCmdtoServer("""{"emeter":{"get_daystat":{"month": ${month}, "year": ${year}}}}""", "engrStatsResponse")
		} else {
			month -= 1
			sendCmdtoServer("""{"emeter":{"get_daystat":{"month": ${month}, "year": ${year}}}}""", "engrStatsResponse")
		}
	}
}

def engrStatsResponse(cmdResponse) {
	getDateData()
	def monTotEnergy = state.monTotEnergy
	def wkTotEnergy = state.wkTotEnergy
	def monTotDays = state.monTotDays
	Calendar calendar = GregorianCalendar.instance
	calendar.set(state.yearToday, state.monthToday, 1)
	def prevMonthDays = calendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH)
	def weekEnd = state.dayToday + prevMonthDays - 1
	def weekStart = weekEnd - 6
	if (cmdResponse["emeter"].err_code == -1) {
		log.error "This DH Only Supports the HS110 plug"
		sendEvent(name: "monthTotalE", value: 0, descriptionText: "Bulb is not a HS110", isStateChange: true)
	} else {
		def dayList = cmdResponse["emeter"]["get_daystat"].day_list
		def dataMonth = dayList[0].month
		def currentMonth = state.monthToday
		def addedDays = 0
		if (currentMonth == dataMonth) {
			addedDays = prevMonthDays
		} else {
			addedDays = 0
		}
		for (int i = 0; i < dayList.size(); i++) {
			def engrData = dayList[i]
			if(engrData.day == state.dayToday && engrData.month == state.monthToday) {
				monTotDays -= 1
			} else {
				monTotEnergy += engrData.energy
			}
			def adjustDay = engrData.day + addedDays
			if (adjustDay <= weekEnd && adjustDay >= weekStart) {
				wkTotEnergy += engrData.energy
			}
		}
		monTotDays += dayList.size()
		state.monTotDays = monTotDays
		state.monTotEnergy = monTotEnergy
		state.wkTotEnergy = wkTotEnergy
		if (state.dayToday == 31 || state.monthToday -1 == dataMonth) {
			log.info "$device.name $device.label: Updated 7 and 30 day energy consumption statistics"
			wkTotEnergy = Math.round(1000*wkTotEnergy) / 1000
			monTotEnergy = Math.round(1000*monTotEnergy) / 1000
			def wkAvgEnergy = Math.round((1000*wkTotEnergy)/7) / 1000
			def monAvgEnergy = Math.round((1000*monTotEnergy)/monTotDays) / 1000
			sendEvent(name: "monthTotalE", value: monTotEnergy, isStateChange: true)
			sendEvent(name: "monthAvgE", value: monAvgEnergy, isStateChange: true)
			sendEvent(name: "weekTotalE", value: wkTotEnergy, isStateChange: true)
			sendEvent(name: "weekAvgE", value: wkAvgEnergy, isStateChange: true)
		}
	}
}

//	----- Update date data ---------------------------------------
def setCurrentDate() {
	sendCmdtoServer('{"time":{"get_time":null}}', "currentDateResponse")
}

def currentDateResponse(cmdResponse) {
	def setDate =  cmdResponse["time"]["get_time"]
	updateDataValue("dayToday", "$setDate.mday")
	updateDataValue("monthToday", "$setDate.month")
	updateDataValue("yearToday", "$setDate.year")
	sendEvent(name: "dateUpdate", value: "${setDate.year}/${setDate.month}/${setDate.mday}")
	log.info "$device.name $device.label: Current Date Updated to ${setDate.year}/${setDate.month}/${setDate.mday}"
	getWkMonStats()
}

def getDateData(){
	state.dayToday = getDataValue("dayToday") as int
	state.monthToday = getDataValue("monthToday") as int
	state.yearToday = getDataValue("yearToday") as int
}
//	---------------------------------------------------------------------------
//	----- SEND COMMAND TO CLOUD VIA SM -----
private sendCmdtoServer(command, action){
	sendEvent(name: "deviceError", value: "OK")
	def appServerUrl = getDataValue("appServerUrl")
	def deviceId = getDataValue("deviceId")
	def cmdResponse = parent.sendDeviceCmd(appServerUrl, deviceId, command)
	String cmdResp = cmdResponse.toString()
	if (cmdResp.substring(0,5) == "ERROR"){
		def errMsg = cmdResp.substring(7,cmdResp.length())
		log.error "${device.name} ${device.label}: ${errMsg}"
		sendEvent(name: "switch", value: "commsError", descriptionText: errMsg)
		sendEvent(name: "deviceError", value: errMsg)
		action = ""
	}
	switch(action) {
		case "onOffResponse":
			onOffResponse(cmdResponse)
			break

		case "refreshResponse":
			refreshResponse(cmdResponse)
			break

		case "energyMeterResponse":
			energyMeterResponse(cmdResponse)
			break
			
		case "useTodayResponse":
			useTodayResponse(cmdResponse)
			break
			
		case "currentDateResponse":
			currentDateResponse(cmdResponse)
			break
			
		case "engrStatsResponse":
			engrStatsResponse(cmdResponse)
			break
			
		default:
			log.debug "at default"
	}
}