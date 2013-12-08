package com.acertainbookstore.client;

import java.util.HashSet;
import java.util.Set;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.utils.BookStoreException;

public class TestBookstoreClient implements Runnable, TestClient {
    private static BookStore client = ConcurrentCertainBookStore.getInstance();
    private Set<BookCopy> booksToBuy;

    private BookRating rating;

    public TestBookstoreClient(BookRating rating) {
    	this.rating = new BookRating(rating.getISBN(), rating.getRating());
    }
    public TestBookstoreClient(Set<BookCopy> booksToBuy) {
	this.booksToBuy = booksToBuy;
    }

    @Override
    public void run() {
	performFunctionality();
    }
    
    public void rateBooks()
    {
    	try{
    		Set<BookRating> ratings = new HashSet<BookRating>();
    		ratings.add(this.rating);
    		client.rateBooks(ratings);
    	}
    	catch(BookStoreException e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public void buyBooks()
    {
    	try{
    		client.buyBooks(booksToBuy);
    	}
    	catch(BookStoreException e)
    	{
    		e.printStackTrace();
    	}
    }
    @Override
    public void performFunctionality() {
		if(this.booksToBuy != null)
		{	
			buyBooks();
		}
		if(this.rating != null)
		{
			rateBooks();
		}

    }

}
