package com.example.Refi;

import ch.qos.logback.core.util.TimeUtil;
import com.webcerebrium.binance.api.BinanceApi;
import com.webcerebrium.binance.api.BinanceApiException;
import com.webcerebrium.binance.datatype.*;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/trade")
public class TradingController {

    private static final String CURRENCY = "ETHUSDT";

    private boolean IS_BUY = true;

    private double BUY_LINE = 0.5;
    private double PROFIT = 0.3;
    private double STOP = -2.0;

    private static BigDecimal LAST_PRICE;
    private static BigDecimal USD_VALUE;
    private static BigDecimal ETH_VALUE;

    private static BigDecimal USD_START;
    private static BigDecimal USD_FINISH;

    private static BinanceApi api = new BinanceApi();

    static {
        try {
            LAST_PRICE = api.pricesMap().get(CURRENCY);
            USD_VALUE = api.balancesMap().get("USDT").getFree();
            ETH_VALUE = api.balancesMap().get("ETH").getFree();

            USD_START = api.balancesMap().get("USDT").getFree();
        } catch (BinanceApiException e) {
            e.printStackTrace();
        }
    }

    @GetMapping()
    public String startTrading(@NonNull @RequestParam String line,
                               @NonNull @RequestParam String profit,
                               @NonNull @RequestParam String stop,
                               @NonNull @RequestParam int counts) throws BinanceApiException{
        BUY_LINE = Double.parseDouble(line);
        PROFIT = Double.parseDouble(profit);
        STOP = Double.parseDouble(stop);
        int count = 0;
        while (count<counts){
            if(tryTrade()){
                count++;
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        USD_FINISH = api.balancesMap().get("USDT").getFree();
        return "Profit" + (USD_FINISH.doubleValue()-USD_START.doubleValue());

    }

    private boolean tryTrade() throws BinanceApiException {
        BigDecimal currPrice = api.pricesMap().get(CURRENCY);
        double diff = LAST_PRICE.doubleValue() - currPrice.doubleValue();
        System.out.println("price " + currPrice);
        System.out.println("diff " + diff);
        if(IS_BUY){
            return tryBuy(diff, currPrice.doubleValue());
        }else {
            return trySell(-diff, currPrice.doubleValue());
        }
    }

    private boolean tryBuy(double diff, double price) throws BinanceApiException {
        if(diff>BUY_LINE){
            placeBuyOrder(price);
            IS_BUY = false;
            return true;
        } else if(diff<0.0){
            LAST_PRICE = new BigDecimal(price);
        }
        return false;
    }

    private void placeBuyOrder(double price) throws BinanceApiException {
        System.out.println("[ORDER]");
        USD_VALUE = api.balancesMap().get("USDT").getFree();

        BinanceSymbol symbol = new BinanceSymbol(CURRENCY);
        BinanceOrderPlacement placement = new BinanceOrderPlacement(symbol, BinanceOrderSide.BUY);
        placement.setType(BinanceOrderType.MARKET);
        placement.setPrice(BigDecimal.valueOf(price));
        long quantity = Double.valueOf(USD_VALUE.intValue()/price * 100000).longValue();
        placement.setQuantity(BigDecimal.valueOf(quantity, 5));

        System.out.println("quantity "+placement.getQuantity());
        System.out.println("USD" + USD_VALUE);

        BinanceOrder order = api.getOrderById(symbol, api.createOrder(placement).get("orderId").getAsLong());
        System.out.println(order.toString());
        LAST_PRICE = new BigDecimal(price);
    }

    private boolean trySell(double diff, double price) throws BinanceApiException {
        if(diff>=PROFIT){
            placeSellOrder(price);
            IS_BUY = true;
            return true;
        } else if(diff<STOP){
            placeSellOrder(price-diff);
            LAST_PRICE = new BigDecimal(price);
            IS_BUY = true;
            return true;
        }
        return false;
    }

    private void placeSellOrder(double price) throws BinanceApiException {
        System.out.println("[ORDER]");
        ETH_VALUE = api.balancesMap().get("ETH").getFree();

        BinanceSymbol symbol = new BinanceSymbol(CURRENCY);
        BinanceOrderPlacement placement = new BinanceOrderPlacement(symbol, BinanceOrderSide.SELL);
        placement.setType(BinanceOrderType.MARKET);
        placement.setPrice(BigDecimal.valueOf(price));
        long quantity = Double.valueOf(ETH_VALUE.doubleValue()* 100000).longValue();
        placement.setQuantity(BigDecimal.valueOf(quantity, 5));

        System.out.println("quantity "+placement.getQuantity());
        System.out.println("ETH" + ETH_VALUE);

        try {
            BinanceOrder order = api.getOrderById(symbol, api.createOrder(placement).get("orderId").getAsLong());
            System.out.println(order.toString());
        } catch (BinanceApiException e){
            System.out.println(e.getMessage());
        }
        LAST_PRICE = new BigDecimal(price);
    }
}
