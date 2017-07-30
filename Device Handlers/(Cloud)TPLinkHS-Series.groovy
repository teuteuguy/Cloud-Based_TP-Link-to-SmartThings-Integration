/*
TP-Link HS-Series Cloud-connect Device Handler

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
3.	This device handler supports the TP-Link HS-Series functions.
4.	Please direct comments to the SmartThings community thread
	'Cloud TP-Link Device SmartThings Integration'.
##### History #####
07-26-2017 - Initial Prototype Release
07-28-2017 - Beta Release
*/

metadata {
    definition (name: "TP-LinkHS-Series", namespace: "beta", author: "Dave Gutheinz") {
        capability "Switch"
        capability "refresh"
        capability "polling"
        capability "Sensor"
        capability "Actuator"
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
      
        main("switch")
        details("switch", "refresh")
    }
}

def installed() {
    updated()
}

def updated() {
    unschedule()
    runEvery15Minutes(refresh)
    runIn(2, refresh)
}

void uninstalled() {
	def alias = device.label
	log.debug "Removing device ${alias} with DNI = ${device.deviceNetworkId}"
	parent.removeChildDevice(alias, device.deviceNetworkId)
}

//    ----- BASIC PLUG COMMANDS ------------------------------------
def on() {
    sendCmdtoServer('{"system":{"set_relay_state":{"state": 1}}}', "onOffResponse")
}

def off() {
    sendCmdtoServer('{"system":{"set_relay_state":{"state": 0}}}', "onOffResponse")
}

def onOffResponse(cmdResponse){
    refresh()
}

//    ----- REFRESH ------------------------------------------------
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
}

//    ---------------------------------------------------------------------------
//    ----- SEND COMMAND TO CLOUD VIA SM -----
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

        default:
        	log.debug "at default"
    }
}