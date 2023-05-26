package abstract_factory_solve_balance_problem;


import java.math.BigDecimal;

public class BalanceStatisticResult {
    /**
     * 余额
     */
    private BigDecimal balance;
    /**
     * 统计截止日期
     */
    private String date;
    /**
     * 资方id
     */
    private Integer orgId;
    /**
     * 产品id
     */
    private Integer productId;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "BalanceStatisticResult{" +
                "balance=" + balance +
                ", date='" + date + '\'' +
                ", orgId=" + orgId +
                ", productId=" + productId +
                '}';
    }
}
