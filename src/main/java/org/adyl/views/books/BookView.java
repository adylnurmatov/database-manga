package org.adyl.views.books;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import org.adyl.model.Book;
import org.adyl.model.Cart;
import org.adyl.repository.BookRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;
import org.adyl.views.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "books", layout = MainView.class)
@PermitAll
@CssImport("/my-styles/style.css")
@PageTitle("Libro")
public class BookView extends FlexLayout implements HasUrlParameter<Integer> {
    private Integer book_id;
    private BookRepository bookRepository;
    private Cart cart;
    private StoreUserDetails principal;
    private List<String> roles;
    private AuthenticationService authenticationService;

    public BookView(@Autowired BookRepository bookRepository, @Autowired Cart Cart, AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
        this.bookRepository = bookRepository;
        this.cart = Cart;

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        roles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        setFlexDirection(FlexDirection.ROW);
//        setAlignItems(Alignment.CENTER);

        setHeight("100%");
    }

    @Override
    public void setParameter(BeforeEvent event, Integer parameter) {
        this.book_id = parameter;
        initInterfaceForBook(book_id);
    }

    private void initInterfaceForBook(Integer book_id){
        Book book = bookRepository.findById(book_id).orElseThrow(() -> new NotFoundException("Such book was not found!"));


        Div image = new Div();
        image.setClassName("bookImage");

        Image bookImage = new Image("/images/books/" + book.getImage(), book.getImage());
        bookImage.setWidth("100%");
        bookImage.setHeight("100%");
        image.add(bookImage);


        FlexLayout info = new FlexLayout();
        info.setClassName("info");
        addBookInfo(info, book);

        FlexLayout buy = new FlexLayout();
        buy.setClassName("book-buy");

        Span price = new Span(book.getRecommendedPrice().toString() + " MDL");
        price.setClassName("book-price");
        buy.add(price);

        Button addToCart = new Button("Add to chart!");
//        if (cart.getBookList().contains(book)){
//            addToCart.setText("Remove from Cart!");
//        }

        FlexLayout counter = new FlexLayout();
        counter.setFlexDirection(FlexDirection.ROW);
        counter.setWidth("250px");

        Button plus = new Button("+");

        NumberField numberField = new NumberField();
        numberField.setStep(1);
        numberField.setMin(1);
//        if (cart.getBookMap().get(book) != null)
//            numberField.setValue(Double.valueOf(cart.getBookMap().get(book)));
//        else
//            numberField.setValue(1.0);
        numberField.setValue(1.0);

        numberField.setWidth("45%");

        Button minus = new Button("-");

        counter.add(plus, numberField, minus);

        addToCart.addClickListener(event -> {
            Integer amount = Math.toIntExact(Math.round(numberField.getValue()));

            if (cart.getBookMap().containsKey(book)) {
                cart.getBookMap().put(book, cart.getBookMap().get(book) + amount);
            } else {
                cart.getBookMap().put(book, amount);
            }
//            if (!cart.getBookList().contains(book)){
//                cart.getBookList().add(book);
//                event.getSource().setText("Remove from Cart!");
//            } else {
//                cart.getBookList().remove(book);
//                event.getSource().setText("Add to Cart!");
//            }
        });

        plus.addClickListener(event -> {
            numberField.setValue(numberField.getValue()+1.0);
        });
        minus.addClickListener(event -> {
            if (numberField.getValue() > 1) {
                numberField.setValue(numberField.getValue()-1.0);
            }
        });

        buy.add(counter, addToCart);

        add(image, info);
        if (!roles.isEmpty() && roles.contains("ROLE_USER")) {
            add(buy);
        } else {
            setJustifyContentMode(JustifyContentMode.CENTER);
        }
    }

    private void addBookInfo(FlexComponent container, Book book){
        FlexLayout title = new FlexLayout();
        title.add(new Div("Title"));
        title.add(new Div(book.getTitle()));
        title.setJustifyContentMode(JustifyContentMode.BETWEEN);
        title.setClassName("infoItem");

        FlexLayout author = new FlexLayout();
        author.add(new Div("Author"));
        author.add(new Anchor("/authors/" + book.getId(), book.getAuthor().getFirstname() + " " + book.getAuthor().getLastname()));
        author.setJustifyContentMode(JustifyContentMode.BETWEEN);
        author.setClassName("infoItem");

        FlexLayout category = new FlexLayout();
        category.add(new Div("Category"));
        category.add(new Div(book.getCategory().getCategoryDescription()));
        category.setJustifyContentMode(JustifyContentMode.BETWEEN);
        category.setClassName("infoItem");

        FlexLayout isbn = new FlexLayout();
        isbn.add(new Div("ISBN"));
        isbn.add(new Div(book.getIsbn()));
        isbn.setJustifyContentMode(JustifyContentMode.BETWEEN);
        isbn.setClassName("infoItem");

        FlexLayout publicationDate = new FlexLayout();
        publicationDate.add(new Div("Publication date"));
        publicationDate.add(new Div(book.getPublicationDate().toString()));
        publicationDate.setJustifyContentMode(JustifyContentMode.BETWEEN);
        publicationDate.setClassName("infoItem");

//        FlexLayout additionalInfo = new FlexLayout();
//        additionalInfo.setFlexDirection(FlexDirection.COLUMN);
//        additionalInfo.add(new Div());
        TextArea description = new TextArea("Additional info");
        description.setValue(book.getComments());
        description.setReadOnly(true);
//        additionalInfo.add(description);
//        additionalInfo.setJustifyContentMode(JustifyContentMode.BETWEEN);
//        additionalInfo.setClassName("infoItem");


//        container.add(title, author, category, isbn, publicationDate, additionalInfo);
        container.add(title, author, category, isbn, publicationDate, description);
    }
}
