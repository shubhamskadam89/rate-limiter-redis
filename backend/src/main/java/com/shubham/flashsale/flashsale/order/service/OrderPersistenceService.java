package com.shubham.flashsale.flashsale.order.service;

import com.shubham.flashsale.flashsale.order.queue.OrderQueueMessage;

public interface OrderPersistenceService {

    void persist(OrderQueueMessage message);

}