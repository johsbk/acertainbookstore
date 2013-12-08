package com.acertainbookstore.client;

import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.utils.BookStoreException;

public class TestBookstoreClient implements Runnable, TestClient {
    private static BookStore client = ConcurrentCertainBookStore.getInstance();
    private Set<BookCopy> booksToBuy;

    public TestBookstoreClient(Set<BookCopy> booksToBuy) {
	this.booksToBuy = booksToBuy;
    }

    @Override
    public void run() {
	performFunctionality();
    }

    @Override
    public void performFunctionality() {
	try {
	    client.buyBooks(booksToBuy);
	} catch (BookStoreException e) {
	    e.printStackTrace();
	}

    }

}
