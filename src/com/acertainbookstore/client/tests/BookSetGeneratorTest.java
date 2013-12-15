package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.Comparator;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.junit.Test;

import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.workloads.BookSetGenerator;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

/**
 * Test class to test the BookStore interface
 * 
 */
public class BookSetGeneratorTest {

	@Test
    public void testGenerateBooks() {
    	BookSetGenerator bsg = new BookSetGenerator();
    	try {
    		int numBooks = 5;
			Set<StockBook> books = bsg.nextSetOfStockBooks(numBooks);
			assertEquals(numBooks, books.size());
			//System.out.println(books);
		} catch (BookStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	@Test
	public void testBLah() {
		StockManager stockManager = CertainBookStore.getInstance();
		BookSetGenerator bsg = new BookSetGenerator();
		
		List<StockBook> allBooks;
		try {
			stockManager.addBooks(bsg.nextSetOfStockBooks(100));
			allBooks = stockManager.getBooks();
		
			int k = 5;
			
			if (k>allBooks.size()) {
				throw new BookStoreException("Num lack stock is higher than the number for books in the store");
			}
			
			PriorityQueue<StockBook> pq = new PriorityQueue<StockBook>(4, new Comparator<StockBook>() {
	
				@Override
				public int compare(StockBook o1, StockBook o2) {
					
					return new Integer(o1.getNumCopies()).compareTo(new Integer(o2.getNumCopies()));
				}
				
			});
			pq.addAll(allBooks);
			System.out.println(pq);
		} catch (BookStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
