package com.example.Refi;

import com.webcerebrium.binance.api.BinanceApi;
import com.webcerebrium.binance.api.BinanceApiException;
import com.webcerebrium.binance.datatype.*;
import lombok.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "/trade")
public class TradingController {

    private static final String CURRENCY = "ETHUSDT";

    private boolean IS_BUY = true;

    private double BUY_LINE = 1.0;
    private double PROFIT = 1.0;
    private double STOP = -4.0;

    private static BigDecimal LAST_PRICE;
    private static BigDecimal MIN_PRICE;
    private static BigDecimal MAX_PRICE;

    private static BigDecimal USD_VALUE;
    private static BigDecimal ETH_VALUE;

    private static BigDecimal USD_START;
    private static BigDecimal USD_FINISH;

    private static BinanceApi api = new BinanceApi();

    static {
        try {
            api.setApiKey("7wv1F00xPh7RjAMPqS5TODuwHxeD1kGXgvApkzZSMxfnHaUvECeJbcYmkQGHGXQ6");
            api.setSecretKey("rwRVcUzvvrMTY2YtyEV5QjoKpRm7Z2PPzKyS6t3jTupLMtuTTeTvMmL1xUivWeqj");

            LAST_PRICE = api.pricesMap().get(CURRENCY);
            MIN_PRICE = api.pricesMap().get(CURRENCY);
            MAX_PRICE = api.pricesMap().get(CURRENCY);
            USD_VALUE = api.balancesMap().get("USDT").getFree();
            ETH_VALUE = api.balancesMap().get("ETH").getFree();

            USD_START = USD_VALUE;
        } catch (BinanceApiException e) {
            e.printStackTrace();
        }
    }

    @GetMapping()
    public String startTrading(@NonNull @RequestParam int counts,
                               @NotNull @RequestParam boolean isBuy) throws BinanceApiException{
        IS_BUY = isBuy;
        LAST_PRICE = api.pricesMap().get(CURRENCY);
        MIN_PRICE = api.pricesMap().get(CURRENCY);
        MAX_PRICE = api.pricesMap().get(CURRENCY);

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

        MIN_PRICE = MIN_PRICE.min(currPrice);
        MAX_PRICE = MAX_PRICE.max(currPrice);

        double diff = LAST_PRICE.doubleValue() - currPrice.doubleValue();
        System.out.println("Current price " + currPrice);
        System.out.println(IS_BUY ? "Min "+MIN_PRICE :"Max"+MAX_PRICE);
        if(IS_BUY){
            return tryBuy(currPrice.doubleValue());
        }else {
            return trySell(-diff, currPrice.doubleValue());
        }
    }

    private boolean tryBuy(double price) throws BinanceApiException {
        if(price - MIN_PRICE.doubleValue() >= BUY_LINE){
            placeBuyOrder(price);
            IS_BUY = false;
            return true;
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

        System.out.println("quantity "+placement.getQuantity() + ", USD" + USD_VALUE);
        try {
            api.createOrder(placement).get("orderId").getAsLong();
        } catch (BinanceApiException e){
            System.out.println(e.getMessage());
        }
        LAST_PRICE = new BigDecimal(price);
        MAX_PRICE = LAST_PRICE;
    }

    private boolean trySell(double diff, double price) throws BinanceApiException {
        if(MAX_PRICE.doubleValue() - price > PROFIT && price - LAST_PRICE.doubleValue()>PROFIT-0.3){
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

        System.out.println("quantity "+placement.getQuantity() + ", USD" + USD_VALUE);
        try {
            api.createOrder(placement).get("orderId").getAsLong();
        } catch (BinanceApiException e){
            System.out.println(e.getMessage());
        }
        LAST_PRICE = new BigDecimal(price);
        MIN_PRICE = LAST_PRICE;
    }
}
