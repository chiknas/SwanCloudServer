package com.chiknas.swancloudserver.repositories;

import com.chiknas.swancloudserver.entities.ThumbnailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author nkukn
 * @since 3/28/2021
 */
@Repository
public interface ThumbnailRepository extends JpaRepository<ThumbnailEntity, Integer> {
    
    boolean existsByFileName(String fileName);

    Optional<ThumbnailEntity> findByFileName(String fileName);
}
