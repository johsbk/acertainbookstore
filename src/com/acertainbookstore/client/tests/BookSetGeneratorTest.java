package com.acertainbookstore.client.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.workloads.BookSetGenerator;
import com.acertainbookstore.utils.BookStoreException;

public class BookSetGeneratorTest {

    private static BookSetGenerator bsg;
    private static Random rand;

    @BeforeClass
    public static void setUpBeforeClass() {
	bsg = new BookSetGenerator();
	rand = new Random();
    }

    @Test
    public void testGenerateRandomNumBooks() {
	int bookSize = 7;
	try {
	    Set<StockBook> books = bsg.nextSetOfStockBooks(bookSize);
	    assertEquals("Books are generated", bookSize, books.size());
	    for (StockBook b : books)
		System.out.println(b);
	} catch (BookStoreException e) {
	    System.out.println(e.getMessage());
	}
    }

    @Test
    public void testSampleFromSetOfISBNS() {
	int numBooks = 4;
	Set<Integer> isbns = new HashSet<Integer>();
	for (int i = 0; i < 10; i++) {
	    isbns.add(rand.nextInt(98765) + 123);
	}

	Set<Integer> result = null;
	try {
	    result = bsg.sampleFromSetOfISBNs(isbns, numBooks);
	    assertEquals("Set of isbns created Successfully", numBooks,
		    result.size());
	} catch (BookStoreException e) {
	    System.out.println(e.getMessage());
	}
	boolean exceptionThrown = false;
	try {
	    result = bsg.sampleFromSetOfISBNs(isbns, 0);
	} catch (BookStoreException e) {
	    System.out.println(e.getMessage());
	    exceptionThrown = true;
	}
	assertTrue("0 must throw exception", exceptionThrown);

	exceptionThrown = false;
	try {
	    result = bsg.sampleFromSetOfISBNs(isbns, 16);
	} catch (BookStoreException e) {
	    System.out.println(e.getMessage());
	    exceptionThrown = true;
	}
	assertTrue(" Bigger than isblist size must throw exception",
		exceptionThrown);
	System.out.println("ISBNList: " + result);
    }

}
