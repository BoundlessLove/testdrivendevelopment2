package com.thesarnas.cosmossql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import javax.annotation.*;
import javax.persistence.*;
import org.slf4j.*;
import com.azure.spring.data.cosmos.core.mapping.*;
import org.springframework.data.annotation.Id;
import reactor.core.publisher.Flux;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
//import reactor.core.publisher.Mono;
import javax.persistence.GeneratedValue;
import org.springframework.boot.autoconfigure.jdbc.*;

//@SpringBootApplication
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class CosmosSqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(CosmosSqlApplication.class, args);
	}

}
/*
Annonating a class as 'component', spring boot will create a bean of this type
and place it in our version controlled container. It will do what needs done to 
have that available for us.
User repository injected.When bean created, @PostConstruct method will run.
@Slf4j is lombok providing a logger
If after initial run, we don't want the data to be loaded, then comment out the component
annotation, the bean won't be created and all good from there.
 */
@Slf4j
@Component
@AllArgsConstructor
class DataLoader {
	private final UserRepository repo;

	@PostConstruct
	void loadData() {
		//clean up to ensure no duplicate rows	
		repo.deleteAll()
				.thenMany(Flux.just(new User("Alpha", "Bravo", "123 N 4th St."),
							new User("Charlie", "Delta", "321 S 99th Ave")))
				.flatMap(repo::save)
				.thenMany(repo.findAll())
				.subscribe(user -> log.info(user.toString()));
	}
}
/*
//Reactive streams can return a flux, which has values from 0..n.
If is not blocking. If small number of values, then no problem. With a reactive
case, it returns data in a drip. Flux allows us to stagger return values.
It is also do it as a mono (if specified) - just one.
*/


@RestController
@AllArgsConstructor
class CosmosSqlController{
	private final UserRepository repo;

	@GetMapping
	Flux<User> getAllUsers(){
		return repo.findAll();
	}
}

/*
//storing objects of type User with id of type string
//The reason we don't need to make a connection with the database is because
//in the pom.xml file, there is a dependancy called 'spring-cloud-azure-starter-data-cosmos'
//This dependancy in classpath tells springboot that class annotated with 'data' will speak to cosmos db.
*/
interface UserRepository extends ReactiveCosmosRepository<User, String> {}

@Container(containerName = "Items")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
class User {
	@Id
	@GeneratedValue
	private String id;
	@NonNull
	private String firstName;
	@NonNull
	@PartitionKey
	private String lastName;
	@NonNull
	private String address;
	
}


