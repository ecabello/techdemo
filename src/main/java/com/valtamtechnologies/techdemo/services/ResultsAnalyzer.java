package com.valtamtechnologies.techdemo.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import com.valtamtechnologies.techdemo.models.SearchResult;

public class ResultsAnalyzer {
	private final int maxResults;
	private final Map<String, SearchResult> deduplicationMap;
	
	public ResultsAnalyzer(int maxResults) {
		this.maxResults = maxResults;
		deduplicationMap = new ConcurrentHashMap<>();
	}

	public Collection<SearchResult> analyze(Collection<SearchResult> results) {
		return analyze(results, maxResults, deduplicationMap);
	}
	
	public static Collection<SearchResult> analyze(Collection<SearchResult> results, int maxResults, Map<String, SearchResult> deduplicationMap) {
		
		Stream<SearchResult> stream = deduplicationMap instanceof ConcurrentMap ? results.parallelStream() :
																				  results.stream();
		stream.forEach(
			(r) -> deduplicationMap.putIfAbsent(r.getUrl(), r)
		);
		return new ArrayList<>(deduplicationMap.values()).subList(0, Math.min(maxResults, deduplicationMap.size()));
	}
}
