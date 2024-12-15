package org.adyl.views.contacts;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.adyl.model.RefContactType;
import org.adyl.repository.RefContactTypeRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;
import org.adyl.views.MainView;
import org.springframework.util.StringUtils;

import java.util.List;

@Route(value = "contact-types", layout = MainView.class)
@RolesAllowed({"ROLE_MANAGER", "ROLE_ADMIN"})
public class ContactTypesView extends FlexLayout implements BeforeEnterObserver {
    private RefContactTypeRepository repository;

    private StoreUserDetails principal;
    private AuthenticationService authenticationService;
    private List<String> principalRoles;

    public ContactTypesView(RefContactTypeRepository repository, AuthenticationService authenticationService) {
        this.repository = repository;
        this.authenticationService = authenticationService;

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        principalRoles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        setFlexDirection(FlexDirection.COLUMN);
        setAlignItems(Alignment.CENTER);
        setSizeFull();
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String filter = event.getLocation().getQueryParameters().getSingleParameter("filter").orElse("");
        List<RefContactType> contactTypes = repository.findAll();

        if (StringUtils.hasText(filter) && filter.matches("^\\d+$")) {
            contactTypes = List.of(repository.findById(Integer.parseInt(filter)).get());
        }

        drawContactTypes(contactTypes);
    }

    private void drawContactTypes(List<RefContactType> contactTypes) {
        removeAll();
        add(new H1("All Contact types:"));

        Grid<RefContactType> contactTypeGrid = new Grid<>(RefContactType.class, false);
        contactTypeGrid.addColumn(LitRenderer.<RefContactType> of("<a href='contacts?filter=type-${item.code}'> ${item.contactType} </a>")
                .withProperty("contactType", RefContactType::getContactTypeDescription).withProperty("code", RefContactType::getCode)).setHeader("Contact type:");

        contactTypeGrid.setItems(contactTypes);

        contactTypeGrid.getStyle().setTextAlign(Style.TextAlign.CENTER);
        contactTypeGrid.setAllRowsVisible(true);
        contactTypeGrid.setWidth("35%");
        contactTypeGrid.setHeight("min-content");
        contactTypeGrid.setMaxHeight("65%");
        contactTypeGrid.getStyle().setMarginLeft("auto");
        contactTypeGrid.getStyle().setMarginRight("auto");
        contactTypeGrid.getStyle().setMarginTop("20px");
//
//        add(availableCategories);
        add(contactTypeGrid);

        if (!principalRoles.isEmpty() && principalRoles.contains("ROLE_ADMIN")) {
            contactTypeGrid.addComponentColumn(type -> {
                Button edit = new Button("Edit");
                edit.addClickListener(event -> {
                    Dialog editDialogue = new Dialog("Edit Contact type info");

                    addAddOrEditDialogueLayout(editDialogue, type);

                    editDialogue.open();
                });
                return edit;
            }).setHeader("Edit");

            contactTypeGrid.addComponentColumn(type -> {
                Button delete = new Button("Delete");
                delete.addClickListener(event -> {
                    Dialog deleteDialogue = new Dialog("Delete Contact type Confirmation");

                    addDeleteDialogueLayout(deleteDialogue, type);

                    deleteDialogue.open();
                });

                return delete;
            }).setHeader("Delete");



            Button add = new Button("Add new");

            add(add);
            add.getStyle().setMarginTop("10px");
            setAlignSelf(Alignment.END, add);

            add.setWidth("max-content");
            add.getStyle().setMarginRight("32.5%");
            add.getStyle().setPadding("5px 10px");

            add.addClickListener(event -> {
                Dialog addDialog = new Dialog("Add new Contact type");

                addAddOrEditDialogueLayout(addDialog, null);

                addDialog.open();
            });
        }
    }

    private void addAddOrEditDialogueLayout(Dialog editDialogue, RefContactType type){
        Binder<RefContactType> binder = new Binder<>();

        TextField contactTypeName = new TextField("Contact type name: ");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel");

        buttons.add(save, cancel);

        if (type != null){
            contactTypeName.setValue(type.getContactTypeDescription());
            save.setText("Edit!");
        }



        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.add(contactTypeName, buttons);

        editDialogue.add(layout);

        save.addClickListener(event -> {
            binder.forField(contactTypeName).withValidator(s -> StringUtils.hasText(s), "Specify Contact type name!").bind(RefContactType::getContactTypeDescription, RefContactType::setContactTypeDescription);

            BinderValidationStatus validation = binder.validate();


            if (!validation.hasErrors()) {
                RefContactType newContactType = new RefContactType(contactTypeName.getValue());

                if (type!=null)
                    newContactType.setCode(type.getCode());

                repository.save(newContactType);
                editDialogue.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            }
        });

        cancel.addClickListener(event -> editDialogue.close());
    }

    private void addDeleteDialogueLayout(Dialog dialog, RefContactType type) {
        H3 question = new H3("Are you sure you want to delete the contact type \"" + type.getContactTypeDescription() + "\"?");
        question.getStyle().setMarginBottom("20px");

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(delete, cancel);
        buttons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dialog.add(question, buttons);

        delete.addClickListener(event -> {
            repository.delete(type);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
    }

}
