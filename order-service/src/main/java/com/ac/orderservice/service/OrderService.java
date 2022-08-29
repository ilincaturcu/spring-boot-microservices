package com.ac.orderservice.service;

import com.ac.orderservice.dto.OrderLineItemsDto;
import com.ac.orderservice.dto.OrderRequest;
import com.ac.orderservice.model.Order;
import com.ac.orderservice.model.OrderLineItems;
import com.ac.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        order.setOrderLineItemsList(orderLineItemsList);

        Boolean result = webClient.get()
                .uri("http://localhost:8082/api/inventory")
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

        if(result){
            orderRepository.save(order);
        }
        else{
            throw  new IllegalArgumentException("Product is not in stock");
        }


    }

    private OrderLineItems mapToDto(OrderLineItemsDto line) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(line.getPrice());
        orderLineItems.setQuantity(line.getQuantity());
        orderLineItems.setSkuCode(line.getSkuCode());
        return orderLineItems;
    }
}
