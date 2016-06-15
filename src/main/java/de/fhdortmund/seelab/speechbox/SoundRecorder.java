package de.fhdortmund.seelab.speechbox;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import javax.sound.sampled.*;;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by jonas on 23.03.16.
 */
public class SoundRecorder implements GpioPinListenerDigital {


    private AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    private TargetDataLine line;
    private final OpenHABConnection connection;
    private final String filename;
    private GpioPinDigitalInput pushToTallkPin;
    private boolean capturing;

    public SoundRecorder(OpenHABConnection conn, Pin talkPin) {
        this.connection = conn;
        filename = System.getProperty("java.io.tmpdir") + "/speech.wav";
        configKeyboardCapture();
        initGPIOCaptrue(talkPin);
    }

    private void configKeyboardCapture() {
        final Scanner sc = new Scanner(System.in);
        new Thread(new Runnable() {
            public void run() {

                for (; ; ) {

                    while (sc.next().charAt(0) != 's') {
                    }
                    new Thread(new Runnable() {
                        public void run() {
                            start();
                        }
                    }).start();

                    while (sc.next().charAt(0) != 's') {
                        System.out.println("not");
                    }
                    finish();
                }
            }
        }).start();
    }

    private void initGPIOCaptrue(Pin talkPin) {
        GpioController controller;
        if((controller = SpeechBox.getGpio()) == null) {
            System.out.println("Could not initialize Push2Talk Button");
            return;
        }
        pushToTallkPin = controller.provisionDigitalInputPin(talkPin, "Push to Talk Pin");
        pushToTallkPin.addListener(this);
    }

    /**
     * Defines an audio format
     */
    private AudioFormat getAudioFormat() {
        float sampleRate = 16000;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                channels, signed, bigEndian);
        return format;
    }

    /**
     * Captures the sound and record into a WAV file
     */
    public void start() {
        try {
            if(capturing) {
                return;
            }
            capturing = true;
            File wavFile = new File(filename);
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // checks if system supports line in
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Line not supported");
                System.exit(0);
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();    // start capturing

            System.out.println("Start capturing...");

            AudioInputStream ais = new AudioInputStream(line);

            System.out.println("Start recording...");
            AudioSystem.write(ais, fileType, wavFile);

        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    public void finish() {
        System.out.println("Finished");
        try {
            line.stop();
            line.close();
            Path path = Paths.get(filename);
            byte[] data = Files.readAllBytes(path);
            connection.sendSpeechData(data);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("This should never happen");
        }
        capturing = false;

    }

    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if(event.getPin() == pushToTallkPin) {
            if(event.getState() == PinState.LOW) {
                new Thread(new Runnable() {
                    public void run() {
                        start();
                    }
                }).start();
            }
            else {
                finish();
            }
        }
    }
}

