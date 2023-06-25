package com.thesarnas.cosmossql;


import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CredentialsRepository extends ReactiveCosmosRepository<Credentials, String> {
    Flux<Credentials> findByUsername(String username);
}