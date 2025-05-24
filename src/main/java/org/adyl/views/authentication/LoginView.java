package org.adyl.views.authentication;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("/login")
@PageTitle("Login | Bookstore")
@AnonymousAllowed
//BeforeEnterObserver is used before this view becomes active (Before User visits it)
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    Anchor register;
    Anchor home;
    LoginForm loginForm = new LoginForm();

    public LoginView(){

        getElement().getThemeList().add(Lumo.DARK);
        //setting it to full size
        setSizeFull();

        register = new Anchor("/register", "Do not have an account? Register!");
        register.getStyle().setMarginBottom("-5px");
        home = new Anchor("/", "Home");
//        register.setVisible(false);

        //centering content
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        loginForm.addForgotPasswordListener(event ->
                loginForm.getUI().ifPresent(ui -> ui.navigate("/forgot-password"))
        );
        loginForm.setAction("login");//it is the "action" attribute from form (where to send data)
        //Set the LoginForm action to "login" to post the login form to Spring Security

        add(new H1("BookStore!"), loginForm, register, home);
    }


    @Override
    public void beforeEnter(BeforeEnterEvent event) { //Here we would like to display errors before user sees the login form
        if(event.getLocation().getQueryParameters().getParameters().containsKey("error")){
            loginForm.setError(true);
//            register.setVisible(true);
        }
    }
}
