package com.valtamtechnologies.techdemo.services.akka.actors;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.valtamtechnologies.techdemo.models.SearchResult;
import com.valtamtechnologies.techdemo.services.SearchService;
import com.valtamtechnologies.techdemo.services.akka.messages.RegisterSearcherMessage;
import com.valtamtechnologies.techdemo.services.akka.messages.SearchMessage;
import com.valtamtechnologies.techdemo.services.akka.messages.SearchResultMessage;

import akka.actor.Props;
import akka.actor.UntypedActor;

public class SearchServiceActor extends UntypedActor {
	private static final Logger log = LoggerFactory.getLogger(SearchServiceActor.class);

	public static Props props() {
		return Props.create(SearchServiceActor.class);
	}

	@Override
	public void postRestart(Throwable reason) throws Exception {
		log.info("Actor restarted because {}", reason.getMessage());
		super.postRestart(reason);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof SearchMessage) {
			try {
				// Tell the sender to watch us in case we crash
				getSender().tell(new RegisterSearcherMessage(), self());
				
				// Do the search
				SearchMessage searchMsg = (SearchMessage) msg;
				for (SearchService service : searchMsg.getServices()) {
					List<SearchResult> results = service.search(
						searchMsg.getTerms(), 
						searchMsg.getMaxResults()
					);
					//if (searchMsg.getTerms().contains("kill"))
					//	throw new NullPointerException();
					
					getSender().tell(new SearchResultMessage(results), self());
				}
			}
			finally {
				getContext().stop(getSelf());
			}
		} 
		else
			unhandled(msg);
	}
}
