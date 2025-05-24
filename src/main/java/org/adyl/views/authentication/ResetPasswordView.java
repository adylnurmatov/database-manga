package org.adyl.views.authentication;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;
import org.adyl.security.services.StoreUserService;

import java.util.List;

@Route("/reset-password")
@PageTitle("Set New Password | Bookstore")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private final StoreUserService userService;
    private String token;

    public ResetPasswordView(StoreUserService userService) {
        this.userService = userService;
        getElement().getThemeList().add(Lumo.DARK);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        token = event.getLocation().getQueryParameters().getParameters()
                .getOrDefault("token", List.of(""))
                .get(0);

        if (token == null || token.isBlank()) {
            add(new H1("Invalid reset token"));
            return;
        }

        H1 header = new H1("Set a new password");
        PasswordField passwordField = new PasswordField("New password");
        Button resetButton = new Button("Reset Password");

        Span resultMessage = new Span();
        resultMessage.setVisible(false);

        resetButton.addClickListener(e -> {
            boolean success = userService.resetPassword(token, passwordField.getValue());
            if (success) {
                resultMessage.setText("Password reset successful. You may now log in.");
                getUI().ifPresent(ui -> ui.navigate("/login"));
            } else {
                resultMessage.setText("Invalid or expired reset token.");
            }
            resultMessage.setVisible(true);
        });

        Anchor backToLogin = new Anchor("/login", "Back to login");

        add(header, passwordField, resetButton, resultMessage, backToLogin);
    }
}

//@Route("/reset-password")
//@AnonymousAllowed
//public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {
//
//    private final StoreUserService userService;
//
//    public ResetPasswordView(StoreUserService userService) {
//        this.userService = userService;
//    }
//
//    @Override
//    public void beforeEnter(BeforeEnterEvent event) {
//        String token = event.getLocation().getQueryParameters().getParameters()
//                .getOrDefault("token", List.of("")).get(0);
//
//        if (token.isBlank()) {
//            add(new H1("Invalid or missing token"));
//            return;
//        }
//
//        PasswordField passwordField = new PasswordField("New Password");
//        Button resetBtn = new Button("Reset Password");
//
//        resetBtn.addClickListener(e -> {
//            boolean success = userService.resetPassword(token, passwordField.getValue());
//            if (success) {
//                Notification.show("Password reset successfully!");
//                getUI().ifPresent(ui -> ui.navigate("/login"));
//            } else {
//                Notification.show("Invalid or expired token");
//            }
//        });
//
//        add(new H1("Reset Password"), passwordField, resetBtn);
//    }
//}

