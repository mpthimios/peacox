package com.peacox.recommender.repository;

import java.util.Date;
import java.util.List;



public interface MessagesShownService {
	
	public List<MessagesShown> findMessagesShownByUserId(Long id);
	public MessagesShown create(MessagesShown messagesShown);
	
}
