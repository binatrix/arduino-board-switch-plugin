# arduino-board-switch-plugin

Arduino plugin that allows you to select only the boards you want to see in the IDE. It works using the undocumented ".hide=" attribute in the "boards.txt" file that hides a board.

## Installation
- Download the "JAR" file from the [releases page](../../releases/latest) folder. 
- Open the "Tools" folder of your sketchbook folder. If this folder not exists please create.
- Inside the "Tools" folder create a new folder named "ArduinoBoardSwitch".
- Inside the "ArduinoBoardSwitch" create another folder named "tool".
- Finally, copy the "JAR" file inside this last folder.

Example:
```
C:\Users\mchav\Documents\Arduino\Tools\ArduinoBoardSwitch\tool\arduino-board-switch.jar
```

If all works and you start Arduino IDE you must see a new entry menu in your "Tools" menu like this:

![Alt text](/screen3.png?raw=true "Arduino plugin menu")

## Using

To hide boards first select the new menu entry and then select the platform you want to modify from the top list and then simply unselect the boards you want to hide. To finish press the "Save Changes" button.

![Alt text](/screen1.png?raw=true "plugin running")

 To see the changes you must restart Arduino IDE.

![Alt text](/screen2.png?raw=true "Arduino IDE screenshoot")

It was tested on the Arduino 1.18.10 version.

To compile you need BlueJ 4.1.4 or earlier (Java 8).

Hope this tools helps you.
