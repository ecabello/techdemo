package com.valtamtechnologies.techdemo.services;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.valtamtechnologies.techdemo.models.SearchResult;

public class FarooSearchService implements SearchService {
	private static final Logger log = LoggerFactory.getLogger(FarooSearchService.class);
	
	private static final String FAROO_API_URL = "http://www.faroo.com/api?q=";
	private static final String ENCODING = "UTF-8";

	@Override
	public List<SearchResult> search(List<String> terms, int maxResults) throws SearchException {
		String search = String.join("+", terms.parallelStream().map(
			(term) -> {
				try {
					return URLEncoder.encode(term, ENCODING);
				}
				catch (UnsupportedEncodingException e) {
					return term;
				}
			}
		).collect(Collectors.toList()));

		final StringBuilder searchUrl = new StringBuilder(FAROO_API_URL).append(search);
		searchUrl.append("&f=json");
		if (maxResults > 0)
			searchUrl.append("&length=").append(Math.min(10, maxResults));
		
			
		log.info("Search query: {}", searchUrl);
		try {
			RestTemplate restTemplate = new RestTemplate();
			return Arrays.asList(restTemplate.getForObject(searchUrl.toString(), FarooSearchResponse.class).getResults());
		}
		catch (Exception e) {
			log.error("Exception while searching: {}", e.getMessage(), e);
			throw new SearchException(e);
		}
		
	}
	
	public static class FarooSearchResponse {
		private SearchResult results[];
		private String query;
		private String suggestions[];
		private int count;
		private int start;
		private int length;
		private int time;
		
		public SearchResult[] getResults() {
			return results;
		}

		public void setResults(SearchResult[] results) {
			this.results = results;
		}

		public String getQuery() {
			return query;
		}

		public void setQuery(String query) {
			this.query = query;
		}

		public String[] getSuggestions() {
			return suggestions;
		}

		public void setSuggestions(String[] suggestions) {
			this.suggestions = suggestions;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public int getTime() {
			return time;
		}

		public void setTime(int time) {
			this.time = time;
		}
	}
}
