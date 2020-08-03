package com.example.Refi;

import com.webcerebrium.binance.api.BinanceApi;
import com.webcerebrium.binance.api.BinanceApiException;
import com.webcerebrium.binance.datatype.BinanceSymbol;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/trade")
public class TradingController {

    @GetMapping()
    public void startTrading() throws BinanceApiException {
        BinanceApi api = new BinanceApi();

        System.out.println(api.pricesMap().get("ETHUSDT"));
        System.out.println(api.account());
    }
}
