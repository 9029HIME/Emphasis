package strategy_factory_solve_market_problem;

import java.util.HashMap;
import java.util.Map;

/**
 * 某个大盘页面展示5种类型的数据，通过接口传参dataType，决定响应哪种类型的大盘数据（注意！！数据返回格式是一样的，只是处理逻辑和值不同）
 */
public class Main {
    public static void main(String[] args) {
        defectiveMethod(1);
        betterMethod(1);
    }

    /**
     * 没使用设计模式的前提下，不太好的处理方法（实际的业务逻辑肯定不是简单的一句sout）
     */
    public static void defectiveMethod(Integer dataType) {
        if (dataType.equals(1)) {
            // 大盘数据类型1的实现
            System.out.println("响应大盘数据1");
        } else if (dataType.equals(2)) {
            // 大盘数据类型2的实现
            System.out.println("响应大盘数据2");
        } else if (dataType.equals(3)) {
            // 大盘数据类型3的实现
            System.out.println("响应大盘数据3");
        } else if (dataType.equals(4)) {
            // 大盘数据类型4的实现
            System.out.println("响应大盘数据4");
        } else if (dataType.equals(5)) {
            // 大盘数据类型5的实现
            System.out.println("响应大盘数据5");
        } else {
            throw new RuntimeException("错误的传入类型");
        }
    }

    /**
     * 使用策略工厂模式后的处理方法
     */
    public static void betterMethod(Integer dataType) {
        DataHandler dataHandler = dataHandlerMap.get(dataType);
        if (dataHandler == null) {
            throw new RuntimeException("错误的传入类型");
        }
        dataHandler.handle();
    }


    /**
     * 策略处理器
     */
    public interface DataHandler {
        void handle();
    }

    /**
     * 策略工厂
     */
    public static final Map<Integer, DataHandler> dataHandlerMap = new HashMap<>();

    /**
     * 实际肯定不是直接添加一个匿名对象，这里为了展示方便。
     */
    static {
        dataHandlerMap.put(1, new DataHandler() {
            @Override
            public void handle() {
                System.out.println("响应大盘数据1");
            }
        });

        dataHandlerMap.put(2, new DataHandler() {
            @Override
            public void handle() {
                System.out.println("响应大盘数据2");
            }
        });

        dataHandlerMap.put(1, new DataHandler() {
            @Override
            public void handle() {
                System.out.println("响应大盘数据3");
            }
        });

        dataHandlerMap.put(1, new DataHandler() {
            @Override
            public void handle() {
                System.out.println("响应大盘数据4");
            }
        });

        dataHandlerMap.put(1, new DataHandler() {
            @Override
            public void handle() {
                System.out.println("响应大盘数据5");
            }
        });
    }

}
