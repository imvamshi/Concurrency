package com.rv;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumer {
    public static void main(String[] args) throws InterruptedException {
        GoodQueue goodQueue = new GoodQueue(7);

        Putter putter = new Putter(goodQueue);
        Putter putter2 = new Putter(goodQueue);
        Putter putter3 = new Putter(goodQueue);

        Eater eater = new Eater(goodQueue);
        Eater eater2 = new Eater(goodQueue);
        Eater eater3 = new Eater(goodQueue);

        putter.start();
        putter2.start();
        putter3.start();
        eater.start();
        eater2.start();
        eater3.start();
    }
}

class GoodQueue {
    private static void printList(List<String> otherlist) {
        System.out.print("[");
        for (int i = 0; i < otherlist.size(); i++) {
            System.out.print(otherlist.get(i));
            if(i != otherlist.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println();
    }

    private int limit;
    List<String> list = new LinkedList<>();


    private Lock lock = new ReentrantLock();
    private Condition someSpaceLeft = lock.newCondition();
    private Condition canConsume = lock.newCondition();

    GoodQueue(int queueSize) {
        this.limit = queueSize;
    }

    public void push(String s) throws InterruptedException {
        lock.lock();
        try {
            if(list.size() > limit) {
                someSpaceLeft.await();
            }
            printList(list);
            list.add(s);
            canConsume.signal();
        } finally {
            lock.unlock();
        }
    }

    public void pop() throws InterruptedException {
        lock.lock();
        try {
            if(list.size() == 0) {
                canConsume.await();
            }
            System.out.println("Consumed " + list.get(0));
            list.remove(0);
            someSpaceLeft.signal();
        } finally {
            lock.unlock();
        }
    }
}

class Putter extends Thread {
    GoodQueue goodQueue;
    Random rand = new Random();

    Putter(GoodQueue thatGoodQueue) {
        this.goodQueue = thatGoodQueue;
    }

    public void put(String s) throws InterruptedException {
        goodQueue.push(s);
    }

    @Override
    public void run() {
//        for (int i = 0; i < 100; i++) {
        while (true) {
            int min = 50, max = 7756;
            int x = (int)Math.floor(Math.random()*(max-min)+min);
            try {
                put(Integer.toString(x));
                int randValue = rand.nextInt(1) + 1;
                Thread.sleep(rand.nextInt(200) + randValue % 2 == 0 ? 200 : 250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Eater extends Thread {
    GoodQueue goodQueue;
    Random rand = new Random();

    Eater(GoodQueue thatGoodQueue) {
        this.goodQueue = thatGoodQueue;
    }

    public void eat() throws InterruptedException {
        goodQueue.pop();
    }

    @Override
    public void run() {
        try {
            while (true) {
                eat();
                int randValue = rand.nextInt(1) + 1;
                Thread.sleep(rand.nextInt(200) + randValue % 2 == 0 ? 200 : 300);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}