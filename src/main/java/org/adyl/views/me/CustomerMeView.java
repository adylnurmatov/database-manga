package org.adyl.views.me;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.security.PermitAll;
import org.adyl.model.Customer;
import org.adyl.repository.CustomerRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;

@Route("customers/me")
@PermitAll
public class CustomerMeView extends FlexLayout {

    private final AuthenticationService authenticationService;
    private final CustomerRepository customerRepository;

    private StoreUserDetails principal;
    private Customer customer;

    public CustomerMeView(AuthenticationService authenticationService, CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        this.authenticationService = authenticationService;

        getElement().getThemeList().add(Lumo.LIGHT);
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setAlignItems(Alignment.CENTER);

        this.principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        this.customer = principal.getUser().getCustomer();

        H1 header = new H1("Your details:");
        header.getStyle().setMarginTop("20px");

        VerticalLayout info = new VerticalLayout();

        BeanValidationBinder<Customer> validationBinder = new BeanValidationBinder<>(Customer.class);

        TextField nameField = new TextField("Name");
        nameField.setValue(customer.getName());
        nameField.setReadOnly(true);
//        validationBinder.forField(nameField)
//                .withValidator(name -> name != null && !name.trim().isEmpty(), "Name is required")
//                .bind(Customer::getName, Customer::setName);

        TextField addressField = new TextField("Address");
        validationBinder.forField(addressField)
                .withValidator(addr -> addr != null && !addr.trim().isEmpty(), "Address is required")
                .bind(Customer::getAddress, Customer::setAddress);

        TextField phoneField = new TextField("Phone");
        validationBinder.forField(phoneField)
                .withValidator(phone -> phone.matches("^\\+996\\d{9}$"), "Phone must be in format +996xxxxxxxxx")
                .bind(Customer::getPhone, Customer::setPhone);

        EmailField emailField = new EmailField("Email");
        validationBinder.forField(emailField)
                .withValidator(email -> email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"),
                        "Enter a valid email address")
                .bind(Customer::getEmail, Customer::setEmail);

        validationBinder.readBean(customer);
        Button saveBtn = new Button("Save Changes");
        saveBtn.setVisible(false);

        // Detect changes and show Save button
        addressField.addValueChangeListener(e -> showSaveButtonIfChanged(saveBtn, addressField, phoneField, emailField));
        phoneField.addValueChangeListener(e -> showSaveButtonIfChanged(saveBtn, addressField, phoneField, emailField));
        emailField.addValueChangeListener(e -> showSaveButtonIfChanged(saveBtn, addressField, phoneField, emailField));

        saveBtn.addClickListener(event -> {
            try {
                validationBinder.writeBean(customer); // triggers validation and updates entity
                customerRepository.save(customer);
                saveBtn.setVisible(false);
                Notification.show("Details updated successfully!");
            } catch (ValidationException e) {
                Notification.show("Please fix validation errors.", 4000, Notification.Position.MIDDLE);
            } catch (Exception ex) {
                ex.printStackTrace();
                Notification.show("An error occurred while saving.", 4000, Notification.Position.MIDDLE);
            }
        });

        Div orders = new Div(new Span("Orders: "), new Anchor("/orders/me", "see my orders..."));

        info.add(nameField, addressField, phoneField, emailField);
        if (principal.getUser().getRoles() != null &&
                !principal.getUser().getRoles().contains("ROLE_MANAGER") &&
                !principal.getUser().getRoles().contains("ROLE_ADMIN")) {
            info.add(orders);
        }

        info.add(saveBtn);
        info.setWidth("max-content");
        info.getStyle().setPadding("10px 20px");
        info.getStyle().setBorderRadius("10px");
        info.getStyle().setBorder("1px dashed white");
        info.getStyle().setMarginTop("10px");
        info.getStyle().setMarginBottom("10px");

        add(header, info);
        add(new Button("Home", event -> getUI().ifPresent(ui -> ui.navigate("/"))));
    }

    private void showSaveButtonIfChanged(Button saveBtn, TextField addressField, TextField phoneField, EmailField emailField) {
        boolean changed =
                        !addressField.getValue().equals(customer.getAddress()) ||
                        !phoneField.getValue().equals(customer.getPhone()) ||
                        !emailField.getValue().equals(customer.getEmail());

        saveBtn.setVisible(changed);
    }
}

