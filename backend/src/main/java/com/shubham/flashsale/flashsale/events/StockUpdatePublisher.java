package com.shubham.flashsale.flashsale.events;

public interface StockUpdatePublisher {

  void publish(StockUpdateEvent event);
}
