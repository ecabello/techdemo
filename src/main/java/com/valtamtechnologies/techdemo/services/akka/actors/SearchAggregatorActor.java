package com.valtamtechnologies.techdemo.services.akka.actors;

import java.util.Collection;
import java.util.Collections;

import org.springframework.web.context.request.async.DeferredResult;

import com.valtamtechnologies.techdemo.models.SearchResult;
import com.valtamtechnologies.techdemo.services.ResultsAnalyzer;
import com.valtamtechnologies.techdemo.services.akka.messages.SearchResultMessage;
import com.valtamtechnologies.techdemo.services.akka.messages.RegisterSearcherMessage;

import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;

public class SearchAggregatorActor extends UntypedActor {
	private final int maxResults;
	private final DeferredResult<Collection<SearchResult>> results;
	private final ResultsAnalyzer resultsAnalyzer;
	private int registeredSearchers;
	
	
	public static Props props(int maxResults, DeferredResult<Collection<SearchResult>> results) {
		return Props.create(SearchAggregatorActor.class, maxResults, results);
	}

	public SearchAggregatorActor(int maxResults, DeferredResult<Collection<SearchResult>> results) {
		this.maxResults = maxResults;
		this.results = results;
		resultsAnalyzer = new ResultsAnalyzer(maxResults);
		registeredSearchers = 0;
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof SearchResultMessage) {
			SearchResultMessage resultMsg = (SearchResultMessage)msg;
			
			Collection<SearchResult> resultsSoFar = resultsAnalyzer.analyze(resultMsg.getResults());
			if (resultsSoFar.size() >= maxResults) {
				results.setResult(resultsSoFar);
				getContext().stop(getSelf());
			}
		}
		else if (msg instanceof RegisterSearcherMessage) {
			registeredSearchers++;
			getContext().watch(getSender());
		}
		else if (msg instanceof Terminated) {
			if (--registeredSearchers == 0){
				results.setResult(resultsAnalyzer.analyze(Collections.emptyList()));
				getContext().stop(getSelf());
			}
		}
		else
			unhandled(msg);
	}
}
