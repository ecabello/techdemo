package com.valtamtechnologies.techdemo.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResult {
	private String title;
	private String url;
	
	public SearchResult() {
	}

	public SearchResult(String title, String url) {
		super();
		this.title = title;
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
