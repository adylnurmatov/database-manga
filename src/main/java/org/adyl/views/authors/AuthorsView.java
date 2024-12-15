package org.adyl.views.authors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.adyl.model.Author;
import org.adyl.repository.AuthorRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;
import org.adyl.views.MainView;
import org.springframework.util.StringUtils;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "authors", layout = MainView.class)
@PermitAll
@CssImport("my-styles/style.css")
@PageTitle("Authori")
//@AnonymousAllowed
public class AuthorsView extends FlexLayout {
    private List<String> roles;
    private StoreUserDetails userDetails;
    private AuthenticationService authenticationService;
    private AuthorRepository authorRepository;
    private MultiFileMemoryBuffer buffer;
    private List<Author> authorsList;
    private Grid<Author> authorsGrid;

    private static final String AUTHORS_IMAGES_FOLDER = "src/main/resources/static/images/authors";

    public AuthorsView(AuthenticationService authenticationService, AuthorRepository authorRepository) {
        this.authenticationService = authenticationService;
        this.authorRepository = authorRepository;

        userDetails = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        roles = userDetails.getAuthorities().stream().map(a -> a.getAuthority()).toList();

        setSizeFull();
        setFlexDirection(FlexDirection.ROW);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setFlexWrap(FlexWrap.WRAP);

        if (!roles.isEmpty() && (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN"))) {
            drawForAdministration();
        } else {
            drawForUser();
        }
    }

    private void drawForUser() {
        authorsList = authorRepository.findAll();

        authorsList.forEach(this::drawAuthor);
    }

    private void drawForAdministration() {
        setFlexDirection(FlexDirection.COLUMN);
        setJustifyContentMode(JustifyContentMode.START);

        H1 header = new H1("Authors:");
        setAlignSelf(Alignment.CENTER, header);
        header.getStyle().setMarginTop("20px");
        header.getStyle().setMarginBottom("20px");

        add(header);

        List <Author> authorsList = authorRepository.findAll();

        authorsGrid = new Grid<>(Author.class, false);
        authorsGrid.setAllRowsVisible(true);
        authorsGrid.setHeight("min-content");
        authorsGrid.setMaxHeight("65%");
        authorsGrid.setWidth("95%");
        authorsGrid.getStyle().setMarginTop("20px");
        authorsGrid.getStyle().setMarginLeft("auto");
        authorsGrid.getStyle().setMarginRight("auto");


        authorsGrid.addColumn(Author::getFirstname).setHeader("Firstname");
        authorsGrid.addColumn(Author::getLastname).setHeader("Lastname");
        authorsGrid.addColumn(Author::getInitials).setHeader("Initials").setAutoWidth(true);
        authorsGrid.addColumn(Author::getBirthDate).setHeader("BirthDate");
        authorsGrid.addColumn(Author::getGender).setHeader("Gender").setAutoWidth(true);
        authorsGrid.addColumn(Author::getContactDetails).setHeader("Contact Details").setAutoWidth(true);
        authorsGrid.addColumn(Author::getOtherDetails).setHeader("Other Details").setAutoWidth(true);
        authorsGrid.addColumn(
                LitRenderer.<Author> of("<vaadin-avatar img=\"/images/authors/${item.pictureUrl}\" name=\"${item.fullName}\" alt=\"User avatar\"></vaadin-avatar>").withProperty("pictureUrl", Author::getImage)
                                                                                                                                                                                   .withProperty("fullName", Author::getImage))
                .setHeader("Image").setAutoWidth(true).setFlexGrow(0);
        if (!roles.isEmpty() && roles.contains("ROLE_ADMIN")) {
            authorsGrid.addComponentColumn(author -> {
                Button edit = new Button("Edit");
                edit.addClickListener(event -> {
                    Dialog editDialogue = new Dialog("Edit Author info");

                    addAddOrEditDialogueLayout(editDialogue, author);

                    editDialogue.open();
                });
                return edit;
            }).setHeader("Edit");

            authorsGrid.addComponentColumn(author -> {
                Button delete = new Button("Delete");
                delete.addClickListener(event -> {
                    Dialog deleteDialogue = new Dialog("Delete Author Confirmation");

                    addDeleteDialogueLayout(deleteDialogue, author);

                    deleteDialogue.open();
                });

                return delete;
            }).setHeader("Delete");
        }

        authorsGrid.setItems(authorsList);

        add(authorsGrid);

        Button add = new Button("Add new");

        add(add);
        add.getStyle().setMarginTop("10px");
        setAlignSelf(Alignment.END, add);

        add.setWidth("max-content");
        add.getStyle().setMarginRight("5%");
        add.getStyle().setPadding("5px 10px");

        add.addClickListener(event -> {
            Dialog addDialog = new Dialog("Add new Author");

            addAddOrEditDialogueLayout(addDialog, null);

            addDialog.open();
        });
    }

    private void drawAuthor(Author author) {
        Div authorDiv = new Div();
        authorDiv.setClassName("author");

        Image authorPhoto = new Image("/images/authors/" + author.getImage(), author.getImage());
        authorPhoto.addClickListener(event -> {
            event.getSource().getUI().ifPresent(ui -> ui.navigate("/authors/" + author.getId()));
        });
        authorPhoto.setClassName("authorImage");

        H3 fullName = new H3(author.getFirstname() + " " + author.getLastname());
        fullName.addClickListener(event -> {
            event.getSource().getUI().ifPresent(ui -> ui.navigate("/authors/" + author.getId()));
        });
        fullName.setClassName("authorFullName");

        authorDiv.add(authorPhoto, fullName);

        add(authorDiv);
    }

    private void addAddOrEditDialogueLayout(Dialog editDialogue, Author author){
        Binder<Author> binder = new Binder<>();

        TextField firstName = new TextField("Firstname: ");
        TextField lastName = new TextField("Lastname: ");
//        TextField initials = new TextField("Initials: ");
        DatePicker birthDate = new DatePicker("Birth date: ");
        ComboBox<String> gender = new ComboBox<>("Gender: ", "M", "F", "U");
        gender.setValue("U");
        gender.setAllowCustomValue(false);
        TextArea contactDetails = new TextArea("Contact details: ");
        TextArea otherDetails = new TextArea("Other details: ");
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

        if (author != null){
            firstName.setValue(author.getFirstname());
            lastName.setValue(author.getLastname());
//            initials.setValue(author.getInitials());
            birthDate.setValue(author.getBirthDate());
            gender.setValue(author.getGender());
            contactDetails.setValue(author.getContactDetails());
            otherDetails.setValue(author.getOtherDetails());

            save.setText("Edit!");
        }



        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.add(firstName, lastName);

//        if (author!=null)
//            layout.add(initials);

        layout.add(birthDate, gender, contactDetails, otherDetails, photo, buttons);

        editDialogue.add(layout);

        save.addClickListener(event -> {
            binder.forField(firstName).withValidator(s -> StringUtils.hasText(s), "Firstname can not be empty!").bind(Author::getFirstname, Author::setFirstname);
            binder.forField(lastName).withValidator(s -> StringUtils.hasText(s), "Lastname can not be empty!").bind(Author::getLastname, Author::setLastname);
//            if (author!=null)
//                binder.forField(initials).withValidator(s -> StringUtils.hasText(s) && s.equals(s.toUpperCase()) && s.length()==2, "Initials - is two letters in uppercase!").bind(Author::getInitials, Author::setInitials);
            binder.forField(birthDate).withValidator(d -> d!=null, "Specify The date!")
                    .withValidator(d -> {
                        LocalDate now = LocalDate.now();
                        return now.getYear() - d.getYear() >= 18;
                    }, "Author must be an adult!").bind(Author::getBirthDate, Author::setBirthDate);

            BinderValidationStatus validation = binder.validate();

            //if we want errors validation to disappear after field looses focus
//            binder.removeBinding(firstName);
//            binder.removeBinding(lastName);
//            binder.removeBinding(initials);

            //Saving file if present
            if (!validation.hasErrors()) {
                String initials = String.valueOf(firstName.getValue().toUpperCase().charAt(0)) +
                                     String.valueOf(lastName.getValue().toUpperCase().charAt(0));
                Author newAuthor = new Author(firstName.getValue(), lastName.getValue(), initials, birthDate.getValue(), gender.getValue(), contactDetails.getValue(), otherDetails.getValue());

                if (author!=null) {
                    newAuthor.setId(author.getId());
//                    newAuthor.setInitials(initials.getValue());
                    newAuthor.setImage(author.getImage());
                }

                if (imageName.get() != null) {
                    newAuthor.setImage(imageName.get());
                    saveUploadedFileTo(imageName.get(), AUTHORS_IMAGES_FOLDER+"/");
                }
//                System.out.println("SAVING " + newAuthor);


                authorRepository.save(newAuthor);
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

    private void addDeleteDialogueLayout(Dialog dialog, Author author) {
        H3 question = new H3("Are you sure you want to delete the author \"" + author.getFirstname() + " " + author.getLastname() + "\"?");
        question.getStyle().setMarginBottom("20px");

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(delete, cancel);
        buttons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dialog.add(question, buttons);

        delete.addClickListener(event -> {
            authorRepository.delete(author);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
    }
}

//todo:multiply for another tables