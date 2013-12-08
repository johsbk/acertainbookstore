package com.acertainbookstore.client;

import java.util.List;
import java.util.Set;

import com.acertainbookstore.business.*;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.utils.BookStoreException;

public class TestBookstoreClientRetriever implements Runnable, TestClient {

    private static BookStore client = ConcurrentCertainBookStore.getInstance();
    private Set<Integer> ISBNlist;
    private List<Book> bookList;

    public TestBookstoreClientRetriever(Set<Integer> iSBNlist) {
	super();
	ISBNlist = iSBNlist;
    }

    @Override
    public void run() {
	performFunctionality();
    }
    
    @Override
    public void performFunctionality() {
	try {
	    setBookList(client.getBooks(ISBNlist));
	} catch (BookStoreException e) {
	    e.printStackTrace();
	}
    }

    public List<Book> getBookList() {
	return bookList;
    }

    public void setBookList(List<Book> bookList) {
	this.bookList = bookList;
    }

}
