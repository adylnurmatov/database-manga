package org.adyl.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.adyl.model.Costumer;
import org.adyl.repository.CostumerRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.security.models.StoreUser;
import org.adyl.security.repositories.StoreUserRepository;
import org.adyl.service.AuthenticationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "users", layout = MainView.class)
@RolesAllowed({"ROLE_MANAGER", "ROLE_ADMIN"})
public class UsersView extends FlexLayout {
    private StoreUserRepository userRepository;
    private CostumerRepository costumerRepository;
    private AuthenticationService authenticationService;
    private Grid<StoreUser> usersGrid;
    private StoreUserDetails principal;
    private List<String> roles;
    private PasswordEncoder encoder;
    private MultiFileMemoryBuffer buffer;

    private static final String USERS_IMAGES_FOLDER = "src/main/resources/static/images/users";

    public UsersView(AuthenticationService authenticationService, StoreUserRepository userRepository, CostumerRepository costumerRepository, PasswordEncoder encoder) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.costumerRepository = costumerRepository;
        this.encoder = encoder;

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        roles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        setAlignItems(Alignment.START);
        setJustifyContentMode(JustifyContentMode.START);
        setWidth("100%");
        setHeight("100%");
        setFlexWrap(FlexWrap.WRAP);

        List<StoreUser> users = userRepository.findAll();
        drawForAdministration(users);
    }

    private void drawForAdministration(List<StoreUser> users) {
        removeAll();
        setFlexDirection(FlexDirection.COLUMN);
        setJustifyContentMode(JustifyContentMode.START);

        H1 header = new H1("Users:");
        setAlignSelf(Alignment.CENTER, header);
        header.getStyle().setMarginTop("20px");
        header.getStyle().setMarginBottom("20px");

        add(header);

        usersGrid = new Grid<>(StoreUser.class, false);
        usersGrid.setAllRowsVisible(true);
        usersGrid.setMaxHeight("65%");
        usersGrid.setHeight("min-content");
        usersGrid.setWidth("95%");
        usersGrid.getStyle().setMarginTop("20px");
        usersGrid.getStyle().setMarginLeft("auto");
        usersGrid.getStyle().setMarginRight("auto");

//        usersGrid.addColumn(storeUser -> storeUser.getUsername()).setHeader("Username");
        usersGrid.addColumn(storeUser -> {
            StringBuilder builder = new StringBuilder();
            builder.append(storeUser.getUsername());

            if (storeUser.equals(principal.getUser())) {
                builder.append(" (You)");
            }
            return builder.toString();
        }).setHeader("Username");

        usersGrid.addColumn(StoreUser::getRoles).setHeader("Roles:");
        usersGrid.addComponentColumn(storeUser -> new Anchor("/costumers?filter=" + storeUser.getCostumer().getId(), storeUser.getCostumer().getName())).setHeader("Costumer");
//        usersGrid.addColumn(storeUser -> new Anchor("costumers/", storeUser.getCostumer().getName())).setHeader("Costumer");
        usersGrid.addColumn(
                        LitRenderer.<StoreUser> of("<vaadin-avatar style='margin-right: 10px;' img=\"/images/users/${item.pictureUrl}\" name=\"${item.userName}\" alt=\"Portrait of ${item.userName}\"></vaadin-avatar>").withProperty("pictureUrl", StoreUser::getImage)
                                .withProperty("userName", StoreUser::getUsername))
                .setHeader("Image").setAutoWidth(true).setFlexGrow(0);

        usersGrid.setItems(users);
        add(usersGrid);

        if (!roles.isEmpty() && roles.contains("ROLE_ADMIN")) {
            usersGrid.addComponentColumn(user -> {
                Button edit = new Button("Edit");
                edit.addClickListener(event -> {
                    Dialog editDialogue = new Dialog("Edit User info");

                    addAddOrEditDialogueLayout(editDialogue, user);

                    editDialogue.open();
                });
                return edit;
            }).setHeader("Edit");

            usersGrid.addComponentColumn(user -> {
                Button delete = new Button("Delete");
                delete.addClickListener(event -> {
                    Dialog deleteDialogue = new Dialog("Delete User Confirmation");

                    addDeleteDialogueLayout(deleteDialogue, user);

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
                Dialog addDialog = new Dialog("Add new User");

                addAddOrEditDialogueLayout(addDialog, null);

                addDialog.open();
            });
        }
    }

    private void addAddOrEditDialogueLayout(Dialog editDialogue, StoreUser user){
        Binder<StoreUser> binder = new Binder<>();

        TextField username = new TextField("Username: ");
        PasswordField password = new PasswordField("Password: ", "Enter new password");

        ComboBox<String> roles = new ComboBox<>("Role: ", "ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN");
        ComboBox<Costumer> costumers = new ComboBox<>("Costumer: ", costumerRepository.findAll());
        costumers.setItemLabelGenerator(Costumer::getName);

        FlexLayout photo = new FlexLayout();
        photo.setFlexDirection(FlexDirection.COLUMN);
        Span photoLabel = new Span("Choose a photo");

        buffer = new MultiFileMemoryBuffer();
        Upload image = new Upload(buffer);
        image.setAcceptedFileTypes("image/png", "image/jpeg");
        AtomicReference<String> imageName = new AtomicReference<>();

        image.addSucceededListener(event -> {
            imageName.set(event.getFileName());
        });

        photo.add(photoLabel, image);


        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel");

        buttons.add(save, cancel);

        if (user != null){
            username.setValue(user.getUsername());
            roles.setValue(user.getRoles());
            costumers.setValue(user.getCostumer());

            save.setText("Edit!");
        }

        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.add(username, password, roles);
        if (user!=null) {
            layout.add(costumers);
        }
        layout.add(photo, buttons);

        editDialogue.add(layout);

        save.addClickListener(event -> {
            if (user==null) {
                binder.forField(username).withValidator(StringUtils::hasText, "Specify username!")
                        .withValidator(s -> {
                            return userRepository.findByUsername(s).isEmpty();
                        }, "Can not use this username!").bind(StoreUser::getUsername, StoreUser::setUsername);

                binder.forField(password).withValidator(s -> s.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*\\_-]).{8,}$"), "minimal one uppercase and lowercase English letter, + minimal one digit and one special character!")
                        .bind(StoreUser::getPassword, StoreUser::setPassword);
            }

            binder.forField(roles).withValidator(s -> s!=null, "Select role!!").bind(StoreUser::getRoles, StoreUser::setRoles);

            BinderValidationStatus validation = binder.validate();
            if (!validation.hasErrors()) {

                StoreUser newUser = new StoreUser(username.getValue(), encoder.encode(password.getValue()), roles.getValue(), costumers.getValue());

                if (user!=null) {
                    newUser.setId(user.getId());
                    newUser.setImage(user.getImage());
                    if (!StringUtils.hasText(password.getValue())) { //if we edit the user we may not want to change it's password
                        newUser.setPassword(user.getPassword());
                    }
                }

                if (imageName.get() != null) {
                    newUser.setImage(imageName.get());
                    saveUploadedFileTo(imageName.get(), USERS_IMAGES_FOLDER+"/");
                }

                if (costumers.getValue() == null) { //if customer is not specified (for new  user) it must be created
                    Costumer costumer = new Costumer();
                    costumer.setName(username.getValue());
                    costumerRepository.save(costumer);
                    newUser.setCostumer(costumer);
                }

                userRepository.save(newUser);
                editDialogue.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            }
        });

        cancel.addClickListener(event -> editDialogue.close());
    }

    private void saveUploadedFileTo(String image, String to) {
        if (image != null) {

            InputStream is = buffer.getInputStream(image);

            try {
                FileOutputStream fos = new FileOutputStream(to + image);
                fos.write(is.readAllBytes());
                fos.flush();

                is.close();
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void addDeleteDialogueLayout(Dialog dialog, StoreUser user) {
        H3 question = new H3("Are you sure you want to delete the user \"" + user.getUsername() + "\"?");
        question.getStyle().setMarginBottom("20px");

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(delete, cancel);
        buttons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dialog.add(question, buttons);

        delete.addClickListener(event -> {
            userRepository.delete(user);

            if (user.equals(principal.getUser())) { //it we've deleted ourselves we must logout
                authenticationService.logout();
            }

            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
    }
}
