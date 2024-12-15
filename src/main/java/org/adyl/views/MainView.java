package org.adyl.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;
import org.adyl.views.authors.AuthorsView;
import org.adyl.views.books.BooksView;
import org.adyl.views.contacts.ContactTypesView;
import org.adyl.views.contacts.ContactsView;
import org.adyl.views.me.MeView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@PageTitle("BookStore")
@Route("/")
@AnonymousAllowed
@CssImport("my-styles/style.css")
public class MainView extends AppLayout {
    private Html home;
    private List<String> roles;
    private Image userIcon;
    private AuthenticationService authenticationService;

    public MainView(AuthenticationService authenticationService) throws FileNotFoundException {
        this.authenticationService = authenticationService;

        StoreUserDetails principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        if (principal != null) {
            roles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();
            userIcon = new Image("/images/users/" + principal.getUser().getImage(), principal.getUser().getImage());
        } else {
            userIcon = new Image("/images/users/Placeholder.png" , "User photo");
        }

//        System.out.println(principal);

        userIcon.setWidth("100%");
        userIcon.setHeight("100%");
        userIcon.getStyle().setBorderRadius("100%");

        getElement().getThemeList().add(Lumo.DARK);

        home = new Html(new FileInputStream("src/main/resources/templates/welcome.html"));

//        System.out.println(VaadinSession.getCurrent().getSession());

        FlexLayout navBarLayout = new FlexLayout();
        navBarLayout.setWidth("100%");

        H2 header = new H2("BookStore");
        header.getStyle().setMarginLeft("10px");
        header.setWidth("9%");
        header.addClickListener(event -> {
            event.getSource().getUI().ifPresent(ui -> {
                ui.navigate("/");
                setContent(home);
            });
        });

        FlexLayout buttons = addNavBarButtons();

        navBarLayout.add(header, buttons);
        if (roles!= null && !roles.contains("ROLE_ADMIN") && !roles.contains("ROLE_MANAGER")) {
            navBarLayout.add(addCart());
        }
        navBarLayout.add(addMe());
        navBarLayout.setFlexGrow(0.1, header);
        navBarLayout.setFlexGrow(10, buttons);
//        navBarLayout.setFlexGrow(0.2, me);

        addToNavbar(navBarLayout);

//        setContent(getHomePage());
        setContent(home);
    }

    public FlexLayout addNavBarButtons(){
        FlexLayout buttons = new FlexLayout();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button authors = new Button("Authors");
        authors.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(AuthorsView.class)));
        buttons.add(authors);

        Button categories = new Button("Categories");
        categories.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(CategoriesView.class)));
        categories.getStyle().setMarginLeft("5px");
        buttons.add(categories);

        Button books = new Button("Books");
        books.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(BooksView.class)));
        books.getStyle().setMarginLeft("5px");
        buttons.add(books);

        Button contactTypes = new Button("Contact types");
        contactTypes.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(ContactTypesView.class)));
        contactTypes.getStyle().setMarginLeft("5px");
        if (roles != null && (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN"))) {
            buttons.add(contactTypes);
        }

        Button contacts = new Button("Contacts");
        contacts.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(ContactsView.class)));
        contacts.getStyle().setMarginLeft("5px");
        if (roles != null && (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN"))) {
            buttons.add(contacts);
        }

        Button costumers = new Button("Costumers");
        costumers.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(CostumerView.class)));
        costumers.getStyle().setMarginLeft("5px");
        if (roles != null && (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN"))) {
            buttons.add(costumers);
        }

        Button orders = new Button("Orders");
        orders.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(OrdersView.class)));
        orders.getStyle().setMarginLeft("5px");
        if (roles != null && (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN"))) {
            buttons.add(orders);
        }

//        Button orderItems = new Button("Order items");
//        orderItems.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(OrderItemsView.class)));
//        orderItems.getStyle().setMarginLeft("5px");
//        if (roles != null && (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN"))) {
//            buttons.add(orderItems);
//        }

        Button users = new Button("Users");
        users.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(UsersView.class)));
        users.getStyle().setMarginLeft("5px");
        if (roles != null && (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN"))) {
            buttons.add(users);
        }

        return buttons;
    }

    private Div addMe(){
        Div me = new Div();
        me.add(userIcon);
        me.getStyle().setDisplay(Style.Display.FLEX);
        me.getStyle().setJustifyContent(Style.JustifyContent.CENTER);
        me.getStyle().setAlignItems(Style.AlignItems.CENTER);
//        me.getStyle().setBackgroundColor("red");
        me.getStyle().setMarginRight("15px");
        me.setWidth("45px");
        me.setHeight("45px");
        me.getStyle().setMarginLeft("2%");
        me.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate(MeView.class)));
        return me;
    }

    private Div addCart(){
        Div cart = new Div();
        cart.setWidth("40px");
        cart.setHeight("40px");
        Image cartIcon = new Image("/images/cart-icon.png", "cart-icon.png");
        cartIcon.setWidth("100%");
        cartIcon.setHeight("100%");
        cart.add(cartIcon);

        cart.getStyle().setMarginTop("auto");

        cart.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate("/cart")));

        return cart;
    }
}
