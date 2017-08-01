# Cloud-Based TP-Link to Smart Things Integration

This package is for the SmartThings integration of TP-Link bulbs and plugs through the TP-Link cloud.  Prerequisites:

    1.  A TP-Link Kasa account current and valid username and password.
    
    2.  Devices that are installed through the Kasa Application.
    
    3.  Devices in "remote control" mode set through the Kasa app.

# INSTALLATION

A.  Install the Service Manager ("TPLink Connect (unoffficial).groovy")

    1)  Log onto the IDE and open the mySmartApps tab
        a)  From community page, upper right side, select "COMMUNITY"
        b)  Upper right:  "Additional Resources"   "Developer Tools"
        c)  Log-in or follow instructions to obtain an account.

    2)  Select '+New SmartApp' at the top-right
    
    3)  Select the 'From Code' tab

    4)  Paste the contents of the file into the space.
    
    5)  Select 'Create then' 'Publish'.
    
B.  Install your selected Device Handlers.

(Note:  Do not select the Energy Monitor version for initial installation.  See below).

    1)  Open the 'My Device Hsndlers' tab.
    
    2)  Follow the same instructions as in A (above) for each device type you have.
    
C.  From your smart phone SmartThings application:

Note 1:  To be detected, the bulb must be set to 'remote control' in the device's options in the Kasa App.

Note 2:  The Service Manager will attain the token from TP-Link for you.  Do not do this yourself.

    1)  Select "Automation" (at bottom), then the 'SmartApps' tab
    
    2)  Select '+ Add a SmartApp' at the bottom.
    
    3)  Select '+ My Apps' at the bottom
    
    4)  Select the app 'TP-Link (unofficial) Connect'
    
    5)  Follow on-screen instructions.

D.  Upgrading to Energy Monitor Function for a device

    1)  Upload the corresponding Energy Monitor Device Handler for the device (as above).
    
    2)  Go to the 'My Devices" tab on the IDE.
    
    3)  Select the device and go to the bottom and select 'EDIT'
    
    4)  Go to the pull-down field 'Type *' and use the pull-down to select the EM device handler.
    
    5)  Select 'UPDATE' at the bottom.
    
# Included Files

In the folder "Service Manager": TPLink Connect (unoffficial).groovy - Service Manager for installation and management of the device handlers.

In the folder "Device Handlers":

1.  (Cloud)TPLinkHS-Series.groovy - Device Handler for the TP-Link HS Series (HS100, HS105, HS110, HS200) plugs/switches.

2.  (Cloud)TPLinkLB100-110.groovy - Device Handler for the TP-Link HS100 and HS110 bulbs.

3.  (Cloud)TPLinkLB120.groovy - Device Handler for the TP-Link LB120 bulb.

4.  (Cloud)TPLinkLB130.groovy - Device Handler for the TP-Link LB130 bulb

5.  (Cloud)TPLinkHS110EM.groovy - Replacement DH for the HS110 with Energy Monitor (see instructions).

6.  (Cloud)TPLinkLB110EM.groovy - Replacement DH for the LB110 with Energy Monitor (see instructions).

7.  (Cloud)TPLinkLB120EM.groovy - Replacement DH for the LB120 with Energy Monitor (see instructions).

8.  (Cloud)TPLinkLB130EM.groovy - Replacement DH for the LB130 with Energy Monitor (see instructions).
