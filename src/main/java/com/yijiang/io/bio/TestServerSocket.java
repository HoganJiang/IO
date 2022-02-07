package com.yijiang.io.bio;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Auther: jiangyi
 * @Date: 2022-01-23
 * @Description: com.yijiang.io.netio
 */
public class TestServerSocket {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8090);
        System.out.println("Step1: new ServerSocket(8090)...");
        while (true) {
            Socket client = serverSocket.accept();
            System.out.println("Step2: client:\t" + client.getPort());

            new Thread(
                    new Runnable() {
                        Socket s;

                        public Runnable setSocket(Socket s) {
                            this.s = s;
                            return this;
                        }

                        @Override
                        public void run() {
                            try {
                                InputStream inputStream = s.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                while (true) {
                                    System.out.println(reader.readLine());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.setSocket(client)
            ).start();
        }
    }
}
