package com.yijiang.io;

import java.io.*;
import java.net.Socket;

/**
 * @Auther: jiangyi
 * @Date: 2022-01-23
 * @Description: com.yijiang.io
 */
public class SocketClient {
    public static void main(String[] args) {

        try {
            Socket client = new Socket("192.168.91.128", 9090);

            client.setSendBufferSize(20);
            client.setTcpNoDelay(true);
            OutputStream out = client.getOutputStream();

            InputStream in = System.in;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    byte[] bb = line.getBytes();
                    for (byte b : bb) {
                        out.write(b);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
