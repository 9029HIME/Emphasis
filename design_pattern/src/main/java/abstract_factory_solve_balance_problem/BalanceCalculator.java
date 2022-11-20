package abstract_factory_solve_balance_problem;

import java.util.List;

/**
 * 抽象工厂
 */
public interface BalanceCalculator {
    BalanceStatisticResult calculate(String date);
}
