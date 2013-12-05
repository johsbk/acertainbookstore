package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookRating;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.client.TestBookstoreClient;
import com.acertainbookstore.client.TestStockManager;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

/**
 * Test class to test the BookStore interface
 * 
 */
public class ConcurrentBookStoreTest {

	private static boolean localTest = true;
	private static StockManager storeManager;
	private static BookStore client;

	@BeforeClass
	public static void setUpBeforeClass() {
		try {
			if (localTest) {
				storeManager = ConcurrentCertainBookStore.getInstance();
				client = ConcurrentCertainBookStore.getInstance();
			} else {
				storeManager = new StockManagerHTTPProxy(
						"http://localhost:8081/stock");
				client = new BookStoreHTTPProxy("http://localhost:8081");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Before
	public void setUp() throws Exception {
		ConcurrentCertainBookStore.reset();
	}


	/**
	 * 
	 * Here we want to test rateBook functionality
	 * 
	 * 1. We add a book.
	 * 
	 * 2. We rate it using rateBook to certain rating.
	 * 
	 * 3. We check if the rating is updated by executing getBooks.
	 * 
	 * 4. We also check that the appropriate exception is thrown when rateBook
	 * is executed with wrong arguments
	 */
	@Test
	public void testBuyBooksAndAddCopies() {

		Integer testISBN = 300;
		Integer numCopies = 5;
		Set<StockBook> booksToAdd = new HashSet<StockBook>();
		booksToAdd.add(new ImmutableStockBook(testISBN, "Book Name",
				"Book Author", (float) 700, numCopies, 0, 0, 0, false));

		List<StockBook> listBooks = null;
		try {
			storeManager.addBooks(booksToAdd);
			listBooks = storeManager.getBooks();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		for (StockBook b : listBooks) {
			if (b.getISBN() == testISBN) {
				assertTrue("Before test: Num copies after adding x copies",
						b.getNumCopies() == numCopies);
				break;
			}
		}

		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		booksToBuy.add(new BookCopy(testISBN, numCopies));

		Set<BookCopy> copiesToAdd = new HashSet<BookCopy>();
		copiesToAdd.add(new BookCopy(testISBN, numCopies));

		TestBookstoreClient c1 = new TestBookstoreClient(booksToBuy);
		TestStockManager s = new TestStockManager(copiesToAdd);
		Thread t1 = new Thread(c1);
		Thread t2 = new Thread(s);
		t1.start();
		t2.start();
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail();
		}
		try {
			listBooks = storeManager.getBooks();
		} catch (BookStoreException e) {
			e.printStackTrace();
			fail();
		}

		for (StockBook b : listBooks) {
			if (b.getISBN() == testISBN) {
				assertTrue("After test: Num copies after adding x copies",
						b.getNumCopies() == numCopies);
				break;
			}
		}

	}

	/**
	 * 
	 * Here we want to test rateBook functionality
	 * 
	 * 1. We add a book.
	 * 
	 * 2. We rate it using rateBook to certain rating.
	 * 
	 * 3. We check if the rating is updated by executing getBooks.
	 * 
	 * 4. We also check that the appropriate exception is thrown when rateBook
	 * is executed with wrong arguments
	 */
	@Test
		public void testBuyBooksAndAddCopiesContinious() {

			Integer testISBN1 = 350;
			Integer testISBN2 = 400;
			Integer testISBN3 = 500;
			Integer numCopies = 5;
			Set<StockBook> booksToAdd = new HashSet<StockBook>();
			booksToAdd.add(new ImmutableStockBook(testISBN1, "Star wars 1",
					"Book Author", (float) 700, numCopies, 0, 0, 0, false));
			booksToAdd.add(new ImmutableStockBook(testISBN2, "Star wars 2",
					"Book Author", (float) 1111700, numCopies, 0, 0, 0, false));
			booksToAdd.add(new ImmutableStockBook(testISBN3, "Star wars 3",
					"Book Author", (float) 700, numCopies, 0, 0, 0, false));
			List<StockBook> listBooks = null;
			try {
				storeManager.addBooks(booksToAdd);
				 listBooks = storeManager.getBooks();
			} catch (BookStoreException e) {
				e.printStackTrace();
				fail();
			}
		   
			for (StockBook b : listBooks) {
			    if (b.getISBN() == testISBN1 || b.getISBN() == testISBN2 || b.getISBN() == testISBN3) {
				assertTrue("Before test: Num copies after adding x copies",
					b.getNumCopies() == numCopies);
				break;
			    }
			}

			Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
			booksToBuy.add(new BookCopy(testISBN1, numCopies));
			booksToBuy.add(new BookCopy(testISBN2, numCopies));
			booksToBuy.add(new BookCopy(testISBN3, numCopies));

			Set<BookCopy> copiesToAdd = new HashSet<BookCopy>();
			copiesToAdd.add(new BookCopy(testISBN1,numCopies));
			copiesToAdd.add(new BookCopy(testISBN2,numCopies));
			copiesToAdd.add(new BookCopy(testISBN3,numCopies));
			
			TestBookstoreClient c1 = new TestBookstoreClient(booksToBuy);
			TestStockManager s = new TestStockManager(copiesToAdd);
			Thread t1 = new Thread(c1);
			Thread t2 = new Thread(s);
			t1.start();
			t2.start();
			while(t1.isAlive() && t2.isAlive()) {
				try {
					 listBooks = storeManager.getBooks();
					 for (StockBook b : listBooks) {
					    if (b.getISBN() == testISBN1 || b.getISBN() == testISBN2 || b.getISBN() == testISBN3) {
						assertTrue("Num copies in store are correct",
							b.getNumCopies() == numCopies || b.getNumCopies() == 0 || b.getNumCopies() == numCopies*2);
					    }
					}
				} catch(BookStoreException e) {
					e.printStackTrace();
					fail();
				}
			}
			
			try {
				t1.join();
				t2.join();
			} catch(InterruptedException e) {
				e.printStackTrace();
				fail();
			}
		   
		}

	@AfterClass
	public static void tearDownAfterClass() {
		if (!localTest) {
			((BookStoreHTTPProxy) client).stop();
			((StockManagerHTTPProxy) storeManager).stop();
		}
	}

}
