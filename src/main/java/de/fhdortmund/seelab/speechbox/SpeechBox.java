package de.fhdortmund.seelab.speechbox;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;

import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Lcd;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jonas on 24.03.16.
 */
public class SpeechBox {
    private static GpioController gpio;
    private static LEDDisplay display;

    public static void main(String[] args) throws Exception {
        display = new LEDDisplay(RaspiPin.GPIO_28);
        display.lightOn();
        display.setDefaultFirstLine("Seelab SpeechBox");
        display.setDefaultSecondLine("Bitte warten...");
        display.notifyContentChanged();
        TextToSpeechEngine.init();
        OpenHABConnection conn = new OpenHABConnection();
        SoundRecorder s = new SoundRecorder(conn, RaspiPin.GPIO_27);
    }


    public static GpioController getGpio() {
        if(gpio == null) {
            try {
                gpio = GpioFactory.getInstance();
            } catch(Error e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return gpio;
    }

    public static LEDDisplay getDisplay() {
        return display;
    }
}
