package abstract_factory_solve_balance_problem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 具体工厂，X资金方的余额计算逻辑
 */
public class XBalanceCalculator implements BalanceCalculator{
    @Override
    public BalanceStatisticResult calculate(String date) {
        BalanceStatisticResult balanceStatisticResult = new BalanceStatisticResult();
        balanceStatisticResult.setOrgId(1);
        balanceStatisticResult.setProductId(1);
        balanceStatisticResult.setDate(date);
        balanceStatisticResult.setBalance(new BigDecimal("10000"));
        return balanceStatisticResult;
    }
}
