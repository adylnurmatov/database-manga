package org.adyl.model;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Component;

import java.util.TreeMap;

@Component
@VaadinSessionScope
public class Cart {
    private TreeMap<Book, Integer> bookMap;

    public Cart(TreeMap<Book, Integer> bookMap) {
        this.bookMap = bookMap;
    }

    public Cart() {
        this.bookMap = new TreeMap<>((b1, b2) -> {
            return b1.getId().compareTo(b2.getId());
        });
    }

    public TreeMap<Book, Integer> getBookMap() {
        return bookMap;
    }

    public void setBookMap(TreeMap<Book, Integer> bookMap) {
        this.bookMap = bookMap;
    }

    public Double getTotal() {
        return bookMap.keySet().stream().map(book -> book.getRecommendedPrice()*bookMap.get(book)).reduce((d1, d2) -> d1+d2).orElse(0D);
    }

    @Override
    public String toString() {
        return "Cart{" +
                "bookList=" + bookMap +
                '}';
    }
}
