package org.adyl.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.adyl.model.Customer;
import org.adyl.repository.CustomerRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.security.models.StoreUser;
import org.adyl.security.repositories.StoreUserRepository;
import org.adyl.service.AuthenticationService;
import org.springframework.util.StringUtils;

import java.util.List;

@Route(value = "customers", layout = MainView.class)
@RolesAllowed({"ROLE_MANAGER", "ROLE_ADMIN"})
public class CustomerView extends FlexLayout implements BeforeEnterObserver {
    private CustomerRepository customerRepository;
    private StoreUserRepository userRepository;
//    @Autowired
//    private CustomerServiceImpl customerService;
    private AuthenticationService authenticationService;
    private StoreUserDetails principal;
    private List<String> roles;
    private Grid<Customer> customersGrid;

    public CustomerView(AuthenticationService authenticationService, CustomerRepository customerRepository, StoreUserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        roles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        setAlignItems(Alignment.START);
        setJustifyContentMode(JustifyContentMode.START);
        setWidth("100%");
        setHeight("100%");
        setFlexWrap(FlexWrap.WRAP);

//        List<Customer> customers = customerRepository.findAll();
//        drawForAdministration(customers);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String filter = event.getLocation().getQueryParameters().getSingleParameter("filter").orElse("");
        List<Customer> customers = customerRepository.findAll();

        if (StringUtils.hasText(filter) && filter.matches("^\\d+$")) {
            customers = List.of(customerRepository.findById(Integer.parseInt(filter)).get());
        }

        drawForAdministration(customers);
    }

    private void drawForAdministration(List<Customer> customers) {
        removeAll();
        setFlexDirection(FlexDirection.COLUMN);
        setJustifyContentMode(JustifyContentMode.START);

        H1 header = new H1("Customers:");
        setAlignSelf(Alignment.CENTER, header);
        header.getStyle().setMarginTop("20px");
        header.getStyle().setMarginBottom("20px");

        add(header);

        customersGrid = new Grid<>(Customer.class, false);
        customersGrid.setAllRowsVisible(true);
        customersGrid.setHeight("min-content");
        customersGrid.setMaxHeight("65%");
        customersGrid.setWidth("95%");
        customersGrid.getStyle().setMarginTop("20px");
        customersGrid.getStyle().setMarginLeft("auto");
        customersGrid.getStyle().setMarginRight("auto");

        customersGrid.addColumn(Customer::getIdnp).setHeader("IDNP");
        customersGrid.addColumn(Customer::getName).setHeader("Name");
        customersGrid.addColumn(Customer::getAddress).setHeader("Address");
        customersGrid.addColumn(Customer::getPhone).setHeader("Phone");
        customersGrid.addColumn(Customer::getEmail).setHeader("Email");

        customersGrid.setItems(customers);
        add(customersGrid);

        if (!roles.isEmpty() && roles.contains("ROLE_ADMIN")) {
            customersGrid.addComponentColumn(customer -> {
                Button edit = new Button("Edit");
                edit.addClickListener(event -> {
                    Dialog editDialogue = new Dialog("Edit Customer info");

                    addAddOrEditDialogueLayout(editDialogue, customer);

                    editDialogue.open();
                });
                return edit;
            }).setHeader("Edit").setAutoWidth(true);

//            customersGrid.addComponentColumn(customer -> {
//                Button delete = new Button("Delete");
//                delete.addClickListener(event -> {
//                    Dialog deleteDialogue = new Dialog("Delete Customer Confirmation");
//
//                    addDeleteDialogueLayout(deleteDialogue, customer);
//
//                    deleteDialogue.open();
//                });
//
//                return delete;
//            }).setHeader("Delete");
        }


//        Button add = new Button("Add new");
//
//        add(add);
//        add.getStyle().setMarginTop("10px");
//        setAlignSelf(Alignment.END, add);
//
//        add.setWidth("max-content");
//        add.getStyle().setMarginRight("5%");
//        add.getStyle().setPadding("5px 10px");
//
//        add.addClickListener(event -> {
//            Dialog addDialog = new Dialog("Add new Customer");
//
//            addAddOrEditDialogueLayout(addDialog, null);
//
//            addDialog.open();
//        });
    }

