package com.acertainbookstore.client;

import java.util.Set;

import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

public class TestStockManagerAdder implements Runnable, TestClient {

    private static StockManager storeManager = ConcurrentCertainBookStore
	    .getInstance();;

    private Set<StockBook> booksToAdd;

    public TestStockManagerAdder(Set<StockBook> booksToAdd) {
	super();
	this.booksToAdd = booksToAdd;

    }

    @Override
    public void run() {
	performFunctionality();
    }

    @Override
    public void performFunctionality() {
	try {
	    storeManager.addBooks(booksToAdd);
	} catch (BookStoreException e) {
	    e.printStackTrace();
	}
    }
}
