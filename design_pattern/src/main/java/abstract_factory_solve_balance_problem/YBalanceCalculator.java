package abstract_factory_solve_balance_problem;

import java.math.BigDecimal;

/**
 * 具体工厂，Y资金方的余额计算逻辑
 */
public class YBalanceCalculator implements BalanceCalculator{
    @Override
    public BalanceStatisticResult calculate(String date) {
        BalanceStatisticResult balanceStatisticResult = new BalanceStatisticResult();
        balanceStatisticResult.setOrgId(2);
        balanceStatisticResult.setProductId(2);
        balanceStatisticResult.setDate(date);
        balanceStatisticResult.setBalance(new BigDecimal("20000"));
        return balanceStatisticResult;
    }
}
