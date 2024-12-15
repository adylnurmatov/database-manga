package org.adyl.views;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import org.adyl.model.OrderItem;

public class OrderItemView extends FlexLayout {
    private Div book = new Div(new Span("Books: "));
    private Div itemAgreedPrice = new Div(new Span("Price: "));
    private Div comment = new Div(new Span("Comment: "));

    public OrderItemView() {
//        itemAgreedPrice.getStyle().setMarginTop("10px");
//        comment.getStyle().setMarginTop("10px");
        add(book, itemAgreedPrice, comment);
        setFlexDirection(FlexLayout.FlexDirection.ROW);
        setWidth("100%");
        setJustifyContentMode(JustifyContentMode.BETWEEN);
    }

    public OrderItemView(OrderItem orderItem) {
        this();
        setOrderItem(orderItem);
    }

    public void setOrderItem(OrderItem orderItem) {
        book.add(new Anchor("/books/" + orderItem.getBook().getId(), orderItem.getBook().getTitle()));
        itemAgreedPrice.add(new Span(orderItem.getItemAgreedPrice().toString()));
        comment.add(new Span(orderItem.getItemComment()));
    }
}
