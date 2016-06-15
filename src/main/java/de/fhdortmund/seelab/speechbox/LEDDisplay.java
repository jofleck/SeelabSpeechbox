package de.fhdortmund.seelab.speechbox;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.Lcd;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by jonas on 15.04.16.
 */
public class LEDDisplay {
    private GpioPinDigitalOutput backgroundLight;
    private int lcdHandle ;
    private String defaultFirstLine;
    private String defaultSecondLine;
    private boolean autoTurnOffLight;
    private Thread queueThread;
    private Queue<DisplayContent> contentQueue;

    public LEDDisplay(Pin backroundLightPin) {
        defaultFirstLine = "";
        defaultSecondLine = "";
        lcdHandle = -1;
        contentQueue = new LinkedList<DisplayContent>();
        initGPIO(backroundLightPin);
    }

    private void initGPIO(Pin backroundLightPin) {
        GpioController controller;
        if ((controller = SpeechBox.getGpio()) == null) {
            System.out.println("GPIO Controller could not be initialized");
            return;
        }
        backgroundLight = controller.provisionDigitalOutputPin(backroundLightPin);
        if (Gpio.wiringPiSetup() == -1) {
            System.out.println(" ==>> GPIO SETUP FAILED");
            return;
        }
        lcdHandle = Lcd.lcdInit(2,     // number of row supported by LCD
                16,  // number of columns supported by LCD
                4,     // number of bits used to communicate to LCD
                7,           // LCD RS pin
                0,           // LCD strobe pin
                1,            // LCD data bit 1
                3,            // LCD data bit 2
                4,            // LCD data bit 3
                5,            // LCD data bit 4
                0,            // LCD data bit 5 (set to 0 if using 4 bit communication)
                0,            // LCD data bit 6 (set to 0 if using 4 bit communication)
                0,            // LCD data bit 7 (set to 0 if using 4 bit communication)
                0);           // LCD data bit 8 (set to 0 if using 4 bit communication)

        // verify initialization
        if (lcdHandle == -1) {
            System.out.println(" ==>> LCD INIT FAILED");
            return;
        }
        // clear LCD
        Lcd.lcdClear(lcdHandle);
        Lcd.lcdHome(lcdHandle);

    }

    public LEDDisplay offerText(DisplayContent content) {
        contentQueue.offer(content);
        return this;
    }

    public void setDefaultFirstLine(String firstLine) {
        assert firstLine != null;
        this.defaultFirstLine = firstLine;
    }
    public void setDefaultSecondLine(String secondLine) {
        assert secondLine != null;
        this.defaultSecondLine = secondLine;
    }

    public synchronized void notifyContentChanged() {
        if(queueThread == null || !queueThread.isAlive()) {
            queueThread = new DisplayThread();
            queueThread.start();
        }
    }


    public void lightOn() {
        autoTurnOffLight = false;
        if (backgroundLight != null) {
            backgroundLight.high();
        }
    }

    public void lightOff() {
        autoTurnOffLight = true;
        if (backgroundLight != null) {
            backgroundLight.low();
        }
    }

    private class DisplayThread extends Thread {
        @Override
        public void run() {
            if(!contentQueue.isEmpty() && backgroundLight != null) {
                backgroundLight.high();
            }
            while(!contentQueue.isEmpty()) {
                try {
                    DisplayContent c = contentQueue.poll();
                    Lcd.lcdClear(lcdHandle);
                    Lcd.lcdHome(lcdHandle);
                    Lcd.lcdPuts(lcdHandle, c.getFirstLine());
                    Lcd.lcdPosition(lcdHandle, 0, 1);
                    Lcd.lcdPuts(lcdHandle, c.getSecondLine());
                    Thread.sleep(c.getDuration());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if(autoTurnOffLight && backgroundLight != null) {
                backgroundLight.low();
            }
            Lcd.lcdClear(lcdHandle);
            Lcd.lcdHome(lcdHandle);
            Lcd.lcdPuts(lcdHandle, defaultFirstLine);
            Lcd.lcdPosition(lcdHandle, 0, 1);
            Lcd.lcdPuts(lcdHandle, defaultSecondLine);

        }
    }


}
