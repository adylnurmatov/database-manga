package org.adyl.views.authentication;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.component.button.Button;
import org.adyl.security.services.StoreUserService;

@Route("/forgot-password")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    public ForgotPasswordView(StoreUserService userService) {
        getElement().getThemeList().add(Lumo.DARK);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 header = new H1("Reset your password");
        TextField emailField = new TextField("Enter your email");
        Button sendLink = new Button("Send Reset Link");

        Span message = new Span();
        message.setVisible(false);

        sendLink.addClickListener(e -> {
            String email = emailField.getValue();
            if (email == null || email.isBlank()) {
                message.setText("Please enter your email.");
                message.setVisible(true);
                return;
            }
            userService.sendResetToken(email);
            message.setText("If this email exists, a reset link has been sent.");
            message.setVisible(true);
        });


        Anchor backToLogin = new Anchor("/login", "Back to login");

        add(header, emailField, sendLink, message, backToLogin);
    }
}


