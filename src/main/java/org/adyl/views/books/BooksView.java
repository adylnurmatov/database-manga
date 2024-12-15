package org.adyl.views.books;

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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.adyl.model.Author;
import org.adyl.model.Book;
import org.adyl.model.BookCategory;
import org.adyl.repository.AuthorRepository;
import org.adyl.repository.BookCategoryRepository;
import org.adyl.repository.BookRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;
import org.adyl.views.MainView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Route(value = "books", layout = MainView.class)
@PermitAll
@CssImport("my-styles/style.css")
@PageTitle("Libri")
public class BooksView extends FlexLayout implements BeforeEnterObserver {
    private BookRepository bookRepository;
    private AuthorRepository authorRepository;
    private BookCategoryRepository categoryRepository;
    private StoreUserDetails principal;
    private List<String> roles;
    private AuthenticationService authenticationService;
    private Grid<Book> booksGrid;
    private MultiFileMemoryBuffer buffer;
    private static final String BOOKS_IMAGES_FOLDER = "src/main/resources/static/images/books";


    public BooksView(@Autowired AuthenticationService authenticationService, BookRepository bookRepository,
                                                                             AuthorRepository authorRepository,
                                                                             BookCategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.authenticationService = authenticationService;

        //This Endpoint is secured, so Principal will never be anonymousUser
        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        roles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        setAlignItems(Alignment.START);
        setJustifyContentMode(JustifyContentMode.START);
        setWidth("100%");
        setHeight("100%");
        setFlexWrap(FlexWrap.WRAP);
    }

    private void drawForUser(List<Book> books){
        removeAll();
        books.forEach(this::drawBook);
    }

    private void drawBook(Book b) {
        FlexLayout book = new FlexLayout();
        book.setFlexDirection(FlexDirection.COLUMN);
        book.setAlignItems(Alignment.CENTER);
        book.setJustifyContentMode(JustifyContentMode.CENTER);
        book.setClassName("book");

        Image bookImage = new Image("/images/books/" + b.getImage(), b.getImage());
        bookImage.setClassName("labelImage");

        book.add(bookImage);
        book.add(new H3(b.getTitle()));

        book.addClickListener(event -> event.getSource().getUI().ifPresent(ui -> ui.navigate("books/" + b.getId())));

        add(book);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String filter = event.getLocation().getQueryParameters().getSingleParameter("filter").orElse(""); //we can access all the parameters from Location object

        List<Book> books = bookRepository.findAll();

        if (StringUtils.hasText(filter)) {
            if (filter.contains("category")) {
                books = bookRepository.findAllByCategory_Code(Integer.parseInt(filter.substring(9)));
            }
        }

        if (!roles.isEmpty() && (roles.contains("ROLE_MANAGER") || roles.contains("ROLE_ADMIN")) ) {
            drawForAdministration(books);
        } else {
            drawForUser(books);
        }
    }

