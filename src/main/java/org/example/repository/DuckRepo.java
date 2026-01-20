package org.example.repository;

import org.example.domain.Duck;
import org.example.domain.DuckType;

public interface DuckRepo extends Repository<Long, Duck> {

    Iterable<Duck> findByType(DuckType type);
    org.example.paging.Page<Duck> findAllOnPage(org.example.paging.Pageable pageable, DuckType type);
}
