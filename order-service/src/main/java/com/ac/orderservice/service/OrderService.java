package com.ac.orderservice.service;

import com.ac.orderservice.dto.OrderLineItemsDto;
import com.ac.orderservice.dto.OrderRequest;
import com.ac.orderservice.model.Order;
import com.ac.orderservice.model.OrderLineItems;
import com.ac.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        order.setOrderLineItemsList(orderLineItemsList);

        orderRepository.save(order);

    }

    private OrderLineItems mapToDto(OrderLineItemsDto line) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(line.getPrice());
        orderLineItems.setQuantity(line.getQuantity());
        orderLineItems.setSkuCode(line.getSkuCode());
        return orderLineItems;
    }
}
