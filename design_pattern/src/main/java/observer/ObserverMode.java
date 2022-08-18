package com.mzsk.designpattern.observer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Desc 观察着模式
 * 核心三要素：观察者，被观察者，事件
 * 被观察者维护一个观察者列表，当被观察者触发事件时，循环观察者列表，调用观察者的方法来处理事件
 */
public class ObserverMode {
    public static void main(String[] args) {
        EventPublisher.publish("Hello~World~");
    }

    public interface Observer{
        void doObserver(String msg);
    }

    public static class ObserverA implements Observer{
        @Override
        public void doObserver(String msg) {
            System.out.println(String.format("A观察者收到消息：%s",msg));
        }
    }

    public static class ObserverB implements Observer{
        @Override
        public void doObserver(String msg) {
            System.out.println(String.format("B观察者收到消息：%s",msg));
        }
    }

    public static class ObserverC implements Observer{
        @Override
        public void doObserver(String msg) {
            System.out.println(String.format("C观察者收到消息：%s",msg));
        }
    }

    public static class EventPublisher {
        static List<Observer> observers = new LinkedList<>();
        static {
            observers.add(new ObserverA());
            observers.add(new ObserverB());
            observers.add(new ObserverC());
        }

        public static void publish(String msg){
            System.out.println(String.format("发送者发送消息:%s",msg));
            for (Observer observer : observers) {
                observer.doObserver(msg);
            }
        }
    }

}
