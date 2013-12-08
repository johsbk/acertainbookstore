package com.acertainbookstore.client.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ConcurrentCertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreHTTPProxy;
import com.acertainbookstore.client.StockManagerHTTPProxy;
import com.acertainbookstore.client.TestBookstoreClient;
import com.acertainbookstore.client.TestBookstoreClientRetriever;
import com.acertainbookstore.client.TestStockManager;
import com.acertainbookstore.client.TestStockManagerAdder;
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
     * Here we want to test buy buuks and add copies functionality with multiple
     * clients
     * 
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

	TestBookstoreClient bsClient = new TestBookstoreClient(booksToBuy);
	TestStockManager stockMgr = new TestStockManager(copiesToAdd);
	/*
	 * executor.execute(stockMgr); executor.execute(bsClient);
	 * executor.shutdown();
	 */

	Thread t1 = new Thread(bsClient);
	Thread t2 = new Thread(stockMgr);
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
     * Here we want to test buy books and add copies functionality Two clients
     * C1 and C2 , running in dierent threads, continuously invoke operations
     * against the BookStore and StockManager interfaces. C1 invokes buyBooks to
     * buy a given and xed collection of books (e.g., the Star Wars trilogy). C1
     * then invokes addCopies to replenish the stock of exactly the same books
     * bought. C2 continuously calls getBooks and ensures that the snapshot
     * returned either has the quantities for all of these books as if they had
     * been just bought or as if they had been just replenished.
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
	    if (b.getISBN() == testISBN1 || b.getISBN() == testISBN2
		    || b.getISBN() == testISBN3) {
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
	copiesToAdd.add(new BookCopy(testISBN1, numCopies));
	copiesToAdd.add(new BookCopy(testISBN2, numCopies));
	copiesToAdd.add(new BookCopy(testISBN3, numCopies));

	TestBookstoreClient c1 = new TestBookstoreClient(booksToBuy);
	TestStockManager s = new TestStockManager(copiesToAdd);
	Thread t1 = new Thread(c1);
	Thread t2 = new Thread(s);
	t1.start();
	t2.start();
	while (t1.isAlive() && t2.isAlive()) {
	    try {
		listBooks = storeManager.getBooks();
		for (StockBook b : listBooks) {
		    if (b.getISBN() == testISBN1 || b.getISBN() == testISBN2
			    || b.getISBN() == testISBN3) {
			assertTrue(
				"Num copies in store are correct",
				b.getNumCopies() == numCopies
					|| b.getNumCopies() == 0
					|| b.getNumCopies() == numCopies * 2);
		    }
		}
	    } catch (BookStoreException e) {
		e.printStackTrace();
		fail();
	    }
	}

	try {
	    t1.join();
	    t2.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    fail();
	}
    }

    /*
     * Here we test retrieving books with a bookstore client while trying to add
     * a book that the client want to retrieve. Bookstore client want to get the
     * booklist of testISBN1 while Stock manager try to add that book with a testISBN1.
     * 
     */
    @Test
    public void addBooksAndGetBooksTest() {
	Integer testISBN = 100;
	Integer testISBN2 = 200;
	Set<StockBook> booksToAdd = new HashSet<StockBook>();
	Set<StockBook> booksForAdding = new HashSet<StockBook>();
	booksToAdd.add(new ImmutableStockBook(testISBN, "Book Name",
		"Book Author", (float) 100, 5, 0, 0, 0, false));
	ImmutableStockBook book = new ImmutableStockBook(testISBN2,
		"Harry Potter", "JK Rawling", 300, 5, 0, 0, 0, false);
	booksForAdding.add(book);
	try {
	    storeManager.addBooks(booksToAdd);
	} catch (BookStoreException e) {
	    e.printStackTrace();
	}

	Set<Integer> ISBNList = new HashSet<Integer>();
	ISBNList.add(testISBN2);
	List<StockBook> books = null;
	try {
	    books = storeManager.getBooks();
	} catch (BookStoreException e) {
	    e.printStackTrace();
	    fail();
	}
	boolean containsTestISBN = false;
	for (StockBook b : books) {
	    if (b.getISBN() == testISBN) {
		containsTestISBN = true;
	    }
	}
	assertTrue("Bookstore contains testISBN1", containsTestISBN);

	TestStockManagerAdder sma = new TestStockManagerAdder(booksForAdding);
	TestBookstoreClientRetriever bscw = new TestBookstoreClientRetriever(
		ISBNList);
	List<Book> bookList;
	Thread c1 = new Thread(sma);
	Thread c2 = new Thread(bscw);
	c1.start();
	c2.start();
	boolean containsTestISBN1 = false;
	while (c1.isAlive() && c2.isAlive()) {
	    bookList = bscw.getBookList();
	    if (bookList != null) {
		for (Book b : bookList) {
		    if (b.getISBN() == testISBN)
			containsTestISBN1 = true;
		}
	    }
	}
	assertFalse("Should not contain book", containsTestISBN1);
	try {
	    c1.join();
	    c2.join();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	bookList = bscw.getBookList();
	containsTestISBN1 = false;
	for (Book b : bookList) {
	    if (b.getISBN() == testISBN2) {
		containsTestISBN1 = true;
	    }
	}
	assertTrue("Threads finished should contain the testISBN1",
		containsTestISBN1);
	try {
	    books = storeManager.getBooks();
	} catch (BookStoreException e) {
	    e.printStackTrace();
	}
	assertTrue("Current Books should contain the added book",
		books.contains(book));

    }

    @AfterClass
    public static void tearDownAfterClass() {
	if (!localTest) {
	    ((BookStoreHTTPProxy) client).stop();
	    ((StockManagerHTTPProxy) storeManager).stop();
	}
    }

}
