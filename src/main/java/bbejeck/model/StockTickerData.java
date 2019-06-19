package bbejeck.model;

/**
 * 股票行情数据
 */
public class StockTickerData {

    /**
     * 价格
     */
    private double price;

    /**
     * 交易码
     */
    private  String symbol;

    public double getPrice() {
        return price;
    }

    public String getSymbol() {
        return symbol;
    }

    public StockTickerData(double price, String symbol) {
        this.price = price;
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "StockTickerData{" +
                "price=" + price +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
