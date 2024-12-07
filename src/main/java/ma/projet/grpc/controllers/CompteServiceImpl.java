package ma.projet.grpc.controllers;

import io.grpc.stub.StreamObserver;
import ma.projet.grpc.entities.CompteEntity;
import ma.projet.grpc.repositories.CompteRepository;
import ma.projet.grpc.stubs.*;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    @Autowired
    private CompteRepository compteRepository;

    @Override
    public void allComptes(GetAllComptesRequest request, StreamObserver<GetAllComptesResponse> responseObserver) {
        List<CompteEntity> comptes = compteRepository.findAll();
        GetAllComptesResponse.Builder responseBuilder = GetAllComptesResponse.newBuilder();
        comptes.forEach(compte -> responseBuilder.addComptes(mapToGrpcCompte(compte)));
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void compteById(GetCompteByIdRequest request, StreamObserver<GetCompteByIdResponse> responseObserver) {
        CompteEntity compte = compteRepository.findById(request.getId()).orElse(null);
        if (compte != null) {
            responseObserver.onNext(GetCompteByIdResponse.newBuilder().setCompte(mapToGrpcCompte(compte)).build());
        } else {
            responseObserver.onError(new Throwable("Compte non trouvé"));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void totalSolde(GetTotalSoldeRequest request, StreamObserver<GetTotalSoldeResponse> responseObserver) {
        int count = (int) compteRepository.count();
        float sum = 0;
        for (CompteEntity compte : compteRepository.findAll()) {
            sum += compte.getSolde();
        }
        float average = count > 0 ? sum / count : 0;

        SoldeStats stats = SoldeStats.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAverage(average)
                .build();

        responseObserver.onNext(GetTotalSoldeResponse.newBuilder().setStats(stats).build());
        responseObserver.onCompleted();
    }

    @Override
    public void saveCompte(SaveCompteRequest request, StreamObserver<SaveCompteResponse> responseObserver) {
        CompteRequest compteReq = request.getCompte();
        String id = UUID.randomUUID().toString();

        CompteEntity compteEntity = new CompteEntity();
        compteEntity.setId(id);
        compteEntity.setSolde(compteReq.getSolde());
        compteEntity.setDateCreation(compteReq.getDateCreation());
        compteEntity.setType(compteReq.getType().name());
        
        CompteEntity savedCompte = compteRepository.save(compteEntity);
        
        responseObserver.onNext(SaveCompteResponse.newBuilder()
                .setCompte(mapToGrpcCompte(savedCompte))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void compteByType(GetCompteByTypeRequest request, StreamObserver<GetAllComptesResponse> responseObserver) {
        List<CompteEntity> comptesByType = compteRepository.findByType(request.getType().name());
        
        GetAllComptesResponse response = GetAllComptesResponse.newBuilder()
                .addAllComptes(comptesByType.stream()
                        .map(this::mapToGrpcCompte)
                        .collect(Collectors.toList()))
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteCompte(DeleteCompteRequest request, StreamObserver<DeleteCompteResponse> responseObserver) {
        CompteEntity removed = compteRepository.findById(request.getId()).orElse(null);
        DeleteCompteResponse response = DeleteCompteResponse.newBuilder()
                .setDeleted(removed != null)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Compte mapToGrpcCompte(CompteEntity entity) {
        return Compte.newBuilder()
                .setId(entity.getId())
                .setSolde((float) entity.getSolde())
                .setDateCreation(entity.getDateCreation())
                .setType(TypeCompte.valueOf(entity.getType()))
                .build();
    }

}
