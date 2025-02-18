package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager entityManager;

    public void save(Order order) {
        entityManager.persist(order);
    }

    public Order findOne(Long id) {
        return entityManager.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        String jpql = "select o from  Order o join o.member m";
        boolean isFirstCondition = true;
        if (orderSearch.getStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            }
        } else {
            jpql += " o.status = :status";
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " m.name like :name";
            }
        }

        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if (orderSearch.getStatus() != null) {
            query = query.setParameter("status", orderSearch.getStatus());
        }
        if (orderSearch.getMemberName() != null) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    // jpa criteria
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> from = cq.from(Order.class);
        Join<Object, Object> member = from.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<Predicate>();

        if (orderSearch.getStatus() != null) {
            Predicate status = cb.equal(from.get("status"), orderSearch.getStatus());
            criteria.add(status);
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(member.get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[0])));
        TypedQuery<Order> query = entityManager.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    public List<Order> findAll() {
        return entityManager.createQuery("select o from Order o", Order.class).getResultList();
    }
}
