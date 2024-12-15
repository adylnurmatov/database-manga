package org.adyl.service.impl;

import org.springframework.stereotype.Service;
import org.adyl.mapper.abstraction.AbstractMapper;
import org.adyl.model.Book;
import org.adyl.model.dto.BookDTO;
import org.adyl.repository.BookRepository;
import org.adyl.service.DefaultService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements DefaultService<BookDTO, Book, Integer> {
    private final BookRepository bookRepository;
    private final AbstractMapper mapper;

    public BookServiceImpl(BookRepository bookRepository, AbstractMapper mapper) {
        this.bookRepository = bookRepository;
        this.mapper = mapper;
    }

    @Override
    public List<BookDTO> findAll() {
        List<Book> books = bookRepository.findAll();
        return books.stream().map(book -> mapper.toDTO(book, BookDTO.class)).collect(Collectors.toList());
    }

    @Override
    public BookDTO findByKey(Integer key) {
        return mapper.toDTO(bookRepository.findById(key), BookDTO.class);
    }

    @Override
    public BookDTO save(BookDTO obj) {
        Book book = bookRepository.save(mapper.toEntity(obj, Book.class));
        return mapper.toDTO(book, BookDTO.class);
    }

    @Override
    public BookDTO update(Integer key, BookDTO obj) {
        Book book = mapper.toEntity(obj, Book.class);
        book.setId(key);
        bookRepository.save(book);
        return mapper.toDTO(book, BookDTO.class);
    }

    @Override
    public void delete(Integer key) {
        bookRepository.deleteById(key);
    }
}
