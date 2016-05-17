package com.valtamtechnologies.techdemo.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.valtamtechnologies.techdemo.models.SearchResult;
import com.valtamtechnologies.techdemo.services.ResultsAnalyzer;
import com.valtamtechnologies.techdemo.services.SearchException;
import com.valtamtechnologies.techdemo.services.SearchService;
import com.valtamtechnologies.techdemo.services.akka.actors.SearchAggregatorActor;
import com.valtamtechnologies.techdemo.services.akka.messages.SearchMessage;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

@RestController
@RequestMapping("search")
public class SearchController {
	private static final Logger log = LoggerFactory.getLogger(SearchController.class);
	
	@Autowired
	private ActorSystem searchSystem;
	
	@Autowired
	private ActorRef searchRunner;
	
	@Autowired
	private Set<SearchService> services;
	
	private static final Executor threadPool = Executors.newCachedThreadPool();
	
	@RequestMapping(path="", produces="application/json")
	public Collection<SearchResult> search(@RequestParam("terms") final List<String> terms, 
									       @RequestParam(name="num", required=false) final Integer num) {
		return runAllSearchEngines(terms, num == null ? 10 : num);
	}
	
	@RequestMapping(path="async", produces="application/json")
	public Callable<Collection<SearchResult>> searchAsync(@RequestParam("terms") final List<String> terms, 
											   		      @RequestParam(name="num", required=false) final Integer num) {
		return () -> runAllSearchEngines(terms, num == null ? 10 : num);
	}
	
	@RequestMapping(path="deferred", produces="application/json")
	public DeferredResult<Collection<SearchResult>> searchDeferred(@RequestParam("terms") final List<String> terms, 
															       @RequestParam(name="num", required=false) final Integer num) {
		DeferredResult<Collection<SearchResult>> deferred = new DeferredResult<>();
		threadPool.execute(() -> {
			try {
				deferred.setResult(runAllSearchEngines(terms, num == null ? 10 : num));
			}
			catch (Exception e) {
				deferred.setErrorResult(e);
			}
		});
		return deferred;
	}
	
	@RequestMapping(path="akka", produces="application/json")
	public DeferredResult<Collection<SearchResult>> searchAkka(@RequestParam("terms") final List<String> terms, 
															   @RequestParam(name="num", required=false) final Integer num) {
		DeferredResult<Collection<SearchResult>> deferred = new DeferredResult<>();
		
		int maxResults = num == null ? 10 : num;
		
		// Create an aggregator
		ActorRef aggregator = searchSystem.actorOf(SearchAggregatorActor.props(maxResults, deferred));
		
		// Run the search
		searchRunner.tell(
			new SearchMessage(
				terms,
				maxResults,
				services
			), 
			aggregator
		);
		return deferred;
	}
	
	@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="There was a problem with your search")  // 404
	@ExceptionHandler(SearchException.class)
	public void badSearch(SearchException e) {
	    // Nothing to do
		log.error("Search Exception: {}", e.getMessage(), e);
	}
	
	private Collection<SearchResult> runAllSearchEngines(final List<String> terms, final int maxResults) {
		List<SearchResult> allResults = new ArrayList<>();
		
		List<SearchResult> syncAllResults = Collections.synchronizedList(allResults);
		services.parallelStream().forEach(
			(s) -> s.search(terms, maxResults).stream().forEach(syncAllResults::add)
		);
		return new ResultsAnalyzer(maxResults).analyze(allResults);
	}
}