    private void drawForAdministration(List<Book> books) {
        removeAll();
        setFlexDirection(FlexDirection.COLUMN);
        setJustifyContentMode(JustifyContentMode.START);

        H1 header = new H1("Books:");
        setAlignSelf(Alignment.CENTER, header);
        header.getStyle().setMarginTop("20px");
        header.getStyle().setMarginBottom("20px");

        add(header);

        booksGrid = new Grid<>(Book.class, false);
        booksGrid.setAllRowsVisible(true);
        booksGrid.setHeight("min-content");
        booksGrid.setMaxHeight("65%");
        booksGrid.setWidth("95%");
        booksGrid.getStyle().setMarginTop("20px");
        booksGrid.getStyle().setMarginLeft("auto");
        booksGrid.getStyle().setMarginRight("auto");

        booksGrid.addColumn(Book::getTitle).setHeader("Title").setAutoWidth(true);
        booksGrid.addColumn(Book::getIsbn).setHeader("ISBN").setAutoWidth(true);
        booksGrid.addComponentColumn(book -> {
            Anchor authorLink = new Anchor("/authors/"+book.getAuthor().getId(), book.getAuthor().getFirstname() + " " + book.getAuthor().getLastname());
            return authorLink;
        }).setHeader("Author").setAutoWidth(true);
        booksGrid.addComponentColumn(book -> new Anchor("/categories", book.getCategory().getCategoryDescription())).setHeader("Category").setAutoWidth(true);


        booksGrid.addColumn(Book::getPublicationDate).setHeader("Publication date").setAutoWidth(true);
        booksGrid.addColumn(Book::getDateAcquired).setHeader("Acquirement date").setAutoWidth(true);
        booksGrid.addColumn(Book::getRecommendedPrice).setHeader("Price").setAutoWidth(true);
        booksGrid.addColumn(Book::getComments).setHeader("Comments").setAutoWidth(true);
        booksGrid.addColumn(
                        LitRenderer.<Book> of("<img class= 'grid-book-image' style=\"height: var(--lumo-size-m); margin-right: var(--lumo-space-s);\" src=\"/images/books/${item.pictureUrl}\" alt=\"${item.title}\" />").withProperty("pictureUrl", Book::getImage)
                                                                                                                                                                                .withProperty("title", Book::getTitle))
                .setHeader("Image").setAutoWidth(true).setFlexGrow(0);

        if (!roles.isEmpty() && roles.contains("ROLE_ADMIN")) {
            booksGrid.addComponentColumn(book -> {
                Button edit = new Button("Edit");
                edit.addClickListener(event -> {
                    Dialog editDialogue = new Dialog("Edit Book info");

                    addAddOrEditDialogueLayout(editDialogue, book);

                    editDialogue.open();
                });
                return edit;
            }).setHeader("Edit");

            booksGrid.addComponentColumn(book -> {
                Button delete = new Button("Delete");
                delete.addClickListener(event -> {
                    Dialog deleteDialogue = new Dialog("Delete Book Confirmation");

                    addDeleteDialogueLayout(deleteDialogue, book);

                    deleteDialogue.open();
                });

                return delete;
            }).setHeader("Delete");
        }

        booksGrid.setItems(books);

        add(booksGrid);

        Button add = new Button("Add new");

        add(add);
        add.getStyle().setMarginTop("10px");
        setAlignSelf(Alignment.END, add);

        add.setWidth("max-content");
        add.getStyle().setMarginRight("5%");
        add.getStyle().setPadding("5px 10px");

        add.addClickListener(event -> {
            Dialog addDialog = new Dialog("Add new Book");

            addAddOrEditDialogueLayout(addDialog, null);

            addDialog.open();
        });
    }

