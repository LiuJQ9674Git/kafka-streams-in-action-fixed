package bbejeck.model;

/**
 * 客户交易
 */
public class CustomerTransactions {

    /**
     * 会话信息
     */
    private String sessionInfo;

    /**
     * 总价
     */
    private double totalPrice = 0;

    /**
     * 总份额
     */
    private long totalShares = 0;


    public CustomerTransactions update(StockTransaction stockTransaction) {
        totalShares += stockTransaction.getShares();
        totalPrice += stockTransaction.getSharePrice() * stockTransaction.getShares();

        return this;
    }

    @Override
    public String toString() {
        return "avg txn=" + totalPrice / totalShares + " sessionInfo='" + sessionInfo;
    }

    public void setSessionInfo(String sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public CustomerTransactions merge(CustomerTransactions other) {
        this.totalShares += other.totalShares;
        this.totalPrice += other.totalPrice;
        this.sessionInfo = other.sessionInfo;
        return this;
    }
}
