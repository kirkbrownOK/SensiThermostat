# SensiThermostat
SmartThings Sensi Thermostat


The Sensi (Connect) SmartApp and the Sensi Thermostat Device Handler are fully functional as far as I know. Please give them a try and see what you think.


Quick word on SmartThings (Connect) apps and their device type handler. You must copy and paste the code into your account for smartapps into the smartapp section. Then you must copy and past the code for the device type handler in the device type handler section. Then you install the smartapp. If you do it right, the smartapp itself will actually create the devices itself and not you manually creating them. Good luck! Here are detailed information on copy/paste your own code.


These directions are from: https://community.smartthings.com/t/faq-an-overview-of-using-custom-code-in-smartthings/16772

USING A CUSTOM SMARTAPP

This involves two steps.

A one time process to "publish" the smartapp code to yourself using the Developers section of the SmartThings.com website so it is available for future installation.

Then using the official SmartThings mobile app to install it so you can use it with specific devices.

First, the One Time Process to Publish it to Yourself

SA1) Copy the code from the author.

SA2) Sign in to the Developers section of the SmartThings website so you can access the IDE (Integrated Development Environment). To get there, first click on "Community" at the top right of this page, then click on "Developer Tools" in the top right of that next page.

SA3) Choose SmartApps, then Add a New SmartApp from Code.

SA4) Paste in the code you copied, change anything necessary based on the author's instructions, then PUBLISH it to yourself.

SA5) Make any additional edits according to the author's instructions, such as enabling OAUTH.

Now when you open the official SmartThings mobile app, this new custom smartapp will appear as a choice under My SmartApps in the SmartApp section in the Marketplace.

Next, the Install Process to Assign that SmartApp to a Specific Device..

To assign that SmartApp to a specific device:

SA6) Open the ST mobile app.

SA7) Go to the Dashboard, then click on the Marketplace icon (the multicolored asterisk in the lower right).

SA8) Choose SmartApps

SA9) Scroll down to the MY SMARTAPPS section and choose it.

SA10) Scroll down to find the custom smartapp you want, then install it.

SA11) Follow the set up wizard instructions for that smartapp.

Some custom SmartApps also require a custom Device Handler to work. If so, the author will mention that in the installation instructions

USING A CUSTOM DEVICE TYPE HANDLER

Easy!

These steps assume you have already added the device to your account through the SmartThings mobile app. It may be using a standard device type handler, or it may just have been added as a "thing", but it should show up on the list of devices for your account.

(If this is an ip-addressable device like a camera you may not have been able to add it to your account through the SmartThings mobile app, so the system will not assign it a device ID. In that case you will need to sign into the Developers section (IDE) and first choose My Devices and then use the ADD NEW DEVICE button to enter a placeholder for the device and assign it a unique device ID. You can choose any device type handler for the placeholder since you're going to change it in a minute anyway. Then you can continue with the following steps.)

DT1) Copy the code from the author.

DT2) Sign in to the Developers section of the SmartThings website. (To get there, first click on "Community" at the top right of this page, then click on "Developer Tools" in the top right of that next page.)

DT3 Choose Device Handlers, then Add a New Device Handler from Code.

DT4) Paste in the code you copied, change anything necessary based on the author's instructions, then CREATE it for yourself.

DT5) Once the Device Handler is published in your own library, select MY DEVICES in the IDE and choose the specific device you want to have use that new device handler.

DT6) Edit the Device so that it uses that device type handler.

Done!

Now any SmartApp that wants to talk to that device will be able to request the features specified in the custom device type handler. (Again, the physical device has to already support the features, the device type handler just translates the requests between SmartThings and the device.)
