# Hub-Based TP-Link Bulb, Plug, and Switch SmartThing Integration

Two versions of TP-Link to Smart Things Integraion now exist:

a. Hub-Based TP-Link to Smart Things Integraion: The Hub-based version that requires an always on Hub device (bridge). Attributes:

    Requires user-configured (PC, Android, Raspberry) Hub with node.js and server script.
    Does not require a token captured from the TP-Link cloud.
    Manual device installation and setting static IP addresses.

b. Cloud-Based TP-Link to Smart Things Integraion: The new version (currently in Beta) that relies on the TP-Link Kasa cloud. Attributes:

    Reliant on TP-Link cloud (and the continued availabilty of same).
    Must have TP-Link account.
    Simpler setup. Install Service Manager and applicable device handlers. Runs service Manager.

# Cloud-Based Pre-requisites:

a.  A valid TP-Link Kasa account (must have login name and password.

b.  TP-Link devices installed and in 'Remote Control' mode (done in Kasa App)

Caveate:  The author is not associated with the company TP-Link except as an owner/consumer of their products.  All date used to create thes applets was garnered from public-domain data.

<img src="https://github.com/DaveGut/Cloud-Based_TP-Link-to-SmartThings-Integration/blob/master/FamilyPic.png" align="center"/>

TP-Link Devices Supported:

    HS100, Hs105, HS110, HS200 - TP-Link_HS_Series.groovy
    HS110 with energy monitor functions - TP-Link_HS110_Emeter.groovy
    LB100, LB110 - TP-Link_LB100_110.groovy
    LB110 with energy monitor functions - TP-Link_LB110_Emeter.groovy
    LB120 - TP-Link_LB120.groovy
    LB120 with energy monitor functions - TP-Link_LB120_Emeter.groovy
    LB130 - TP-Link_LB130.groovy
    LB130 with energy monitor functions - TP-Link_LB130_Emeter.groovy

Installation instructions can be found in the documentation folder.

    New (initial): In the README.MD files in the Device Handler and Service Manager folders.

Files:

Service Manager.  The installation and communications application.

Device Handlers. All SmartThings device handlers. Names are clear as to device applicability.


Documentation. Design Notes, and Interface description, TP-Link Cloud Error Codes.
