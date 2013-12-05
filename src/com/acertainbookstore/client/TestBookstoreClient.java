package com.acertainbookstore.client;


import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.utils.BookStoreException;

public class TestBookstoreClient implements Runnable {
	private static BookStore client = ConcurrentCertainBookStore.getInstance();;
	private Set<BookCopy> booksToBuy;
	public TestBookstoreClient(Set<BookCopy> booksToBuy) {
		this.booksToBuy = booksToBuy;
	}
	public void buyBooks() {
		try {
		    client.buyBooks(booksToBuy);;
		} catch (BookStoreException e) {
		    e.printStackTrace();
		}
	}
	@Override
	public void run() {
		buyBooks();
	}

}
