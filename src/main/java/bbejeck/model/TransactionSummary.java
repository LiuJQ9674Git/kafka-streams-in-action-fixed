package bbejeck.model;

/**
 * 交易摘要
 */
public class TransactionSummary {

    /**
     * 客户ID
     */
    private String customerId;

    /**
     * 债券报价机
     */
    private String stockTicker;

    /**
     * 行业
     */
    private String industry;

    /**
     * 总额
     */
    private long summaryCount;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 公司名
     */
    private String companyName;


    public TransactionSummary(String customerId, String stockTicker, String industry) {
        this.customerId = customerId;
        this.stockTicker = stockTicker;
        this.industry = industry;
    }

    public void setSummaryCount(long summaryCount){
        this.summaryCount = summaryCount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getStockTicker() {
        return stockTicker;
    }

    public String getIndustry() {
        return industry;
    }

    public long getSummaryCount() {
        return summaryCount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public TransactionSummary withCustomerName(String customerName) {
        this.customerName = customerName;
        return this;
    }

    public String getCompmanyName() {
        return companyName;
    }

    public TransactionSummary withCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public static TransactionSummary from(StockTransaction transaction){
        return new TransactionSummary(transaction.getCustomerId(), transaction.getSymbol(), transaction.getIndustry());
    }

    @Override
    public String toString() {
        return "TransactionSummary{" +
                "customerId='" + customerId + '\'' +
                ", stockTicker='" + stockTicker + '\'' +
                ", customerName='" + customerName + '\'' +
                ", companyName='" + companyName + '\'' +
                '}';
    }
}
