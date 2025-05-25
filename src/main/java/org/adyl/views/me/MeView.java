package org.adyl.views.me;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.security.PermitAll;
import org.adyl.exceptions.PasswordFormatException;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.security.models.StoreUser;
import org.adyl.security.models.dto.StoreUserDTO;
import org.adyl.security.services.StoreUserService;
import org.adyl.service.AuthenticationService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


@Route(value = "/me")
@RouteAlias(value = "/users/me")
@PageTitle("Me")
@PermitAll
public class MeView extends FlexLayout {

//    private OrderRepository orderRepository;
    private AuthenticationService authenticationService;
    StoreUserDetails principal;
    private StoreUserService storeUserService;
    private MultiFileMemoryBuffer buffer;

    private static final String USERS_IMAGES_PATH = "src/main/resources/static/images/users";

    public MeView(/*@Autowired OrderRepository orderRepository,*/ AuthenticationService authenticationService, StoreUserService storeUserService) {
//        this.orderRepository = orderRepository;
        this.authenticationService = authenticationService;
        this.storeUserService = storeUserService;

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();

        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setAlignItems(Alignment.CENTER);
//        setJustifyContentMode(JustifyContentMode.CENTER);

        getElement().getThemeList().add(Lumo.LIGHT);

        Image image = new Image("/images/users/" + principal.getUser().getImage(), principal.getUser().getImage());
        image.setWidth("200px");
        image.setHeight("200px");
        image.getStyle().setBorderRadius("100%");
        image.getStyle().setMarginTop("20px");
        image.getStyle().setMarginBottom("20px");

        H1 name = new H1("Hello, " + principal.getUser().getUsername() + '!');

        add(image, name);

        VerticalLayout userData = new VerticalLayout();
        userData.getStyle().setBorder("1px dashed");
        userData.getStyle().setBorderRadius("10px");
        userData.setWidth("max-content");
        userData.getStyle().setPadding("10px 20px");
        userData.getStyle().setMargin("10px");

        Span username = new Span("Username: " + principal.getUsername());
        Button passwordRefresh = new Button("Change password");
        passwordRefresh.getStyle().setMargin("auto");
        passwordRefresh.addClickListener(event -> {
            Dialog refreshPasswordDialog = new Dialog("Refresh the password...");

            refreshPasswordDialog.add(getRefreshPasswordDialogLayout(refreshPasswordDialog));

            refreshPasswordDialog.open();
        });

        Button changeUsername = new Button("Change username");
        changeUsername.getStyle().setMargin("auto");

        changeUsername.addClickListener(event -> {
            Dialog changeUsernameDialog = new Dialog("Change Username...");
            changeUsernameDialog.add(getChangeUsernameLayout(changeUsernameDialog));
            changeUsernameDialog.open();
        });

        Button changePic = new Button("Change picture");
        changePic.getStyle().setMargin("auto");

        changePic.addClickListener(event -> {
            Dialog changePicDialog = new Dialog("Change you picture...");
            changePicDialog.add(getChangePictureLayout(changePicDialog));
            changePicDialog.open();
        });


        Span role = new Span("Current role: " + ((List)principal.getAuthorities()).get(0).toString().replace("ROLE_", ""));

        Span customerLabel = new Span("Customer: ");
        Anchor customerLink = new Anchor("/customers/me", principal.getUser().getCustomer().getName());
        Div customer = new Div(customerLabel, customerLink);

        Span ordersLabel = new Span("Orders: ");
        Anchor ordersLink = new Anchor("/orders/me", "see all the orders");
        Div orders = new Div(ordersLabel, ordersLink);

        userData.add(username, role, customer);
        if (principal.getUser().getRoles()!=null && !principal.getUser().getRoles().contains("ROLE_MANAGER") && !principal.getUser().getRoles().contains("ROLE_ADMIN"))
            userData.add(orders);
        userData.add(changeUsername, passwordRefresh, changePic);

        add(userData);


//        add(new H3(principal.toString()));
//        add(new H3(principal.getUser().getCustomer().toString()));
//        add(new H3(orderRepository.findAllByCustomer(principal.getUser().getCustomer()).toString()));
//            add(new H3(Cart.toString()));


        Button logout = new Button("Logout", event -> {
//            VaadinSession.getCurrent().getSession().invalidate();
            authenticationService.logout();
//            getUI().ifPresent(ui -> ui.navigate("/"));

//            VaadinSession.getCurrent().getSession().
//            UI.getCurrent().getPage().reload();
//            getUI().ifPresent(ui -> ui.getPage().reload());
        });
        logout.getStyle().setMargin("5px");

        Button home = new Button("Home", event -> {
            getUI().ifPresent(ui -> ui.navigate("/"));
        });
        home.getStyle().setMargin("5px");

        add(new Div(logout, home));
    }