    private void addAddOrEditDialogueLayout(Dialog editDialogue, Customer customer){
        Binder<Customer> binder = new Binder<>();

        TextField idnp = new TextField("IDNP: ");
        TextField name = new TextField("Name: ");
        TextField address = new TextField("Address: ");
        TextField phone = new TextField("Phone: ");
        EmailField email = new EmailField("Email: ");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel");

        buttons.add(save, cancel);

        if (customer != null){
            idnp.setValue(customer.getIdnp());
            name.setValue(customer.getName());
            address.setValue(customer.getAddress());
            phone.setValue(customer.getPhone());
            email.setValue(customer.getEmail());

            save.setText("Edit!");
        }

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.add(idnp, name, address, phone, email, buttons);

        editDialogue.add(layout);

        save.addClickListener(event -> {
            binder.forField(idnp).withValidator(s -> StringUtils.hasText(s) || !s.equals("0000000000000"), "Specify IDNP!")
                    .withValidator(s -> s.matches("^\\d{13}$"), "Specify IDNP in format of 0000000000000").bind(Customer::getIdnp, Customer::setIdnp);
            binder.forField(name).withValidator(StringUtils::hasText, "Specify name!").bind(Customer::getName, Customer::setName);
            binder.forField(address).withValidator(StringUtils::hasText, "Specify address!").bind(Customer::getAddress, Customer::setAddress);
            binder.forField(phone).withValidator(StringUtils::hasText, "Specify the phone!")
                    .withValidator(s -> s.matches("^\\+373\\d{8}$"), "Specify phone in format of +373xxxxxxxx").bind(Customer::getPhone, Customer::setPhone);
            binder.forField(email).withValidator(StringUtils::hasText, "Specify email!")
                    .withValidator(s -> s.matches("^.+@.+\\..+$"), "Specify email in format example@gmail.com").bind(Customer::getEmail, Customer::setEmail);


            BinderValidationStatus validation = binder.validate();
            if (!validation.hasErrors()) {
                Customer newCustomer = new Customer(idnp.getValue(), name.getValue(), address.getValue(), phone.getValue(), email.getValue());

                if (customer !=null) {
                    newCustomer.setId(customer.getId());
                }

                customerRepository.save(newCustomer);
                editDialogue.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            }
        });

        cancel.addClickListener(event -> editDialogue.close());
    }


    protected void addDeleteDialogueLayout(Dialog dialog, Customer customer) {
        H3 question = new H3("Are you sure you want to delete contact \"" + customer.getName() + "\" (" + customer.getIdnp() + ")?");
        question.getStyle().setMarginBottom("20px");
        H3 note = new H3("*new customer will be created instead!!!!");
        question.getStyle().setMarginBottom("20px");

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(delete, cancel);
        buttons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dialog.add(question, buttons);

        delete.addClickListener(event -> {
            //firstly we create new customer for user
            StoreUser user = userRepository.findByCustomer(customer);
            if (user!=null) {
                Customer newCustomer = new Customer();
                newCustomer.setName(user.getUsername());
                customerRepository.save(newCustomer);

                user.setCustomer(newCustomer);
                userRepository.save(user);
            }

            customerRepository.delete(customer);

//            userRepository.save(user);
            //User must have its customer. If we want to delete the customer for user we must create new one, but empty
//            deleteCustomerForForItsUser(customer);
//            customerRepository.delete(customer);
//            customerService.deleteUserForCustomer(customer);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
    }



//    @Transactional
//    protected void deleteCustomerForForItsUser(Customer customer) {
//        StoreUser user = userRepository.findByCustomer(customer);
//        user.setUsername("CCC");
////        user.setCustomer(null);
//    }
}
