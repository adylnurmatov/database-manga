package org.adyl.views.contacts;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.adyl.model.Contact;
import org.adyl.model.RefContactType;
import org.adyl.repository.ContactRepository;
import org.adyl.repository.RefContactTypeRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;
import org.adyl.views.MainView;
import org.springframework.util.StringUtils;

import java.util.List;

@Route(value = "contacts", layout = MainView.class)
@RolesAllowed({"ROLE_MANAGER", "ROLE_ADMIN"})
public class ContactsView extends FlexLayout implements BeforeEnterObserver {
    private ContactRepository contactRepository;
    private RefContactTypeRepository contactTypeRepository;
    private AuthenticationService authenticationService;
    private StoreUserDetails principal;
    private List<String> roles;

    private Grid<Contact> contactGrid;

    public ContactsView(ContactRepository contactRepository, RefContactTypeRepository contactTypeRepository, AuthenticationService authenticationService) {
        this.contactRepository = contactRepository;
        this.contactTypeRepository = contactTypeRepository;
        this.authenticationService = authenticationService;

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        roles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        setAlignItems(Alignment.START);
        setJustifyContentMode(JustifyContentMode.START);
        setWidth("100%");
        setHeight("100%");
        setFlexWrap(FlexWrap.WRAP);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String type = event.getLocation().getQueryParameters().getSingleParameter("filter").orElse("");

        List<Contact> contacts = contactRepository.findAll();

        if (StringUtils.hasText(type)) {
            if (type.contains("type")) {
                contacts = contactRepository.findAllByContactType_Code(Integer.parseInt(type.substring(5)));
            }
        }

        drawForAdministration(contacts);
    }

    private void drawForAdministration(List<Contact> contacts) {
        removeAll();
        setFlexDirection(FlexDirection.COLUMN);
        setJustifyContentMode(JustifyContentMode.START);

        H1 header = new H1("Contacts:");
        setAlignSelf(Alignment.CENTER, header);
        header.getStyle().setMarginTop("20px");
        header.getStyle().setMarginBottom("20px");

        add(header);

        contactGrid = new Grid<>(Contact.class, false);
        contactGrid.setAllRowsVisible(true);
        contactGrid.setHeight("min-content");
        contactGrid.setMaxHeight("65%");
        contactGrid.setWidth("95%");
        contactGrid.getStyle().setMarginTop("20px");
        contactGrid.getStyle().setMarginLeft("auto");
        contactGrid.getStyle().setMarginRight("auto");

        contactGrid.addComponentColumn(contact -> new Anchor("/contact-types?filter="+contact.getContactType().getCode(), contact.getContactType().getContactTypeDescription())).setHeader("Contact type");
        contactGrid.addColumn(Contact::getFirstname).setHeader("Firstname");
        contactGrid.addColumn(Contact::getLastname).setHeader("Lastname");
        contactGrid.addColumn(Contact::getWorkPhone).setHeader("Work phone");
        contactGrid.addColumn(Contact::getCellPhone).setHeader("Cell phone");
        contactGrid.addColumn(Contact::getOtherDetails).setHeader("Other details");

        contactGrid.setItems(contacts);
        add(contactGrid);

        if (!roles.isEmpty() && roles.contains("ROLE_ADMIN")) {
            contactGrid.addComponentColumn(category -> {
                Button edit = new Button("Edit");
                edit.addClickListener(event -> {
                    Dialog editDialogue = new Dialog("Edit Contact info");

                    addAddOrEditDialogueLayout(editDialogue, category);

                    editDialogue.open();
                });
                return edit;
            }).setHeader("Edit");

            contactGrid.addComponentColumn(category -> {
                Button delete = new Button("Delete");
                delete.addClickListener(event -> {
                    Dialog deleteDialogue = new Dialog("Delete Contact Confirmation");

                    addDeleteDialogueLayout(deleteDialogue, category);

                    deleteDialogue.open();
                });

                return delete;
            }).setHeader("Delete");


            Button add = new Button("Add new");

            add(add);
            add.getStyle().setMarginTop("10px");
            setAlignSelf(Alignment.END, add);

            add.setWidth("max-content");
            add.getStyle().setMarginRight("5%");
            add.getStyle().setPadding("5px 10px");

            add.addClickListener(event -> {
                Dialog addDialog = new Dialog("Add new Contact");

                addAddOrEditDialogueLayout(addDialog, null);

                addDialog.open();
            });
        }


    }

    private void addAddOrEditDialogueLayout(Dialog editDialogue, Contact contact){
        Binder<Contact> binder = new Binder<>();

        ComboBox<RefContactType> type = new ComboBox<>("Type: ", contactTypeRepository.findAll());
        type.setItemLabelGenerator(RefContactType::getContactTypeDescription);
        type.setAllowCustomValue(false);

        TextField firstname = new TextField("Firstname: ");
        TextField lastname = new TextField("Lastname: ");
        TextField workPhone = new TextField("Work phone: ");
        TextField cellPhone = new TextField("Cell phone: ");
        TextArea otherDetails = new TextArea("Other details: ");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel");

        buttons.add(save, cancel);

        if (contact != null){
            type.setValue(contact.getContactType());
            firstname.setValue(contact.getFirstname());
            lastname.setValue(contact.getLastname());
            workPhone.setValue(contact.getWorkPhone());
            cellPhone.setValue(contact.getCellPhone());
            otherDetails.setValue(contact.getOtherDetails());

            save.setText("Edit!");
        }

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.add(type, firstname, lastname, workPhone, cellPhone, otherDetails, buttons);

        editDialogue.add(layout);

        save.addClickListener(event -> {
            binder.forField(type).withValidator(t -> t!=null, "Specify contact type!").bind(Contact::getContactType, Contact::setContactType);
            binder.forField(firstname).withValidator(StringUtils::hasText, "Specify firstname!").bind(Contact::getFirstname, Contact::setFirstname);
            binder.forField(lastname).withValidator(StringUtils::hasText, "Specify lastname!").bind(Contact::getLastname, Contact::setLastname);
            binder.forField(workPhone).withValidator(StringUtils::hasText, "Specify work phone!")
                                      .withValidator(s -> s.matches("^\\+373\\d{8}$"), "Specify phone in format of +373xxxxxxxx").bind(Contact::getWorkPhone, Contact::setWorkPhone);
            binder.forField(cellPhone).withValidator(StringUtils::hasText, "Specify cell phone!")
                    .withValidator(s -> s.matches("^\\+373\\d{8}$"), "Specify phone in format of +373xxxxxxxx").bind(Contact::getCellPhone, Contact::setCellPhone);

            BinderValidationStatus validation = binder.validate();
            if (!validation.hasErrors()) {
                Contact newContact = new Contact(type.getValue(), firstname.getValue(), lastname.getValue(), workPhone.getValue(), cellPhone.getValue(), otherDetails.getValue());

                if (contact!=null) {
                    newContact.setId(contact.getId());
                }

                contactRepository.save(newContact);
                editDialogue.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            }
        });

        cancel.addClickListener(event -> editDialogue.close());
    }

    private void addDeleteDialogueLayout(Dialog dialog, Contact contact) {
        H3 question = new H3("Are you sure you want to delete contact \"" + contact.getFirstname() + " " + contact.getLastname() + "\"?");
        question.getStyle().setMarginBottom("20px");

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(delete, cancel);
        buttons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dialog.add(question, buttons);

        delete.addClickListener(event -> {
            contactRepository.delete(contact);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
    }
}
