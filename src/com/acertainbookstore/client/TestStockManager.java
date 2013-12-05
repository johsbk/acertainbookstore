package com.acertainbookstore.client;

import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

public class TestStockManager implements Runnable {
    private static StockManager storeManager =ConcurrentCertainBookStore.getInstance();;
    private Set<BookCopy> copiesToAdd;
    public TestStockManager(Set<BookCopy> copiesToAdd) {
    	this.copiesToAdd = copiesToAdd;
    }
	public void addCopies() {
		try {

			storeManager.addCopies(copiesToAdd);
		} catch (BookStoreException e) {
		    e.printStackTrace();
		}
	}
	@Override
	public void run() {
		addCopies();
	}

}
