import com.forex.jExpertAdvisor.candles.Candle;
import com.forex.jExpertAdvisor.main.MarketMgr;
import com.forex.jExpertAdvisor.stoplosses.MovingStopLoss;
import com.forex.jExpertAdvisor.stoplosses.StopLoss;
import com.forex.jExpertAdvisor.trades.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class PivotPoint extends IStrategy {


    private BigDecimal pivotPoint;
    private BigDecimal R1;
    private BigDecimal R2;
    private BigDecimal S1;
    private BigDecimal S2;



    public boolean isThisStrategyTradeType(TradeType tradeType){
        for(Trade trade: ExistingTrades.getInstance()){
            if (trade.getStrategy().equals(this)&&trade.getType().equals(tradeType))
                return true;
        }
        return false;
    }

    private void calculatePivotPointSupportAndResistance(){
       List<Candle> lastweek = MarketMgr.getInstance(this.getSymbol()).getHistoricView().subList(MarketMgr.getInstance(this.getSymbol()).getHistoricView().size()-(7*24+1), MarketMgr.getInstance(this.getSymbol()).getHistoricView().size()-1);
       BigDecimal min = Collections.min(lastweek.stream().map(Candle::getLow).collect(Collectors.toList()));
       BigDecimal max = Collections.max(lastweek.stream().map(Candle::getHigh).collect(Collectors.toList()));
       BigDecimal close = lastweek.get(lastweek.size()-1).getClose();
       this.pivotPoint = (min.add(max.add(close))).divide(new BigDecimal(3), RoundingMode.HALF_UP);
       this.R1 = pivotPoint.multiply(new BigDecimal(2)).subtract(min);
       this.S1 = pivotPoint.multiply(new BigDecimal(2)).subtract(max);
       this.R2 = pivotPoint.add(max.subtract(min));
       this.S2 = pivotPoint.add(max.subtract(min));
    }



    public void OnInit() {

    }

    public void OnDenit() {

    }

    public void OnStart() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("u-HH:mm");
       String[] strings = dateFormat.format(MarketMgr.getInstance(getSymbol()).getCurrentCandle().getDate()).split("-");



        if(strings[0].equals("1")) {
            for (int j = ExistingTrades.getInstance().size() - 1; j == 0; j--) {
                if (ExistingTrades.getInstance().get(j).getStrategy().equals(this)) {
                    try {
                        TradeMgr.getInstance().close(ExistingTrades.getInstance().get(j));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            calculatePivotPointSupportAndResistance();


            if (MarketMgr.getInstance(getSymbol()).getAsk().compareTo(R1) > 0 && !isThisStrategyTradeType(TradeType.BUY)) {
                TradeMgr.getInstance().open(this, new StopLoss(pivotPoint), TradeType.BUY, getSymbol(), getSize(), getAccount());


            } else if (MarketMgr.getInstance(getSymbol()).getAsk().compareTo(S1) < 0 && !isThisStrategyTradeType(TradeType.SELL)) {
                TradeMgr.getInstance().open(this, new StopLoss(pivotPoint), TradeType.SELL, getSymbol(), getSize(), getAccount());

            }
        }

    }
}