    private VerticalLayout getRefreshPasswordDialogLayout(Dialog dialog){
        Binder<StoreUser> binder = new Binder<>(StoreUser.class);

        VerticalLayout verticalLayout = new VerticalLayout(Alignment.STRETCH);

        PasswordField password = new PasswordField("Enter new password: ");
        PasswordField confirmation = new PasswordField("Confirm the password: ");

        Button change = new Button("Change!");
        change.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        verticalLayout.getStyle().set("width", "25rem").set("max-width", "100%");
        verticalLayout.add(password, confirmation, change);

        change.addClickListener(event -> {
//            System.out.println(binder.validate());

            binder.forField(password).withValidator(s -> s.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*\\_-]).{8,}$"), "minimal one uppercase and lowercase English letter, + minimal one digit and one special character!")
                            .bind(StoreUser::getPassword, StoreUser::setPassword);
            binder.forField(confirmation).withValidator(s -> s.equals(password.getValue()), "Passwords do not match!")
                    .bind(StoreUser::getPassword, StoreUser::setPassword);

            BinderValidationStatus validationStatus = binder.validate();

            binder.removeBinding(password);
            binder.removeBinding(confirmation);

            if (!validationStatus.hasErrors()) {
                try {
                    storeUserService.updatePasswordFor(password.getValue(), principal.getUser());
                    dialog.close();

                    showSuccess("Password has been successfully changed!!");
                } catch (PasswordFormatException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return verticalLayout;
    }

    private VerticalLayout getChangeUsernameLayout(Dialog dialog) {
        VerticalLayout verticalLayout = new VerticalLayout(Alignment.STRETCH);

        TextField newName = new TextField("Enter new name: ");

        Button change = new Button("Change!");
        change.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        verticalLayout.getStyle().set("width", "25rem").set("max-width", "100%");
        verticalLayout.add(newName, change);

        change.addClickListener(event -> {
            newName.setErrorMessage("");
            newName.setInvalid(false);

            if (storeUserService.findByUsername(newName.getValue()) != null) {
                newName.setErrorMessage("Username is already used!");
                newName.setInvalid(true);
            } else {
                StoreUserDTO currentUser = storeUserService.findByUsername(principal.getUsername());
                currentUser.setUsername(newName.getValue());
                storeUserService.update(currentUser.getId(), currentUser);
                showSuccess("Username has been successfully changed!");
                principal.getUser().setUsername(newName.getValue());
                dialog.close();
            }
        });

        return verticalLayout;
    }

    private VerticalLayout getChangePictureLayout(Dialog dialog) {
        VerticalLayout verticalLayout = new VerticalLayout(Alignment.STRETCH);

        FlexLayout photo = new FlexLayout();
        photo.setFlexDirection(FlexDirection.COLUMN);

        Span choosePhotoLabel = new Span("Choose new photo:");
        buffer = new MultiFileMemoryBuffer();
        Upload image = new Upload(buffer);
        image.setAcceptedFileTypes("image/png", "image/jpeg");

        AtomicReference<String> imageName = new AtomicReference<>("");
        image.addSucceededListener(event -> {
            imageName.set(event.getFileName());
        });

        photo.add(choosePhotoLabel, image);

        Button change = new Button("Change!");
        change.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        verticalLayout.getStyle().set("width", "25rem").set("max-width", "100%");
        verticalLayout.add(photo, change);

        change.addClickListener(event -> {
            if (!imageName.get().equals("")){
                principal.getUser().setImage(imageName.get());
                storeUserService.update(principal.getUser().getId(), principal.getUser());
                saveUploadedFileTo(imageName.get(), USERS_IMAGES_PATH+"/");
                dialog.close();
                showSuccess("Image has been updated Successfully!");
            } else {
                dialog.close();
            }
        });

        return verticalLayout;
    }


    private void showSuccess(String message){
        Dialog success = new Dialog("Success!");
        VerticalLayout layout = new VerticalLayout(Alignment.CENTER);
        layout.add(new H4(message));

        Button ok = new Button("Ok", event1 -> {
            success.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        layout.add(ok);

        success.add(layout);

        success.open();
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
}
