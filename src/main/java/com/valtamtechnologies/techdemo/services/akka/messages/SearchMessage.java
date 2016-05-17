package com.valtamtechnologies.techdemo.services.akka.messages;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.valtamtechnologies.techdemo.services.SearchService;

public class SearchMessage {
	private List<String> terms;
	private int maxResults;
	private Set<SearchService> services;
	
	public SearchMessage(List<String> terms, int maxResults, Set<SearchService> services) {
		this.terms = Collections.unmodifiableList(terms);
		this.maxResults = maxResults;
		this.services = services;
	}

	public List<String> getTerms() {
		return terms;
	}
	
	public int getMaxResults() {
		return maxResults;
	}

	public Set<SearchService> getServices() {
		return services;
	}
}
