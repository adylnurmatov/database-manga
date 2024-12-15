package org.adyl.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.adyl.model.BookCategory;
import org.adyl.repository.BookCategoryRepository;
import org.adyl.security.details.StoreUserDetails;
import org.adyl.service.AuthenticationService;
import org.springframework.util.StringUtils;

import java.util.List;

@Route(value = "categories", layout = MainView.class)
@PermitAll
@CssImport("my-styles/grid-style.css")
@PageTitle("Сategorie")
public class CategoriesView extends FlexLayout {
    private BookCategoryRepository repository;
    private List<BookCategory> categories;

    private StoreUserDetails principal;
    private AuthenticationService authenticationService;
    private List<String> roles;

    public CategoriesView(BookCategoryRepository repository, AuthenticationService authenticationService) {
        this.repository = repository;
        this.authenticationService = authenticationService;

        principal = (StoreUserDetails) authenticationService.getCurrentPrincipal();
        roles = principal.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).toList();

        this.categories = repository.findAll();

        setFlexDirection(FlexDirection.COLUMN);
        setAlignItems(Alignment.CENTER);
        setSizeFull();
//        setJustifyContentMode(JustifyContentMode.CENTER);
        add(new H1("Available Categories:"));

//        Div availableCategories = new Div();
//        availableCategories.setWidth("100%");
//        availableCategories.setHeight("min-content");

        Grid<BookCategory> categoryGrid = new Grid<>(BookCategory.class, false);
//        categoryGrid.addColumn(BookCategory::getCategoryDescription).setHeader("Category");
//        categoryGrid.addColumn(LitRenderer.<BookCategory> of("<span>${book.name}</span>").withProperty("book.category", BookCategory::getCategoryDescription)).setHeader("Category");
        categoryGrid.addColumn(LitRenderer.<BookCategory> of("<a href='books?filter=category-${item.code}'> ${item.categoryDescription} </a>") //item - current object, categoryDescription - property getter that will be called for this object
                .withProperty("categoryDescription", BookCategory::getCategoryDescription).withProperty("code", BookCategory::getCode)).setHeader("Category");

        categoryGrid.setItems(categories);

//        availableCategories.add(categoryGrid);
//        availableCategories.setWidth("35%");
//        availableCategories.getStyle().setMarginTop("20px");
        categoryGrid.getStyle().setTextAlign(Style.TextAlign.CENTER);
        categoryGrid.setAllRowsVisible(true);
        categoryGrid.setWidth("35%");
        categoryGrid.setHeight("min-content");
        categoryGrid.setMaxHeight("65%");
        categoryGrid.getStyle().setMarginLeft("auto");
        categoryGrid.getStyle().setMarginRight("auto");
        categoryGrid.getStyle().setMarginTop("20px");
//
//        add(availableCategories);
        add(categoryGrid);

        if (!roles.isEmpty() && roles.contains("ROLE_ADMIN")) {
            categoryGrid.addComponentColumn(category -> {
                Button edit = new Button("Edit");
                edit.addClickListener(event -> {
                    Dialog editDialogue = new Dialog("Edit Category info");

                    addAddOrEditDialogueLayout(editDialogue, category);

                    editDialogue.open();
                });
                return edit;
            }).setHeader("Edit");

            categoryGrid.addComponentColumn(category -> {
                Button delete = new Button("Delete");
                delete.addClickListener(event -> {
                    Dialog deleteDialogue = new Dialog("Delete Category Confirmation");

                    addDeleteDialogueLayout(deleteDialogue, category);

                    deleteDialogue.open();
                });

                return delete;
            }).setHeader("Delete");



            Button add = new Button("Add new");

            add(add);
            add.getStyle().setMarginTop("10px");
            setAlignSelf(Alignment.END, add);

            add.setWidth("max-content");
            add.getStyle().setMarginRight("32.5%");
            add.getStyle().setPadding("5px 10px");

            add.addClickListener(event -> {
                Dialog addDialog = new Dialog("Add new Category");

                addAddOrEditDialogueLayout(addDialog, null);

                addDialog.open();
            });
        }
    }

    private void addAddOrEditDialogueLayout(Dialog editDialogue, BookCategory category){
        Binder<BookCategory> binder = new Binder<>();

        TextField categoryName = new TextField("Category name: ");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setJustifyContentMode(JustifyContentMode.CENTER);

        Button save = new Button("Save");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel");

        buttons.add(save, cancel);

        if (category != null){
            categoryName.setValue(category.getCategoryDescription());
            save.setText("Edit!");
        }



        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexDirection.COLUMN);
        layout.add(categoryName, buttons);

        editDialogue.add(layout);

        save.addClickListener(event -> {
            binder.forField(categoryName).withValidator(s -> StringUtils.hasText(s), "Specify category name!").bind(BookCategory::getCategoryDescription, BookCategory::setCategoryDescription);

            BinderValidationStatus validation = binder.validate();


            if (!validation.hasErrors()) {
                BookCategory newCategory = new BookCategory(categoryName.getValue());

                if (category!=null)
                    newCategory.setCode(category.getCode());

                repository.save(newCategory);
                editDialogue.close();
                getUI().ifPresent(ui -> ui.getPage().reload());
            }
        });

        cancel.addClickListener(event -> editDialogue.close());
    }

    private void addDeleteDialogueLayout(Dialog dialog, BookCategory category) {
        H3 question = new H3("Are you sure you want to delete the category \"" + category.getCategoryDescription() + "\"?");
        question.getStyle().setMarginBottom("20px");

        Button delete = new Button("Delete");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button("Cancel", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.add(delete, cancel);
        buttons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        dialog.add(question, buttons);

        delete.addClickListener(event -> {
            repository.delete(category);
            dialog.close();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });
    }
}

//https://vaadin.com/docs/latest/components/grid/styling