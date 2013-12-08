package com.acertainbookstore.client;

import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

public class TestStockManager implements Runnable, TestClient {
    private static StockManager storeManager = ConcurrentCertainBookStore
	    .getInstance();;
    private Set<BookCopy> copiesToAdd;

    public TestStockManager(Set<BookCopy> copiesToAdd) {
	this.copiesToAdd = copiesToAdd;
    }

    @Override
    public void run() {
	performFunctionality();
    }

    @Override
    public void performFunctionality() {
	try {
	    storeManager.addCopies(copiesToAdd);
	} catch (BookStoreException e) {
	    e.printStackTrace();
	}
    }

}
