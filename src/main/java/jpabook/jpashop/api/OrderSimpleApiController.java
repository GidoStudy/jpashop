package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import jpabook.jpashop.repository.order.simplequery.SimpleOrderQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * xTonOne : 컬렉션 관계 X
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /* Order Member 서로 참조하면서 무한 루프에 빠진다
     한쪽은 JsonIgnore를 해야되는데 또 엔티티에 프레젠테이션 계층 관련 로직이 추가된다
     그리고 500이 난다 -> 지연 로딩을 하기 때문에 Order만 가져오고 Member는 bytebuddy라는 프록시 객체를
     박아 넣어 놓고 member에 직접 접근할때 엔티티를 가져옴 여기서 양방향으로 엮어서 가져오려다 보니
     프록시 객체에 접근하게되고 그러면서 500이 터진다
     해결법 : Hibernate5Module 추가로 프록시 객체에 대해서는 아무것도 안하게 만들어벌임 ! ㅋ
     근데 어차피 엔티티 반환이라 쓰레기 코드임 이렇게 할 일도 없다
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> orderV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName(); // Lazy 강제 초기화
            order.getDelivery().getStatus(); //Lazy 강제 초기화
        }
        return all;
        }
    // Dto를 반환하도록 변경
    // 여기서도 지연 로딩에 의해 쿼리가 너무 많이 나감 -> 성능 하락 N+1 문제 발생
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> orderV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream()
                .map(SimpleOrderDto::new)
                .toList();
    }
    // fetch join 으로 지연로딩에 관한 성능 최적화
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3(){
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream()
                .map(SimpleOrderDto::new)
                .toList();
    }

    @GetMapping("/api/v4/simple-orders")
    public List<SimpleOrderQueryDto> orderV4(){
       return orderSimpleQueryRepository.findOrderDtos();
    }
/*
    쿼리 방식 선택 권장 순서
 1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
 2. 필요하면 페치 조인으로 성능을 최적화 한다.  대부분의 성능 이슈가 해결된다.
 3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
 4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사용한다
 */
    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }

    }

