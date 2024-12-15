package org.adyl.views.authors;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.adyl.model.Author;
import org.adyl.repository.AuthorRepository;
import org.adyl.views.MainView;

@Route(value = "authors", layout = MainView.class)
@PageTitle("Authore")
@PermitAll
public class AuthorView extends FlexLayout implements HasUrlParameter<Integer> {
    private Integer authorId;
    private AuthorRepository repository;

    public AuthorView(AuthorRepository repository) {
        this.repository = repository;

        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer parameter) {
        this.authorId = parameter;
        drawAuthorInfo(parameter);
    }

    private void drawAuthorInfo(Integer id) {
        Author author = repository.findById(id).orElseThrow(() -> new NotFoundException("Such author was not found!!!"));

        FlexLayout authorInfo = new FlexLayout();
        authorInfo.getStyle().setMarginTop("20px");
        authorInfo.setFlexDirection(FlexDirection.COLUMN);

        Image authorImage = new Image("/images/authors/" + author.getImage(), author.getImage());
        authorImage.getStyle().setBorderRadius("100%");
        authorImage.setWidth("400px");
        authorImage.setHeight("400px");
        authorImage.getStyle().setMarginBottom("20px");

        authorInfo.add(authorImage);

        addAuthorInfo(authorInfo, author);

        add(authorInfo);
    }

    private void addAuthorInfo(FlexComponent container, Author author) {
        FlexLayout fullName = new FlexLayout();
        H4 fullNameHeader = new H4("Full Name:");
        fullNameHeader.getStyle().setMarginLeft("2%");
        fullName.add(fullNameHeader);
        Div fullNameValue = new Div(author.getFirstname() + " " + author.getLastname());
        fullNameValue.getStyle().setMarginRight("2%");
        fullName.add(fullNameValue);
        fullName.setJustifyContentMode(JustifyContentMode.BETWEEN);
        fullName.setClassName("infoItem");

        FlexLayout initials = new FlexLayout();
        H4 initialsHeader = new H4("Initials:");
        initialsHeader.getStyle().setMarginLeft("2%");
        initials.add(initialsHeader);
        Div initialsValue = new Div(author.getInitials());
        initialsValue.getStyle().setMarginRight("2%");
        initials.add(initialsValue);
        initials.setJustifyContentMode(JustifyContentMode.BETWEEN);
        initials.setClassName("infoItem");

        FlexLayout birthDate = new FlexLayout();
        H4 birthDateHeader = new H4("Birth Date:");
        birthDateHeader.getStyle().setMarginLeft("2%");
        birthDate.add(birthDateHeader);
        Div birthDateValue = new Div(author.getBirthDate().toString());
        birthDateValue.getStyle().setMarginRight("2%");
        birthDate.add(birthDateValue);
        birthDate.setJustifyContentMode(JustifyContentMode.BETWEEN);
        birthDate.setClassName("infoItem");

        FlexLayout gender = new FlexLayout();
        H4 genderHeader = new H4("Gender:");
        genderHeader.getStyle().setMarginLeft("2%");
        gender.add(genderHeader);
        Div genderValue = new Div(author.getGender());
        genderValue.getStyle().setMarginRight("2%");
        gender.add(genderValue);
        gender.setJustifyContentMode(JustifyContentMode.BETWEEN);
        gender.setClassName("infoItem");

        TextArea contactDetails = new TextArea("Contact Details");
        contactDetails.setValue(author.getContactDetails());
        contactDetails.setReadOnly(true);
        contactDetails.setWidth("100%");
        contactDetails.getStyle().setMarginTop("10px");
        contactDetails.getStyle().setMarginBottom("10px");

        TextArea otherDetails = new TextArea("Other Details");
        otherDetails.setValue(author.getOtherDetails());
        otherDetails.setReadOnly(true);
        otherDetails.setWidth("100%");

//        FlexLayout contactDetails = new FlexLayout();
//        H4 contactDetailsHeader = new H4("Contact Details:");
//        contactDetailsHeader.getStyle().setMarginLeft("2%");
//        contactDetails.add(contactDetailsHeader);
//        Div contactDetailsValue = new Div(author.getContactDetails());
//        contactDetailsValue.getStyle().setMarginRight("2%");
//        contactDetails.add(contactDetailsValue);
//        contactDetails.setJustifyContentMode(JustifyContentMode.BETWEEN);
//        contactDetails.setClassName("infoItem");

//        FlexLayout otherDetails = new FlexLayout();
//        H4 otherDetailsHeader = new H4("Other details:");
//        otherDetailsHeader.getStyle().setMarginLeft("2%");
//        otherDetails.add(otherDetailsHeader);
//        Div otherDetailsValue = new Div(author.getOtherDetails());
//        otherDetailsValue.getStyle().setMarginRight("2%");
//        otherDetails.add(otherDetailsValue);
//        otherDetails.setJustifyContentMode(JustifyContentMode.BETWEEN);
//        otherDetails.setClassName("infoItem");

        container.add(fullName, initials, birthDate, gender, contactDetails, otherDetails);
    }
}
