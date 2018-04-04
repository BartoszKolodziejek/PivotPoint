import com.forex.jExpertAdvisor.candles.Candle;
import com.forex.jExpertAdvisor.main.MarketMgr;
import com.forex.jExpertAdvisor.stoplosses.MovingStopLoss;
import com.forex.jExpertAdvisor.trades.ExistingTrades;
import com.forex.jExpertAdvisor.trades.IStrategy;
import com.forex.jExpertAdvisor.trades.TradeMgr;
import com.forex.jExpertAdvisor.trades.TradeType;

import java.math.BigDecimal;
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
    private BigDecimal S1;


    private void calculatePivotPointSupportAndResistance(){
       List<Candle> last24h = MarketMgr.getInstance(this.getSymbol()).getHistoricView().subList(MarketMgr.getInstance(this.getSymbol()).getHistoricView().size()-25, MarketMgr.getInstance(this.getSymbol()).getHistoricView().size()-1);
       BigDecimal min = Collections.min(last24h.stream().map(Candle::getLow).collect(Collectors.toList()));
       BigDecimal max = Collections.max(last24h.stream().map(Candle::getHigh).collect(Collectors.toList()));
       BigDecimal close = last24h.get(last24h.size()-1).getClose();
       this.pivotPoint = (min.add(max.add(close))).divide(new BigDecimal(3));
       this.R1 = pivotPoint.multiply(new BigDecimal(2)).subtract(min);
       this.S1 = pivotPoint.multiply(new BigDecimal(2)).subtract(max);
    }



    public void OnInit() {

    }

    public void OnDenit() {

    }

    public void OnStart() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("u-HH:mm");
       String[] strings = dateFormat.format(MarketMgr.getInstance(getSymbol()).getCurrentCandle().getDate()).split("-");



        if(strings[1].equals("00:00")&& !strings[0].equals("1")&&!strings[0].equals("7")){
           for (int j=ExistingTrades.getInstance().size()-1; j==0; j--){
               if (ExistingTrades.getInstance().get(j).getStrategy().equals(this)) {
                   try {
                       TradeMgr.getInstance().close(ExistingTrades.getInstance().get(j));
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }
            }
            calculatePivotPointSupportAndResistance();


        if(MarketMgr.getInstance(getSymbol()).getAsk().compareTo(pivotPoint)>0){
            TradeMgr.getInstance().open(this,  new MovingStopLoss(pivotPoint.subtract(getPoint().multiply(new BigDecimal(10))), new BigDecimal(100)), TradeType.BUY, getSymbol() );


        }
        else if(MarketMgr.getInstance(getSymbol()).getAsk().compareTo(pivotPoint)<0){
            TradeMgr.getInstance().open(this,  new MovingStopLoss(pivotPoint.add(getPoint().multiply(new BigDecimal(10))), new BigDecimal(100)), TradeType.SELL, getSymbol() );

        }

    }
    }
}
