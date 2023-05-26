package abstract_factory_solve_balance_problem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 目前的项目（截止202211）对接了许多资方，每个资方的余额计算公式各不相同，甚至同一资方在不同产品的运用上，余额计算逻辑也不相同。
 * 早期是被抽象成各个方法，单独计算再汇总。后期需要将每个资方不同类型的余额计入“余额表”，因此改用工厂方法模式进行优化
 *
 * 省去了部分敏感业务字段和业务逻辑，以及省去了IOC注入过程。
 */
public class Main {
    public static void main(String[] args) {
        // 这里应使用ApplicationContext的getBean方法，通过BalanceCalculator.class获取所有资方余额计算器
        List<BalanceCalculator> calculatorList = new ArrayList<>();
        calculatorList.add(new XBalanceCalculator());
        calculatorList.add(new YBalanceCalculator());
        calculatorList.add(new ZBalanceCalculator());

        List<BalanceStatisticResult> results = new ArrayList<>(calculatorList.size());
        for (BalanceCalculator balanceCalculator : calculatorList) {
            BalanceStatisticResult result = balanceCalculator.calculate("2022-11-21");
            results.add(result);
        }

        // results的入库逻辑
    }
}