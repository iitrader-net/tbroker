## Test Quote
```
$>quote tbroker.QuoteII acc_pass,http://i2trader.com:5691,<token>
$>bind SPY
$>lsq 
```

## Test WebSocket Quote
```
$>quote tbroker.QuoteIIWS acc_pass,ws://i2trader.com:5693,<token>
$>bind SPY
$>lsq 
```

## Test Order
```
$>broker tbroker.QuoteII acc_pass,http://i2trader.com:5691,<token>
$>order order SPY 1 200 aaa 0
roid=xxxx
$>cancel xxxx
```


