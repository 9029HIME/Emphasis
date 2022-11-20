package abstract_factory_solve_balance_problem;

import java.math.BigDecimal;

/**
 * 具体工厂，Z资金方的余额计算逻辑
 */
public class ZBalanceCalculator implements BalanceCalculator{
    @Override
    public BalanceStatisticResult calculate(String date) {
        BalanceStatisticResult balanceStatisticResult = new BalanceStatisticResult();
        balanceStatisticResult.setOrgId(3);
        balanceStatisticResult.setProductId(3);
        balanceStatisticResult.setDate(date);
        balanceStatisticResult.setBalance(new BigDecimal("999.99"));
        return balanceStatisticResult;
    }
}