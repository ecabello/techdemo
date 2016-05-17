package com.valtamtechnologies.techdemo.services.akka.actors;

import java.util.Arrays;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.valtamtechnologies.techdemo.services.SearchService;
import com.valtamtechnologies.techdemo.services.akka.messages.SearchMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class SearchRunnerActor extends UntypedActor {
	private static final Logger log = LoggerFactory.getLogger(SearchRunnerActor.class);
	
	private int succeses = 0;
	
	public static Props props() {
		return Props.create(SearchRunnerActor.class);
	}
	
	@Override
	public void postRestart(Throwable reason) throws Exception {
		log.info("Actor restarted because {}", reason.getMessage());
		super.postRestart(reason);
	}
	
	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof SearchMessage) {
			if (++succeses > 3)
				throw new NullPointerException();

			SearchMessage searchMsg = (SearchMessage)msg;
			for (SearchService service : searchMsg.getServices()) {
				ActorRef searcher = getContext().actorOf(SearchServiceActor.props());
				searcher.tell(
					new SearchMessage(
						searchMsg.getTerms(),
						searchMsg.getMaxResults(),
						new HashSet<>(Arrays.asList(service))
					), 
					getSender()
				);
			}
		}
		else
			unhandled(msg);
	}
}
