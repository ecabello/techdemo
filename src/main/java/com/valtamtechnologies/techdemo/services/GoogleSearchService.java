package com.valtamtechnologies.techdemo.services;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.valtamtechnologies.techdemo.models.SearchResult;

@Service
public class GoogleSearchService implements SearchService {
	private static final Logger log = LoggerFactory.getLogger(GoogleSearchService.class);
	
	private static final String GOOGLE_SEARCH_URL = "http://www.google.com/search?q=";
	private static final String ENCODING = "UTF-8";
	private static final String USER_AGENT = "Googlebot/2.1 (+http://www.google.com/bot.html)";
	private static final SearchResult empty = new SearchResult(); 
	
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

		final StringBuilder searchUrl = new StringBuilder(GOOGLE_SEARCH_URL).append(search);
		if (maxResults > 0)
			searchUrl.append("&num=").append(maxResults);
		
		log.info("Search query: {}", searchUrl);
		try {
			Elements links = Jsoup.connect(searchUrl.toString())
				.userAgent(USER_AGENT)
				.get().select(".g>.r>a");
	
			return links.parallelStream().map((link) -> {
			    String title = link.text();
			    String url = link.absUrl("href");
			    
			    try {
			    	url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");
			    	if (url.startsWith("http")) {
			    		log.info("Title: {} - URL: {}", title, url);
			    		return new SearchResult(title, url);
			    	}
			    }
			    catch (Exception e) {
			    	log.debug("Problem procesing result: {}", e.getMessage());
			    }
			    return empty; 
			}).filter((r) -> !r.equals(empty)).collect(Collectors.toList());
		}
		catch (Exception e) {
			log.error("Exception while searching: {}", e.getMessage(), e);
			throw new SearchException(e);
		}
	}
}
