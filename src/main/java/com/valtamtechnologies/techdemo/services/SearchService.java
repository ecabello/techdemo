package com.valtamtechnologies.techdemo.services;

import java.util.List;

import com.valtamtechnologies.techdemo.models.SearchResult;

public interface SearchService {
	List<SearchResult> search(List<String> terms, int maxResults) throws SearchException;
}
