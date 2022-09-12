package com.ac.orderservice.service;

import com.ac.orderservice.dto.InventoryResponse;
import com.ac.orderservice.dto.OrderLineItemsDto;
import com.ac.orderservice.dto.OrderRequest;
import com.ac.orderservice.model.Order;
import com.ac.orderservice.model.OrderLineItems;
import com.ac.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        order.setOrderLineItemsList(orderLineItemsList);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .collect(Collectors.toList());

        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory", uriBuilder->uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        //check that all the products required in order are available in stock, then save the order
       boolean allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);

        if(allProductsInStock){
            orderRepository.save(order);
            return "Order placed successfully!";
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
