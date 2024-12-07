package ma.projet.grpc.repositories;

import ma.projet.grpc.entities.CompteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CompteRepository extends JpaRepository<CompteEntity, String> {
    List<CompteEntity> findByType(String type);
} 