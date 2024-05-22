package jpabook.jpashop.serivce;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.Exception.NotEnoughStockException;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Item.Book;
import jpabook.jpashop.domain.Item.Item;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderService orderService;

    @Test
    void 상품주문() throws Exception{
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);
        //when
        int orderCount = 2;
        Long orderedId = orderService.order(member.getId(), book.getId(), orderCount);
        em.flush();
        //then
        Order order = orderRepository.findOne(orderedId);
        assertThat(OrderStatus.ORDER).isEqualTo(order.getStatus());
        assertThat(1).isEqualTo(order.getOrderItems().size());
        assertThat(book.getPrice() * orderCount).isEqualTo(order.getTotalPrice());
        assertThat(8).isEqualTo(book.getStockQuantity());
    }



    @Test
    void 주문취소() throws Exception{
        //given
        Member member = createMember();
        Book book = createBook("시골 JPA", 10000, 10);
        int orderCount = 2;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        orderService.cancelOrder(orderId);
        //then
        Order order = orderRepository.findOne(orderId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL);
        assertThat(book.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void 재고수량초과() throws Exception{
        //given
        Member member = createMember();
        Item item = createBook("시골 JPA", 10000, 10);
        int orderCount = 11;
        //when
        em.flush();
        assertThatExceptionOfType(NotEnoughStockException.class).isThrownBy(()->{
            orderService.order(member.getId(), item.getId(), orderCount);
        }).withMessage("need more stock");
        //then
    }

    private Book createBook(String name, int price, int quantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        em.persist(member);
        return member;
    }
}