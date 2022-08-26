package com.ac.orderservice;

import com.ac.orderservice.dto.OrderLineItemsDto;
import com.ac.orderservice.dto.OrderRequest;
import com.ac.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;

    @Container
    public static MySQLContainer<?> mySqlDB = new MySQLContainer<>
            ("mysql:5.7.37")
            .withDatabaseName("orderservicetest")
            .withUsername("root")
            .withPassword("Ilinca-113473");


    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySqlDB::getJdbcUrl);
        registry.add("spring.datasource.username", mySqlDB::getUsername);
        registry.add("spring.datasource.password", mySqlDB::getPassword);

    }

    @Test
    void shouldAddOrder() throws Exception {
        OrderRequest orderRequest = getOrderRequest();
        String orderRequestString = objectMapper.writeValueAsString(orderRequest);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderRequestString))
                .andExpect(status().isCreated());
        Assertions.assertEquals(1, orderRepository.findAll().size());

    }

    private OrderRequest getOrderRequest() {
        return OrderRequest.builder()
                .orderLineItemsDtoList(getOrderLineItemsDtoList())
                .build();
    }

    private List<OrderLineItemsDto> getOrderLineItemsDtoList() {

        OrderLineItemsDto order = OrderLineItemsDto.builder()
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1344))
                .quantity(1)
                .build();
        List<OrderLineItemsDto> orderLineItemsDtoList = new ArrayList<>();
        orderLineItemsDtoList.add(order);
        return orderLineItemsDtoList;
    }

}
