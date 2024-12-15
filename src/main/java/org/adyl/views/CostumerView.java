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
import org.adyl.model.Costumer;
import org.adyl.repository.CostumerRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.security.models.StoreUser;
import org.adyl.security.repositories.StoreUserRepository;
import org.adyl.service.AuthenticationService;
import org.springframework.util.StringUtils;

import java.util.List;

@Route(value = "costumers", layout = MainView.class)
@RolesAllowed({"ROLE_MANAGER", "ROLE_ADMIN"})
public class CostumerView extends FlexLayout implements BeforeEnterObserver {
    private CostumerRepository costumerRepository;
    private StoreUserRepository userRepository;
//    @Autowired
//    private CostumerServiceImpl costumerService;
    private AuthenticationService authenticationService;
    private StoreUserDetails principal;
    private List<String> roles;
    private Grid<Costumer> costumersGrid;

    public CostumerView(AuthenticationService authenticationService, CostumerRepository costumerRepository, StoreUserRepository userRepository) {
        this.authenticationService = authenticationService;
        this.costumerRepository = costumerRepository;
        this.userRepository = userRepository;

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        roles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        setAlignItems(Alignment.START);
        setJustifyContentMode(JustifyContentMode.START);
        setWidth("100%");
        setHeight("100%");
        setFlexWrap(FlexWrap.WRAP);

//        List<Costumer> costumers = costumerRepository.findAll();
//        drawForAdministration(costumers);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String filter = event.getLocation().getQueryParameters().getSingleParameter("filter").orElse("");
        List<Costumer> costumers = costumerRepository.findAll();

        if (StringUtils.hasText(filter) && filter.matches("^\\d+$")) {
            costumers = List.of(costumerRepository.findById(Integer.parseInt(filter)).get());
        }

        drawForAdministration(costumers);
    }

    private void drawForAdministration(List<Costumer> costumers) {
        removeAll();
        setFlexDirection(FlexDirection.COLUMN);
        setJustifyContentMode(JustifyContentMode.START);

        H1 header = new H1("Costumers:");
        setAlignSelf(Alignment.CENTER, header);
        header.getStyle().setMarginTop("20px");
        header.getStyle().setMarginBottom("20px");

        add(header);

        costumersGrid = new Grid<>(Costumer.class, false);
        costumersGrid.setAllRowsVisible(true);
        costumersGrid.setHeight("min-content");
        costumersGrid.setMaxHeight("65%");
        costumersGrid.setWidth("95%");
        costumersGrid.getStyle().setMarginTop("20px");
        costumersGrid.getStyle().setMarginLeft("auto");
        costumersGrid.getStyle().setMarginRight("auto");

        costumersGrid.addColumn(Costumer::getIdnp).setHeader("IDNP");
        costumersGrid.addColumn(Costumer::getName).setHeader("Name");
        costumersGrid.addColumn(Costumer::getAddress).setHeader("Address");
        costumersGrid.addColumn(Costumer::getPhone).setHeader("Phone");
        costumersGrid.addColumn(Costumer::getEmail).setHeader("Email");

        costumersGrid.setItems(costumers);
        add(costumersGrid);

        if (!roles.isEmpty() && roles.contains("ROLE_ADMIN")) {
            costumersGrid.addComponentColumn(costumer -> {
                Button edit = new Button("Edit");
                edit.addClickListener(event -> {
                    Dialog editDialogue = new Dialog("Edit Costumer info");

                    addAddOrEditDialogueLayout(editDialogue, costumer);

                    editDialogue.open();
                });
                return edit;
            }).setHeader("Edit").setAutoWidth(true);

//            costumersGrid.addComponentColumn(costumer -> {
//                Button delete = new Button("Delete");
//                delete.addClickListener(event -> {
//                    Dialog deleteDialogue = new Dialog("Delete Costumer Confirmation");
//
//                    addDeleteDialogueLayout(deleteDialogue, costumer);
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
//            Dialog addDialog = new Dialog("Add new Costumer");
//
//            addAddOrEditDialogueLayout(addDialog, null);
//
//            addDialog.open();
//        });
    }

    private void addAddOrEditDialogueLayout(Dialog editDialogue, Costumer costumer){
        Binder<Costumer> binder = new Binder<>();

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

        if (costumer != null){
            idnp.setValue(costumer.getIdnp());
            name.setValue(costumer.getName());
            address.setValue(costumer.getAddress());
            phone.setValue(costumer.getPhone());
            email.setValue(costumer.getEmail());

            save.setText("Edit!");
        }

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.add(idnp, name, address, phone, email, buttons);

        editDialogue.add(layout);

        save.addClickListener(event -> {
            binder.forField(idnp).withValidator(s -> StringUtils.hasText(s) || !s.equals("0000000000000"), "Specify IDNP!")
                    .withValidator(s -> s.matches("^\\d{13}$"), "Specify IDNP in format of 0000000000000").bind(Costumer::getIdnp, Costumer::setIdnp);
            binder.forField(name).withValidator(StringUtils::hasText, "Specify name!").bind(Costumer::getName, Costumer::setName);
            binder.forField(address).withValidator(StringUtils::hasText, "Specify address!").bind(Costumer::getAddress, Costumer::setAddress);
            binder.forField(phone).withValidator(StringUtils::hasText, "Specify the phone!")
                    .withValidator(s -> s.matches("^\\+373\\d{8}$"), "Specify phone in format of +373xxxxxxxx").bind(Costumer::getPhone, Costumer::setPhone);
            binder.forField(email).withValidator(StringUtils::hasText, "Specify email!")
                    .withValidator(s -> s.matches("^.+@.+\\..+$"), "Specify email in format example@gmail.com").bind(Costumer::getEmail, Costumer::setEmail);


            BinderValidationStatus validation = binder.validate();
            if (!validation.hasErrors()) {
                Costumer newCostumer = new Costumer(idnp.getValue(), name.getValue(), address.getValue(), phone.getValue(), email.getValue());

                if (costumer!=null) {
                    newCostumer.setId(costumer.getId());
                }

                costumerRepository.save(newCostumer);
                editDialogue.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            }
        });

        cancel.addClickListener(event -> editDialogue.close());
    }


    protected void addDeleteDialogueLayout(Dialog dialog, Costumer costumer) {
        H3 question = new H3("Are you sure you want to delete contact \"" + costumer.getName() + "\" (" + costumer.getIdnp() + ")?");
        question.getStyle().setMarginBottom("20px");
        H3 note = new H3("*new costumer will be created instead!!!!");
        question.getStyle().setMarginBottom("20px");

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(delete, cancel);
        buttons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dialog.add(question, buttons);

        delete.addClickListener(event -> {
            //firstly we create new costumer for user
            StoreUser user = userRepository.findByCostumer(costumer);
            if (user!=null) {
                Costumer newCostumer = new Costumer();
                newCostumer.setName(user.getUsername());
                costumerRepository.save(newCostumer);

                user.setCostumer(newCostumer);
                userRepository.save(user);
            }

            costumerRepository.delete(costumer);

//            userRepository.save(user);
            //User must have it's costumer. If we want to delete the customer for user we must create new one, but empty
//            deleteCostumerForForItsUser(costumer);
//            costumerRepository.delete(costumer);
//            costumerService.deleteUserForCustomer(costumer);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
    }



//    @Transactional
//    protected void deleteCostumerForForItsUser(Costumer costumer) {
//        StoreUser user = userRepository.findByCostumer(costumer);
//        user.setUsername("CCC");
////        user.setCostumer(null);
//    }
}
