package bbejeck.model;

import java.util.Date;
import java.util.Objects;

/**
 * 购物Key
 */
public class PurchaseKey {

    /**
     * 顾客ID
     */
    private String customerId;

    /**
     * 交易日期
     */
    private Date transactionDate;

    public PurchaseKey(String customerId, Date transactionDate) {
        this.customerId = customerId;
        this.transactionDate = transactionDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PurchaseKey)) return false;
        PurchaseKey that = (PurchaseKey) o;
        return Objects.equals(customerId, that.customerId) &&
                Objects.equals(transactionDate, that.transactionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, transactionDate);
    }
}
