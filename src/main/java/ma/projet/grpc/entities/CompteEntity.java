package ma.projet.grpc.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "comptes")
public class CompteEntity {
    @Id
    private String id;
    private double solde;
    private String dateCreation;
    private String type;
} 