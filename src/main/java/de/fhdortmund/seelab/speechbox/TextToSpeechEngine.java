package de.fhdortmund.seelab.speechbox;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.data.audio.AudioPlayer;

import javax.sound.sampled.AudioInputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by jonas on 07.04.16.
 */
public class TextToSpeechEngine {

    private static MaryInterface marytts;
    private static Queue<String> sayQueue;
    private static Thread queueThread;

    public static void init() {
        try {

            sayQueue = new LinkedList<String>();
            marytts = new LocalMaryInterface();
            Set<String> voices = marytts.getAvailableVoices();
            for (String v : voices) {
                System.out.println(v);
            }
            System.out.println("Voices count: " + voices.size());
            marytts.setVoice("bits1-hsmm");
            SpeechBox.getDisplay().offerText(new DisplayContent("Sprachsynthese", "aktiviert", 2000, false))
                    .notifyContentChanged();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void say(String text) {
        sayQueue.add(text);
        if (queueThread == null || !queueThread.isAlive()) {
            queueThread = new QueueThread();
            queueThread.start();
        }
    }

    private static class QueueThread extends Thread {
        @Override
        public void run() {
            while (!sayQueue.isEmpty()) {
                try {
                    AudioInputStream audio = marytts.generateAudio(sayQueue.poll());
                    AudioPlayer player = new AudioPlayer(audio);
                    player.start();
                    player.join();
                } catch (Exception e) {
                    System.out.println("Message: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
