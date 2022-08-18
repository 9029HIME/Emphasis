package com.mzsk.designpattern.strategyfactory;

import java.util.HashMap;

/**
 * @Desc 策略工厂模式
 * 作为【策略工厂】，里面有许多的【策略】，每一个【策略】都有同一个【共同目标】，通过不同的【条件】去执行对应的【策略】
 * 假设有一个场景，通过用户的传参来确定处理策略。这个传参就是【条件】
 */
public class StrategyFactoryMode {
    public static void main(String[] args) {
        StrategyFactory.handle(1);
    }

    public static class StrategyFactory{
        private static final HashMap<Object,UserHandler> strategies = new HashMap<>();
        static {
            strategies.put("1", new UserHandler() {
                @Override
                public void doHandle() {
                    System.out.println("处理流程1");
                }
            });

            strategies.put("2", new UserHandler() {
                @Override
                public void doHandle() {
                    System.out.println("处理流程2");
                }
            });

            strategies.put("3", new UserHandler() {
                @Override
                public void doHandle() {
                    System.out.println("处理流程3");
                }
            });

            strategies.put(null, new UserHandler() {
                @Override
                public void doHandle() {
                    System.out.println("兜底处理流程");
                }
            });
        }

        public static void handle(Object condition){
            UserHandler userHandler = strategies.get(condition);
            if(userHandler != null){
                userHandler.doHandle();
            }else{
                UserHandler fallback = strategies.get(null);
                fallback.doHandle();
            }
        }
    }

    public interface UserHandler{
        void doHandle();
    }
}
