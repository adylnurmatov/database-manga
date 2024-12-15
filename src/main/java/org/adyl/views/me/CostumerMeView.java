package org.adyl.views.me;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.security.PermitAll;
import org.adyl.model.Costumer;
import org.adyl.repository.CostumerRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;

@Route("costumers/me")
@PermitAll
public class CostumerMeView extends FlexLayout {
    private AuthenticationService authenticationService;
    private CostumerRepository costumerRepository;

    private StoreUserDetails principal;

    public CostumerMeView(AuthenticationService authenticationService, CostumerRepository costumerRepository){
        this.costumerRepository = costumerRepository;
        this.authenticationService = authenticationService;

        getElement().getThemeList().add(Lumo.DARK);
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setAlignItems(Alignment.CENTER);

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();

        Costumer costumer = principal.getUser().getCostumer();

        H1 header = new H1("Your details:");
        header.getStyle().setMarginTop("20px");

        VerticalLayout info = new VerticalLayout();

        PasswordField idnp = new PasswordField();
        idnp.setValue(costumer.getIdnp());
        idnp.setReadOnly(true);
        idnp.getStyle().setMarginLeft("5px");

        Div idnpDiv = new Div(new Span("IDNP: "), idnp);

        Span name = new Span("Name: " + costumer.getName());
        Span address = new Span("Address: " + costumer.getAddress());
        Span phone = new Span("Phone: " + costumer.getPhone());

        Span email = new Span("Email: "+ costumer.getEmail());

//        EmailField email = new EmailField();
//        email.setValue(costumer.getEmail());
//        email.setReadOnly(true);

        Div orders = new Div(new Span("Orders: "), new Anchor("/orders/me", "see my orders..."));

        add(header);
        info.add(idnpDiv, name, address, phone, email);
        if (principal.getUser().getRoles()!=null && !principal.getUser().getRoles().contains("ROLE_MANAGER") && !principal.getUser().getRoles().contains("ROLE_ADMIN"))
            info.add(orders);
        info.setWidth("max-content");
        info.getStyle().setPadding("10px 20px");
        info.getStyle().setBorderRadius("10px");
        info.getStyle().setBorder("1px dashed white");
        info.getStyle().setMarginTop("10px");
        info.getStyle().setMarginBottom("10px");

        add(info);
        add(new Button("Home", event -> {
            getUI().ifPresent(ui -> ui.navigate("/"));
        }));
    }
}
