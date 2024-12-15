package org.adyl.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import org.adyl.model.Book;
import org.adyl.model.Order;
import org.adyl.model.OrderItem;
import org.adyl.repository.BookRepository;
import org.adyl.repository.OrderItemRepository;
import org.adyl.repository.OrderRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;
import org.adyl.utils.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class OrderItemsDetail extends VerticalLayout {
//    private Book book;
//    private Double itemAgreedPrice;
//    private String comment;
    private OrderItemRepository orderItemRepository;
    private OrderRepository orderRepository;
    private BookRepository bookRepository;

    private List<OrderItemView> orderItemViewList;
    private AuthenticationService authenticationService;
    private StoreUserDetails principal;
    private List<String> roles;

    public OrderItemsDetail() {
        orderItemViewList = new ArrayList<>();
        this.orderItemRepository = ApplicationContext.getContext().getBean("orderItemRepository", OrderItemRepository.class);
        this.orderRepository = ApplicationContext.getContext().getBean("orderRepository", OrderRepository.class);
        this.bookRepository = ApplicationContext.getContext().getBean("bookRepository", BookRepository.class);
        this.authenticationService = ApplicationContext.getContext().getBean("authenticationService", AuthenticationService.class);

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        roles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();
    }

    public void setOrder(Order order) {
//        System.out.println(order);
//        System.out.println(order.getItemList());
        order.getItemList().forEach(orderItem -> {
            orderItemViewList.add(new OrderItemView(orderItem));
        });
        if (!orderItemViewList.isEmpty()) {
//            orderItemViewList.forEach(orderItemView -> add(orderItemView));
            Grid<OrderItem> orderItemGrid = new Grid<>(OrderItem.class, false);

            orderItemGrid.addComponentColumn(orderItem -> {
                return new Anchor("books/" + orderItem.getBook().getId(), orderItem.getBook().getTitle());
            }).setHeader("Book").setAutoWidth(true);
            orderItemGrid.addColumn(orderItem -> orderItem.getItemAgreedPrice() + " MDL").setHeader("Price").setAutoWidth(true);
            orderItemGrid.addColumn(OrderItem::getItemComment).setHeader("Comment").setAutoWidth(true);
            orderItemGrid.addColumn(OrderItem::getAmount).setHeader("Amount").setAutoWidth(true);

            orderItemGrid.setItems(order.getItemList());
            orderItemGrid.setAllRowsVisible(true);
            add(orderItemGrid);

            if (!roles.isEmpty() && roles.contains("ROLE_ADMIN")) {
                orderItemGrid.addComponentColumn(orderItem -> {
                    Button edit = new Button("Edit");
                    edit.addClickListener(event -> {
                        Dialog editDialogue = new Dialog("Edit Order item info");

                        addAddOrEditDialogueLayout(editDialogue, orderItem, order);

                        editDialogue.open();
                    });
                    return edit;
                }).setHeader("Edit");

                orderItemGrid.addComponentColumn(orderItem -> {
                    Button delete = new Button("Delete");
                    delete.addClickListener(event -> {
                        Dialog deleteDialogue = new Dialog("Delete Order item Confirmation");

                        addDeleteDialogueLayout(deleteDialogue, orderItem, order);

                        deleteDialogue.open();
                    });

                    return delete;
                }).setHeader("Delete");
            }
            } else {
                H4 empty = new H4("Here's no items!");
                empty.getStyle().setMargin("auto");
                add(empty);
            }
        if (!roles.isEmpty() && roles.contains("ROLE_ADMIN")) {
            Button add = new Button("Add new");

            add(add);
            add.getStyle().setMarginTop("10px");
            setAlignSelf(Alignment.END, add);

            add.setWidth("max-content");
            //            add.getStyle().setMarginRight("32.5%");
            add.getStyle().setPadding("5px 10px");

            add.addClickListener(event -> {
                Dialog addDialog = new Dialog("Add new OrderItem");

                addAddOrEditDialogueLayout(addDialog, null, order);

                addDialog.open();
            });
        }
    }

    private void addAddOrEditDialogueLayout(Dialog editDialogue, OrderItem orderItem, Order order){
        Binder<OrderItem> binder = new Binder<>();

        ComboBox<Book> books = new ComboBox<>("Book", bookRepository.findAll());
        books.setItemLabelGenerator(Book::getTitle);
        books.setRenderer(getBooksComboBoxRenderer());

        NumberField price = new NumberField("Price: ");
        price.setStep(0.1);
        price.setMin(1.0);

        books.addValueChangeListener(event ->
                price.setValue(event.getSource().getValue().getRecommendedPrice()));

        TextArea comment = new TextArea("Comment");
        NumberField amount = new NumberField("Amount: ");
        amount.setStep(1.0);
        amount.setMin(1.0);
        Double oldAmount;

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel");

        buttons.add(save, cancel);

        if (orderItem != null){
            books.setValue(orderItem.getBook());
            price.setValue(orderItem.getItemAgreedPrice());
            if (orderItem.getItemComment() != null)
                comment.setValue(orderItem.getItemComment());
            amount.setValue(orderItem.getAmount()*1.0);
            oldAmount = orderItem.getAmount()*1.0;
            save.setText("Edit!");
        } else {
            oldAmount = 0.0;
        }


        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        layout.add(books, price, comment, amount, buttons);

        editDialogue.add(layout);

        save.addClickListener(event -> {
            binder.forField(books).withValidator(b -> b!=null, "Specify the book!").bind(OrderItem::getBook, OrderItem::setBook);
            binder.forField(price).withValidator(p -> p>=1, "Price must be >= 1").bind(OrderItem::getItemAgreedPrice, OrderItem::setItemAgreedPrice);
            binder.forField(amount).withValidator(a -> a>=1, "Amount must be at least 1").bind(orderItem1 -> orderItem1.getAmount().doubleValue(), (orderItem1, aDouble) -> orderItem1.setAmount(aDouble.intValue()));
            if (orderItem!=null)
                binder.forField(amount).withValidator(a -> a<=oldAmount, "You can only decrease books amount!").bind(orderItem1 -> orderItem1.getAmount().doubleValue(), (orderItem1, aDouble) -> orderItem1.setAmount(aDouble.intValue()));

            BinderValidationStatus validation = binder.validate();


            if (!validation.hasErrors()) {
                OrderItem newOrderItem = new OrderItem(order, books.getValue(), price.getValue(), comment.getValue(), amount.getValue().intValue());

                if (orderItem!=null)
                    newOrderItem.setId(orderItem.getId());

                orderItemRepository.save(newOrderItem);

                //Hibernate stores old version of order in cache. Thus we must update this local version before save it!!!
                order.setItemList(orderRepository.findFullById(order.getId()).getItemList());

                order.recalculateOrderValue();
                orderRepository.save(order);


                order.recalculateOrderValue();
                orderRepository.save(order);

                editDialogue.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            }
        });

        cancel.addClickListener(event -> editDialogue.close());
    }

    private void addDeleteDialogueLayout(Dialog dialog, OrderItem orderItem, Order order) {
        H3 question = new H3("Are you sure you want to delete this order item?");
        question.getStyle().setMarginBottom("20px");

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(delete, cancel);
        buttons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dialog.add(question, buttons);

        delete.addClickListener(event -> {
            //changing order's total price
//            order.setOrderValue(order.getOrderValue()-(orderItem.getItemAgreedPrice()*orderItem.getAmount()));
            order.getItemList().remove(orderItem);
            order.recalculateOrderValue();
            orderRepository.save(order);
//            orderItem.getOrder().setOrderValue(orderItem.getOrder().getOrderValue()-orderItem.getItemAgreedPrice());
//            orderRepository.save(orderItem.getOrder());

            orderItemRepository.delete(orderItem);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
    }

    public Renderer<Book> getBooksComboBoxRenderer() {
        StringBuilder tpl = new StringBuilder();
        tpl.append("<div style=\"display: flex;\">");
        tpl.append(
                "  <img class= 'grid-book-image' style=\"height: var(--lumo-size-m); margin-right: var(--lumo-space-s);\" src=\"/images/books/${item.pictureUrl}\" alt=\"${item.title}\" />");
        tpl.append("  <div>");
        tpl.append("    ${item.title}");
        tpl.append("</div>");

        return LitRenderer.<Book> of(tpl.toString()).withProperty("pictureUrl", Book::getImage)
                .withProperty("title", Book::getTitle);
    }
}
