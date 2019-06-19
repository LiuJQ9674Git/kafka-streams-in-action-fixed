package bbejeck.model;

import java.util.Date;
import java.util.List;

/**
 * 相互关联购买单据
 */
public class CorrelatedPurchase {

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 购买清单
     */
    private List<String> itemsPurchased;

    /**
     * 总额
     */
    private double totalAmount;

    /**
     * 首次购买时间
     */
    private Date firstPurchaseTime;

    /**
     * 第二次购买时间
     */
    private Date secondPurchaseTime;

    private CorrelatedPurchase(Builder builder) {
        customerId = builder.customerId;
        itemsPurchased = builder.itemsPurchased;
        totalAmount = builder.totalAmount;
        firstPurchaseTime = builder.firstPurchasedItem;
        secondPurchaseTime = builder.secondPurchasedItem;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<String> getItemsPurchased() {
        return itemsPurchased;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Date getFirstPurchaseTime() {
        return firstPurchaseTime;
    }

    public Date getSecondPurchaseTime() {
        return secondPurchaseTime;
    }


    @Override
    public String toString() {
        return "CorrelatedPurchase{" +
                "customerId='" + customerId + '\'' +
                ", itemsPurchased=" + itemsPurchased +
                ", totalAmount=" + totalAmount +
                ", firstPurchaseTime=" + firstPurchaseTime +
                ", secondPurchaseTime=" + secondPurchaseTime +
                '}';
    }

    public static final class Builder {
        private String customerId;
        private List<String> itemsPurchased;
        private double totalAmount;
        private Date firstPurchasedItem;
        private Date secondPurchasedItem;

        private Builder() {
        }

        public Builder withCustomerId(String val) {
            customerId = val;
            return this;
        }

        public Builder withItemsPurchased(List<String> val) {
            itemsPurchased = val;
            return this;
        }

        public Builder withTotalAmount(double val) {
            totalAmount = val;
            return this;
        }

        public Builder withFirstPurchaseDate(Date val) {
            firstPurchasedItem = val;
            return this;
        }

        public Builder withSecondPurchaseDate(Date val) {
            secondPurchasedItem = val;
            return this;
        }

        public CorrelatedPurchase build() {
            return new CorrelatedPurchase(this);
        }
    }
}
