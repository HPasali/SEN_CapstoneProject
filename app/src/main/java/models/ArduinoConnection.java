package models;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ArduinoConnection {
    private static Socket socket;
    private static final String IP_ADDRESS = "192.168.43.160"; //IP address of the ESP8266 WIFI module.
    private static final int PORT = 80; //Port of the ESP8266 WIFI module.
    private static OutputStream outputStream;
    private static PrintWriter printWriter;

    /*=>The sent commands on the ReservationPage and ProfilePage's methods as a parameter to this method  will be sent to the target WIFI module that is embedded
         to the NodeMCU module;*/
    public static void sendCommand(final String command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(IP_ADDRESS, PORT);
                    outputStream = socket.getOutputStream();
                    printWriter = new PrintWriter(outputStream);
                    printWriter.write(command);
                    printWriter.flush();
                    printWriter.close();
                    outputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}





