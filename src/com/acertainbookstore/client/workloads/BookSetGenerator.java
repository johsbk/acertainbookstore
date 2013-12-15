package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator {

    private final Random rand;

    public BookSetGenerator() {
	this.rand = new Random();
    }

    /*
     * Returns num randomly selected isbns from the input set
     */
    
    /**
     * @param isbns
     * @param num
     * @return
     * @throws BookStoreException
     */
    public Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num)
	    throws BookStoreException {
	if (num <= 0)
	    throw new BookStoreException("Num is below 0");
	if (num > isbns.size())
	    throw new BookStoreException(
		    "List of isbn size is smaller than num");
	Set<Integer> result = new HashSet<Integer>();
	int range = isbns.size();
	Integer[] isbnsArray = isbns.toArray(new Integer[isbns.size()]);
	for (; result.size() < num;) {
	    Integer isbn = isbnsArray[rand.nextInt(range)];
	    if (!result.contains(isbn))
		result.add(isbn);
	}
	return result;
    }

    /*
     * Return num stock books. For now return an ImmutableStockBook
     */
    
    /**
     * @param num
     * @return
     * @throws BookStoreException
     */
    public Set<StockBook> nextSetOfStockBooks(int num)
	    throws BookStoreException {
	if (num < 0)
	    throw new BookStoreException("Num is smaller than 0");
	Set<StockBook> books = new HashSet<StockBook>();
	List<Integer> isbns = new ArrayList<Integer>();

	for (; books.size() < num;) {
	    StockBook book = generateRandomStockBook();
	    if (!isbns.contains(book.getISBN())) {
		books.add(book);
		isbns.add(book.getISBN());
	    }
	}
	return books;
    }

    private StockBook generateRandomStockBook() {
	return new ImmutableStockBook(generateISBN(), generateRandString(),
		generateRandString(), generatePrice(), generateNumCopies(),
		generateNextLong(2951, 364), generateNextLong(2571, 271),
		generateNextLong(2567, 534), rand.nextBoolean());
    }

    
    private long generateNextLong(int multiply, int sum) {
	return (long) (rand.nextDouble() * multiply + sum);
    }

    private int generateNumCopies() {
	return rand.nextInt(1234) + 654;
    }

    private float generatePrice() {
	return rand.nextFloat() * 586f + 467f;
    }

    private String generateRandString() {
	String alphabet = "abcdefghijklmnopqrstuvwxyz";
	StringBuilder sb = new StringBuilder();
	char[] chars = alphabet.toCharArray();
	final int strSize = 20;
	for (int i = 0; i < strSize; i++) {
	    sb.append(chars[rand.nextInt(chars.length)]);
	}
	return sb.toString();
    }

    private int generateISBN() {
	final int ISBN_RANGE = 9876543;
	final int ISBN_SUM = 123456;
	return rand.nextInt(ISBN_RANGE) + ISBN_SUM;
    }
}
