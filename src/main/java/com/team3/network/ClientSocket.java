package com.team3.network;

import java.io.*;
import java.net.Socket;

public class ClientSocket {

    private final String HOST = "127.0.0.1";
    private final int PORT = 5000;

    public String send(String msg) {
        try (
                Socket socket = new Socket(HOST, PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            out.println(msg);
            return in.readLine();
        } catch (Exception e) {
            return "ERROR";
        }
    }
}
