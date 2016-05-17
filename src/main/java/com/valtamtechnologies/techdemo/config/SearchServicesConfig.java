package com.valtamtechnologies.techdemo.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.valtamtechnologies.techdemo.services.FarooSearchService;
import com.valtamtechnologies.techdemo.services.GoogleSearchService;
import com.valtamtechnologies.techdemo.services.SearchService;
import com.valtamtechnologies.techdemo.services.akka.actors.SearchRunnerActor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@Configuration
public class SearchServicesConfig {
	//@Bean
	public SearchService googleSearchService() {
		return new GoogleSearchService();
	}
	
	//@Bean
	public SearchService farooSearchService() {
		return new FarooSearchService();
	}

	@Bean
	public Set<SearchService> searchServices() {
		return new HashSet<>(Arrays.asList(
			//farooSearchService(),
			googleSearchService())
		);
	}
	
	@Bean
	public ActorSystem actorSystem() {
		return ActorSystem.create("searchSystem");
	}
	
	@Bean
	public ActorRef searchRunnerActor() {
		return actorSystem().actorOf(SearchRunnerActor.props());
	}
}
