package org.example.repository;

import org.example.domain.Entity;
import org.example.paging.Page;
import org.example.paging.Pageable;

public interface PagingRepository<ID , E extends Entity<ID>> extends Repository<ID, E> {

    /**
     * Gaseste toate entitatile pe o anumita pagina.
     * @param pageable contine pageNumber si pageSize
     * @return un obiect Page care contine elementele paginii si numarul total de elemente.
     */
    Page<E> findAllOnPage(Pageable pageable);
}
