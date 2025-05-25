package org.adyl.views.authentication;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;
import org.adyl.exceptions.ObjectAlreadyPresentException;
import org.adyl.mapper.abstraction.AbstractMapperImpl;
import org.adyl.security.models.StoreUser;
import org.adyl.security.models.dto.StoreRegistrationUserDTO;
import org.adyl.security.services.StoreUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.thymeleaf.util.StringUtils;


@Route("/register")
@AnonymousAllowed
@CssImport("my-styles/style.css")
@PageTitle("Registration | BookStore")
public class RegistrationView extends FlexLayout {
    private BeanValidationBinder<StoreUser> validationBinder = new BeanValidationBinder<>(StoreUser.class);
    private PasswordEncoder encoder;
    private StoreUserService userService;
    private AbstractMapperImpl mapper;
//    private BCryptPasswordEncoder encoder = ApplicationContext.getContext().getBean("encoder", BCryptPasswordEncoder.class);
//    private StoreUserService userService = ApplicationContext.getContext().getBean("storeUserService", StoreUserService.class);
//    private AbstractMapperImpl mapper = ApplicationContext.getContext().getBean("abstractMapperImpl", AbstractMapperImpl.class);

    Span usedError;

    public RegistrationView(@Autowired PasswordEncoder encoder, @Autowired StoreUserService userService, @Autowired AbstractMapperImpl mapper) {
        this.encoder = encoder;
        this.userService = userService;
        this.mapper = mapper;

        getElement().getThemeList().add(Lumo.LIGHT);
        setSizeFull();
        setFlexDirection(FlexDirection.COLUMN);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        FlexLayout mainLayout = new FlexLayout();
        mainLayout.setFlexDirection(FlexDirection.COLUMN);
        mainLayout.getStyle().setPadding("40px 20px");
//        mainLayout.getStyle().setBackgroundColor("#2c3d52");
        mainLayout.setWidth("20%");


        H2 header = new H2("Registration:");
        header.getStyle().setMarginBottom("2%");

        usedError = new Span("This user can not be used");
        usedError.setClassName("error");
        usedError.setVisible(false);


        TextField username = new TextField("Username: ");
        validationBinder.forField(username)
                .withValidator(s -> !StringUtils.isEmpty(s), "Specify the username!").bind(StoreUser::getUsername, StoreUser::setUsername);
        TextField email = new TextField("Email: ");
        validationBinder.forField(email)
                .withValidator(emailStr -> emailStr != null && emailStr.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"),
                        "Enter a valid email address!")
                .bind(StoreUser::getEmail, StoreUser::setEmail);
        PasswordField password = new PasswordField("Password: ");
        PasswordField passwordConfirmation = new PasswordField("Confirm password: ");
        validationBinder.forField(password).withValidator(s -> s.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*\\_-]).{8,}$"), "minimal one uppercase and lowercase English letter, + minimal one digit and one special character!")
                .withValidator(s -> s.equals(passwordConfirmation.getValue()), "Passwords do not match!").bind(StoreUser::getPassword, StoreUser::setPassword);

        Button register = new Button("Register!");
//        register.getThemeNames().add(Lumo.DARK);
        register.getStyle().setMarginTop("15%");
//        register.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Anchor home = new Anchor("/", "Home");

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("min-content");
        formLayout.setWidth("100%");
        formLayout.add(username, email, password, passwordConfirmation);

        mainLayout.add(header, usedError, formLayout, register, home);
        add(mainLayout);
        mainLayout.setAlignContent(ContentAlignment.CENTER);

        setAlignSelf(Alignment.CENTER, formLayout);



        register.addClickListener(event -> {
            usedError.setVisible(false);
            System.out.println("SAVE ATTEMPT");
            StoreUser user = new StoreUser();
            try {
                validationBinder.writeBean(user);
                user.setPassword(encoder.encode(user.getPassword()));
                userService.register(mapper.toDTO(user, StoreRegistrationUserDTO.class));

                event.getSource().getUI().ifPresent(ui -> ui.navigate("/login"));
            } catch (ValidationException e) {
//                System.out.println(e.getFieldValidationErrors());
                throw new RuntimeException(e);
            } catch (ObjectAlreadyPresentException e) {
                usedError.setVisible(true);
            }
        });
    }
}
