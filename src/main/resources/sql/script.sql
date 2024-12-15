-- Таблица авторов
CREATE TABLE authors (
                         id SERIAL PRIMARY KEY,
                         firstname VARCHAR(20),
                         lastname VARCHAR(20),
                         initials CHAR(2),
                         birth_date DATE,
                         gender CHAR(1) DEFAULT 'U' CHECK (gender IN ('M', 'F', 'U')),
                         contact_details VARCHAR(100),
                         other_details VARCHAR(100),
                         image VARCHAR(100) DEFAULT 'Placeholder.png'
);

-- Таблица категорий книг
CREATE TABLE book_categories (
                                 code SERIAL PRIMARY KEY,
                                 category_description VARCHAR(20)
);

-- Таблица клиентов
CREATE TABLE customers (
                           id SERIAL PRIMARY KEY,
                           idnp CHAR(13),
                           name VARCHAR(20),
                           address VARCHAR(20),
                           phone CHAR(12) CHECK (phone ~ '^\+\d{11}$'),
    email VARCHAR(30) CHECK (email ~ '^[^@]+@[^@]+\.[^@]+$')
);

-- Таблица книг
CREATE TABLE books (
                       id SERIAL PRIMARY KEY,
                       author_id INT REFERENCES authors(id) ON DELETE CASCADE,
                       book_category_code INT REFERENCES book_categories(code) ON DELETE CASCADE,
                       isbn VARCHAR(17),
                       publication_date DATE,
                       date_aquired DATE,
                       title VARCHAR(50),
                       recommended_price DECIMAL(6, 2),
                       comments VARCHAR(100),
                       image VARCHAR(100) DEFAULT 'Placeholder.png'
);

-- Таблица заказов
CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,
                        customer_id INT REFERENCES customers(id) ON DELETE SET NULL,
                        order_date DATE,
                        order_value DECIMAL(7, 2)
);

-- Таблица элементов заказа
CREATE TABLE order_items (
                             id SERIAL PRIMARY KEY,
                             order_id INT REFERENCES orders(id) ON DELETE CASCADE,
                             book_id INT REFERENCES books(id) ON DELETE SET NULL,
                             item_agreed_price DECIMAL(6, 2),
                             item_comment VARCHAR(100)
);

-- Таблица типов контактов
CREATE TABLE ref_contact_types (
                                   code SERIAL PRIMARY KEY,
                                   contact_type_description VARCHAR(20)
);

-- Таблица контактов
CREATE TABLE contacts (
                          id SERIAL PRIMARY KEY,
                          contact_type_code INT REFERENCES ref_contact_types(code),
                          firstname VARCHAR(20),
                          lastname VARCHAR(20),
                          work_phone CHAR(12),
                          cell_phone CHAR(12),
                          other_details VARCHAR(100)
);

-- Таблица пользователей
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(20) NOT NULL UNIQUE,
                       password VARCHAR(100) NOT NULL,
                       roles VARCHAR(100) NOT NULL,
                       customer_id INT REFERENCES customers(id),
                       image VARCHAR(100) DEFAULT 'Placeholder.png'
);


-- Авторы
INSERT INTO authors (firstname, lastname, initials, birth_date, gender, contact_details, other_details)
VALUES
    ('John', 'Doe', 'JD', '1970-01-01', 'M', 'john.doe@gmail.com', 'Some details here'),
    ('Jane', 'Smith', 'JS', '1980-02-02', 'F', 'jane.smith@mail.com', 'Some details here');

-- Категории книг
INSERT INTO book_categories (category_description)
VALUES
    ('Fiction'),
    ('Non-Fiction');

-- Клиенты
INSERT INTO customers (idnp, name, address, phone, email)
VALUES
    ('1234567890123', 'Alice Brown', '123 Main St', '+37360000001', 'alice.brown@yahoo.com'),
    ('9876543210987', 'Bob Johnson', '456 Elm St', '+37360000002', 'bob.johnson@hotmail.com');

-- Книги
INSERT INTO books (author_id, book_category_code, isbn, publication_date, date_aquired, title, recommended_price, comments)
VALUES
    (1, 1, '978-3-16-148410-0', '2020-01-01', '2021-01-01', 'The Great Adventure', 19.99, 'Bestseller'),
    (2, 2, '978-1-23-456789-7', '2019-01-01', '2021-01-02', 'Science for Beginners', 29.99, 'Excellent for students'),
    (2, 1, '123-4-56-789098-7', '2019-09-10', '2019-09-10', 'Java for Beginners', 999.99, 'Excellent for true javers');

-- Заказы
INSERT INTO orders (customer_id, order_date, order_value)
VALUES
    (1, '2021-01-03', 49.98),
    (2, '2021-01-04', 29.99);

-- Элементы заказа
INSERT INTO order_items (order_id, book_id, item_agreed_price, item_comment)
VALUES
    (1, 4, 19.99, 'Gift wrapped'),
    (1, 5, 29.99, ''),
    (2, 5, 29.99, 'Include a bookmark');

-- Типы контактов
INSERT INTO ref_contact_types (contact_type_description)
VALUES
    ('Author'),
    ('Supplier');

-- Контакты
INSERT INTO contacts (contact_type_code, firstname, lastname, work_phone, cell_phone, other_details)
VALUES
    (1, 'John', 'Doe', '+37360000003', '+37360000004', 'Contact for book signings'),
    (2, 'Supply', 'Company', '+37360000005', '+37360000006', 'Bulk order contact');
