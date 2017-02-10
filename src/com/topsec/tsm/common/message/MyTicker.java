package com.topsec.tsm.common.message;

import java.util.List;

import com.topsec.tsm.util.ticker.Ticker;
import com.topsec.tsm.util.ticker.Tickerable;
public class MyTicker extends Ticker {

	public void setTickers(List<Tickerable> tickers){
		super.clear() ;
		for(Tickerable t:tickers){
			addTicker(t) ;
		}
	}
	
}
