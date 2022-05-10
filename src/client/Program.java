package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Program {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(ResourceBundle.getBundle("settings").getString("port"));
        String host = ResourceBundle.getBundle("settings").getString("host");
        try (Client client=  new Client(host,port, (message) -> {
            System.out.println(message);
        }))  {
            System.out.println("Connected to server");

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    String command = scanner.nextLine();
                    client.send(command);

                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            System.exit(0);
        }

    }

}