    private void addAddOrEditDialogueLayout(Dialog editDialogue, Book book){
        Binder<Book> binder = new Binder<>();

        ComboBox<Author> author = new ComboBox<>("Author: ", authorRepository.findAll());
        author.setItemLabelGenerator(item -> item.getFirstname() + " " + item.getLastname()); //Setting option label
        author.setRenderer(getAuthorComboBoxRenderer()); //Setting option renderer for custom rendering
//        author.setValue(book.getAuthor());
        author.setAllowCustomValue(false);

        ComboBox<BookCategory> categories = new ComboBox<>("Category: ", categoryRepository.findAll());
        categories.setItemLabelGenerator(item -> item.getCategoryDescription());
//        categories.setValue(book.getCategory());
        categories.setAllowCustomValue(false);

        TextField isbn = new TextField("ISBN: ");
//        isbn.setPattern("$/[0-9]{3}-[0-9]-[0-9]{2}-[0-9]{6}-[0-9]$/");

        DatePicker publicationDate = new DatePicker("Publication date: ");
        DatePicker dateAcquired = new DatePicker("Acquirement date: ");

        TextField title = new TextField("Title: ");

        NumberField price = new NumberField("Price: ");
        price.setMin(1.0);
        price.setStep(0.1);
        price.setValue(1.0);

        TextField comments = new TextField("Comments: ");

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

        if (book != null){
            author.setValue(book.getAuthor());
            categories.setValue(book.getCategory());
            isbn.setValue(book.getIsbn());
            publicationDate.setValue(book.getPublicationDate());
            dateAcquired.setValue(book.getDateAcquired());
            title.setValue(book.getTitle());
            price.setValue(book.getRecommendedPrice());
            comments.setValue(book.getComments());

            save.setText("Edit!");
        }



        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.add(author, categories, isbn, publicationDate, dateAcquired, title, comments, price, photo, buttons);

        editDialogue.add(layout);

        save.addClickListener(event -> {
            binder.forField(isbn).withValidator(s -> s.matches("^\\d{3}\\-\\d\\-\\d{2}\\-\\d{6}\\-\\d$"), "ISBN must be of format 000-0-00-000000-0!").bind(Book::getIsbn, Book::setIsbn);
            binder.forField(publicationDate).withValidator(d -> d!=null, "Specify The publication date!").bind(Book::getPublicationDate, Book::setPublicationDate);
            binder.forField(dateAcquired).withValidator(d -> d!=null, "Specify The acquirement date!").bind(Book::getDateAcquired, Book::setDateAcquired);
            binder.forField(title).withValidator(s -> StringUtils.hasText(s), "Specify the title!").bind(Book::getTitle, Book::setTitle);
            binder.forField(price).withValidator(p -> p > 0, "Price must be grather than 0!").bind(Book::getRecommendedPrice, Book::setRecommendedPrice);
            binder.forField(author).withValidator(a -> a!=null, "Specify The author!").bind(Book::getAuthor, Book::setAuthor);
            binder.forField(categories).withValidator(c -> c!=null, "Specify The category!").bind(Book::getCategory, Book::setCategory);

            BinderValidationStatus validation = binder.validate();
            if (!validation.hasErrors()) {
                Book newBook = new Book(author.getValue(), categories.getValue(), isbn.getValue(), publicationDate.getValue(), dateAcquired.getValue(), title.getValue(), price.getValue(), comments.getValue());

                if (book!=null) {
                    newBook.setId(book.getId());
                    newBook.setImage(book.getImage());
                }

                if (imageName.get() != null) {
                    newBook.setImage(imageName.get());
                    saveUploadedFileTo(imageName.get(), BOOKS_IMAGES_FOLDER+"/");
                }

                bookRepository.save(newBook);
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

    private void addDeleteDialogueLayout(Dialog dialog, Book book) {
        H3 question = new H3("Are you sure you want to delete the book \"" + book.getTitle() + "\" (" + book.getIsbn() + ")?");
        question.getStyle().setMarginBottom("20px");

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(delete, cancel);
        buttons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dialog.add(question, buttons);

        delete.addClickListener(event -> {
            bookRepository.delete(book);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
    }

    public Renderer<Author> getAuthorComboBoxRenderer() {
        StringBuilder tpl = new StringBuilder();
        tpl.append("<div style=\"display: flex;\">");
        tpl.append(
                "  <vaadin-avatar style='margin-right: 10px;' img=\"/images/authors/${item.pictureUrl}\" name=\"${item.firstName}-${item.lastName}\" alt=\"Portrait of ${item.firstName} ${item.lastName}\"></vaadin-avatar>");
//                "  <img style=\"height: var(--lumo-size-m); margin-right: var(--lumo-space-s);\" src=\"/images/authors/${item.pictureUrl}\" alt=\"Portrait of ${item.firstName} ${item.lastName}\" />");
        tpl.append("  <div>");
        tpl.append("    ${item.firstName} ${item.lastName}");
        tpl.append("</div>");

        return LitRenderer.<Author> of(tpl.toString())
                .withProperty("pictureUrl", Author::getImage)
                .withProperty("firstName", Author::getFirstname)
                .withProperty("lastName", Author::getLastname);
    }
}
