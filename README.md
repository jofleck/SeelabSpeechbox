# SeelabSpeechbox


The SeelabSpeechbox is a device to capture audio and synthesize text to speech. The networking is done by Socket.IO. The SpeechBox captures and forwards audio for further processing. It sends the caputured audio file in form of a byte array as a "speech" message to a connected client. If the speechbox receives the command "say" with a string as the parameter as a message, the text gets synthesized through MaryTTS. 

We use this together with a OpenHAB Binding to recognize speech in the smarthome context. You can use this for your own needs. Just connect a Socket.IO client to the speechbox and do what you want :)


### required hardware

Raspberry Pi B+/2/3  
Hitachi HD44780 compatible display 16x2  
resistors 2x10kOhm, 1x8,2kOhm, 1x56Ohm1, button (opener)  
1 NPN transistor  
GPIO-Pin connectors  
Microphone (USB)


### required software

JRE 7 or higher   
Maven 3  
WiringPi (included in Raspian)

### build your own speechbox

Connect the pins from your HD44780 to your raspberry pi. The pinout should be described in the datasheet of your display. You can change the datapins for your own needs in the LEDDisplay class. If you want you can use a NPN transistor to control the background light. You can choose every GPIO pin you want (parameter of the constructor of LEDDisplay class). In the example the pin is GPIO20. Please do not forget to connect a resistor to the base(10kOhm)! You need to set the contrast of the display with a resistor between the contrast pin of the display and GND of the pi. For our display a 8,2kOhm resistor was perfect. Maybe you have to play around a little bit. Every display is different. The microphone isn´t active until GPIO16 changes to LOW. To realize this, you have to connect the button to +3,3 and GPIO16. The GPIO connected side of the button must be connected to GND with a resistor (10kOhm or higher) in between. This is a pulldown circuit. 

If you want to modify the pinout from the code pay attention to the Pi4J/WiringPi pinout: 

![image](http://pi4j.com/images/j8header-b-plus.png)


You can run the code with "mvn clean install && mvn exec:java" as root.

