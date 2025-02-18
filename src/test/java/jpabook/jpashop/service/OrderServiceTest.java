package jpabook.jpashop.service;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    EntityManager entityManager;

    @Test
    void 상품주분() {
        //given
        Member member = createMember();

        Item book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        // when

        Long order = orderService.order(member.getId(), book.getId(), orderCount);

        Order getOrder = orderRepository.findOne(order);

        // then
        assertEquals(OrderStatus.ORDER, getOrder.getStatus(), "상품 주문시 상태는 ORDER");
        assertEquals(1, getOrder.getOrderItems().size(), "주문한 상품 종류 수가 정확해야 한다.");
        assertEquals(10_000 * orderCount, getOrder.getTotalPrice(), "주문 가격은 가격 & 수량이다.");
        assertEquals(8, book.getStockQuantity(), "주문 수량만큼 재고가 줄어야 한다.");
    }

    private Item createBook(String name, int price, int quantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(quantity);
        entityManager.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "강가", "123-123"));
        entityManager.persist(member);
        return member;
    }

    @Test
    void 주문취소() {
        //given
        Member member = createMember();

        Item book = createBook("시골 JPA", 10000, 10);
        int orderCount = 2;
        Long order = orderService.order(member.getId(), book.getId(), orderCount);
        // when
        orderService.cancelOrder(order);
        // then
        Order getOrder = orderRepository.findOne(order);

        assertEquals(OrderStatus.CANCELLED, getOrder.getStatus(), "주문 취소 시 상태는 CANCEL이다");
        assertEquals(10, book.getStockQuantity(), "주문이 취소된 상품은 그만큼 재고가 증가해야한다.");
    }

    @Test
    void 상품주문_재고수량초과() {
        //given
        Member member = createMember();

        Item book = createBook("시골 JPA", 10000, 10);
        int orderCount = 11;
        // when

        Throwable throwable = Assertions.catchThrowable(
                () -> orderService.order(member.getId(), book.getId(), orderCount));

        // then
        Assertions.assertThat(throwable)
                .isInstanceOf(NotEnoughStockException.class);
    }
}