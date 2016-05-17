package com.valtamtechnologies.techdemo.services.akka.messages;

import java.util.Collections;
import java.util.List;

import com.valtamtechnologies.techdemo.models.SearchResult;

public class SearchResultMessage {
	private List<SearchResult> results;

	public SearchResultMessage(List<SearchResult> results) {
		this.results = Collections.unmodifiableList(results);
	}

	public List<SearchResult> getResults() {
		return results;
	}
}
