package org.adyl.views.me;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.security.RolesAllowed;
import org.adyl.model.Order;
import org.adyl.repository.OrderRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;
import org.adyl.views.OrderItemsDetail;

import java.util.List;

@Route("orders/me")
//@PermitAll
@RolesAllowed("ROLE_USER")
public class OrdersMeView extends FlexLayout {
    private AuthenticationService authenticationService;
    private OrderRepository orderRepository;
    private StoreUserDetails principal;
    private Button home;
    private FlexLayout ordersList;
    private List<Order> orders;

    public OrdersMeView(AuthenticationService authenticationService, OrderRepository orderRepository){
        this.orderRepository = orderRepository;
        this.authenticationService = authenticationService;

        getElement().getThemeList().add(Lumo.DARK);
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setAlignItems(Alignment.CENTER);

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();

        orders = orderRepository.findAllByCustomer(principal.getUser().getCustomer());
//        System.out.println(orders);

        H1 header = new H1("Orders:");
        header.getStyle().setMarginTop("20px");

        ordersList = new FlexLayout();
        ordersList.setFlexDirection(FlexDirection.COLUMN);
        ordersList.setAlignItems(Alignment.CENTER);
        ordersList.setWidth("max-content");
        ordersList.getStyle().setMarginTop("30px");

        home = new Button("Home", event -> {
            getUI().ifPresent(ui -> ui.navigate("/"));
        });
        home.getStyle().setMarginTop("20px");
        add(header, ordersList, home);

        drawOrders(ordersList);
    }

    private void drawOrders(FlexComponent content) {
        if (orders.isEmpty()) {
            if (principal.getUser().getRoles().contains("ROLE_USER"))
                content.add(new H3(new Span("You haven't ordered anything yet! "), new Anchor("/books", "Choose something!")));
            else
                content.add(new H3("You haven't ordered anything!"));
        } else {
            content.setWidth("70%");
            Grid<Order> ordersGrid = new Grid<>(Order.class, false);

            ordersGrid.addColumn(Order::getOrderDate).setHeader("Order date");
            ordersGrid.addColumn(order -> order.getOrderValue()+" MDL").setHeader("Order value");

            ordersGrid.setItemDetailsRenderer(new ComponentRenderer<OrderItemsDetail, Order>(OrderItemsDetail::new, OrderItemsDetail::setOrder));
            ordersGrid.addColumn(LitRenderer.<Order> of("<vaadin-button theme=\"tertiary\" @click=\"${show}\">Toggle details</vaadin-button>").withFunction("show", order -> ordersGrid.setDetailsVisible(order, !ordersGrid.isDetailsVisible(order)))).setHeader("Order items"); //@ - indicates, that it is not just a value - it is a function
            ordersGrid.setDetailsVisibleOnClick(false);

            ordersGrid.setItems(orders);
//            ordersGrid.setHeight("400px");
            ordersGrid.setAllRowsVisible(true);
            ordersGrid.setWidth("80%");
            content.setAlignSelf(Alignment.CENTER, ordersGrid);
            content.add(ordersGrid);
//            System.out.println(cart.getBookMap().keySet());
        }
//        add(new H3(cart.toString()));
    }
}
