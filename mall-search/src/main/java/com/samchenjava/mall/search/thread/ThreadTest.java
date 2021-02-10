package com.samchenjava.mall.search.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ThreadTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
        new Thread(futureTask).start();
        //阻塞等待，等待整个线程执行完成，获取返回结果
        Integer integer = futureTask.get();
        System.out.println(integer);
    }

    private static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println();
        }
    }

    private static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println();
        }
    }

    private static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            return 11;
        }
    }
}
