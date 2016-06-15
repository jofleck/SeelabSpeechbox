package de.fhdortmund.seelab.speechbox;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.protocol.Packet;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by jonas on 24.03.16.
 */
public class OpenHABConnection{
    private SocketIOClient client;
    final SocketIOServer server;

    public OpenHABConnection() {
        Configuration config = new Configuration();
        //config.setHostname("localhost");
        config.setPort(1301);
        config.setMaxFramePayloadLength(1024 * 1024 * 1024);
        config.setMaxHttpContentLength(1024 * 1024 * 1024);

        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

         server = new SocketIOServer(config);

        server.addConnectListener(new ConnectListener() {
            public void onConnect(SocketIOClient socketIOClient) {
                if(OpenHABConnection.this.client == null) {
                    OpenHABConnection.this.client = socketIOClient;
                    System.out.println("OpenHAB Connected...");
                    SpeechBox.getDisplay().offerText(new DisplayContent("Haussteuerung", "verbunden", 5000, true));
                    SpeechBox.getDisplay().setDefaultFirstLine("Zum Sprechen");
                    SpeechBox.getDisplay().setDefaultSecondLine("Knopf halten");
                    SpeechBox.getDisplay().notifyContentChanged();
                    TextToSpeechEngine.say("Verbindung hergestellt.");
                }
                else {
                    socketIOClient.sendEvent("msg", "Speechbox already in use");
                    socketIOClient.disconnect();
                }
            }
        });
        server.addDisconnectListener(new DisconnectListener() {
            public void onDisconnect(SocketIOClient socketIOClient) {
                if(socketIOClient == client) {
                    client = null;
                    System.out.println("OpenHAB Disconnected");
                    SpeechBox.getDisplay().offerText(new DisplayContent("Haussteuerung", "getrennt", 5000, true));
                    SpeechBox.getDisplay().setDefaultFirstLine("Keine Verbindung");
                    SpeechBox.getDisplay().setDefaultSecondLine("zum Server");
                    SpeechBox.getDisplay().notifyContentChanged();
                    TextToSpeechEngine.say("Die Verbindung zur Haussteuerung wurde unterbrochen. Dumm gelaufen.");
                }
            }
        });

        server.addEventListener("say", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                    TextToSpeechEngine.say(data);
                    System.out.println("Say: " + data);
            }
        });
        server.addEventListener("display", String.class, new DataListener<String>() {
            public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
                SpeechBox.getDisplay().offerText(new DisplayContent(data, "", 5000, false));
                SpeechBox.getDisplay().notifyContentChanged();
                System.out.println("Display: " + data);
            }
        });

        server.start();
        showConnectionInformation();

    }

    private void showConnectionInformation() {
        InetAddress adrr = getLocalAddress();
        String ip;
        if(adrr != null) {
            ip = adrr.getHostAddress();
        }
        else {
            ip = "Keine IP Adresse";
        }
        SpeechBox.getDisplay().setDefaultFirstLine("Nicht Verbunden");
        SpeechBox.getDisplay().setDefaultSecondLine(ip);
        SpeechBox.getDisplay().notifyContentChanged();
    }

    public boolean isConnected() {
        return client != null;
    }

    private class ShutdownHook extends Thread
    {
        public void run()
        {
            System.out.println("shutting down server...");
            server.stop();
            System.out.print("Stopped server");
        }
    }

    public void sendSpeechData(byte[] data) {
        System.out.println("Sending data with length:" + data.length);
        if(client != null) {
            client.sendEvent("speech", data);
        }
    }


    private InetAddress getLocalAddress()
    {
        try {

            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
