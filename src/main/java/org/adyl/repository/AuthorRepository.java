package org.adyl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.adyl.model.Author;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Integer> {
}
