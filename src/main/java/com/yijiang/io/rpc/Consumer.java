package com.yijiang.io.rpc;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Auther: jiangyi
 * @Date: 2022-02-04
 * @Description: com.yijiang.io.rpc
 */
public class Consumer {

    public static void main(String[] args) {
        get();
    }

    public static void get(){
        new Thread(() -> {
            Provider.startServer();
        }).start();

        System.out.println("Server started....");

        AtomicInteger atomicInteger = new AtomicInteger(0);
        Thread[] threads = new Thread[20];
        for(int i = 0; i < threads.length;i++){
            threads[i] = new Thread(() -> {
                Car car = MyRPC.getByProxy(Car.class);
                String arg = "bmw" + atomicInteger.incrementAndGet();
                String ans = car.drive(arg);
                System.out.println(ans);
//                System.out.println("client over msg: " + ans + " src arg: " + arg);
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
