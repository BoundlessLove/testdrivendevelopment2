package com.thesarnas.cosmossql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.boot.autoconfigure.jdbc.*;
import org.springframework.stereotype.Component;

import java.util.Optional;

import javax.annotation.PostConstruct;

//@SpringBootApplication
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class CosmosSqlApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSqlApplication.class);

    @Autowired
    private UserRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(CosmosSqlApplication.class, args);
    }

    public void run(String... var1) {
       // this.repository.deleteAll().block();
       //LOGGER.info("Deleted all data in container.");

        final User testUser = new User("testId", "testFirstName", "testLastName", "test address line one");

        // Save the User class to Azure Cosmos DB database.
        final Mono<User> saveUserMono = repository.save(testUser);

        final Flux<User> firstNameUserFlux = repository.findByFirstName("testFirstName");

        //  Nothing happens until we subscribe to these Monos.
        //  findById won't return the user as user isn't present.
        final Mono<User> findByIdMono = repository.findById(testUser.getId());
        final User findByIdUser = findByIdMono.block();
        Assert.isNull(findByIdUser, "User must be null");

        final User savedUser = saveUserMono.block();
        Assert.state(savedUser != null, "Saved user must not be null");
        Assert.state(savedUser.getFirstName().equals(testUser.getFirstName()), "Saved user first name doesn't match");

        firstNameUserFlux.collectList().block();

        final Optional<User> optionalUserResult = repository.findById(testUser.getId()).blockOptional();
        Assert.isTrue(optionalUserResult.isPresent(), "Cannot find user.");

        final User result = optionalUserResult.get();
        Assert.state(result.getFirstName().equals(testUser.getFirstName()), "query result firstName doesn't match!");
        Assert.state(result.getLastName().equals(testUser.getLastName()), "query result lastName doesn't match!");

        LOGGER.info("findOne in User collection get result: {}", result.toString());
    }
}

@Slf4j
@Component
@AllArgsConstructor
class DataLoader {
	private final UserRepository repo;
	//private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);
	@PostConstruct
	void loadData() {
		//clean up to ensure no duplicate rows	
    	repo.deleteAll()
				.thenMany(Flux.just(new User("testId1","Alpha", "Bravo", "123 N 4th St."),
							new User("testid2","Charlie", "Delta", "321 S 99th Ave")))
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

	@PostMapping("/add")
	Mono<User> addUser(@RequestBody User user) {
		return repo.save(user);
	}

}
/* 
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
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;

import org.springframework.data.annotation.Id;
import reactor.core.publisher.Flux;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import reactor.core.publisher.Mono;
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
/*
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
/*


@RestController
@AllArgsConstructor
class CosmosSqlController{
	private final UserRepository repo;

	@GetMapping
	Flux<User> getAllUsers(){
		return repo.findAll();
	}

	@PostMapping("/add")
	Mono<User> addUser(@RequestParam User user) {
		return repo.save(user);
	}

}
*/
/*
//storing objects of type User with id of type string
//The reason we don't need to make a connection with the database is because
//in the pom.xml file, there is a dependancy called 'spring-cloud-azure-starter-data-cosmos'
//This dependancy in classpath tells springboot that class annotated with 'data' will speak to cosmos db.

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
*/

