package com.acertainbookstore.client.workloads;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
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
	private Random randNum = new Random();
	public BookSetGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns num randomly selected isbns from the input set
	 * 
	 * @param num
	 * @return
	 * @throws BookStoreException 
	 */
	public Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num) throws BookStoreException {
		Set<Integer> sampleSet = new HashSet<Integer>();
		// old code throw cast object exception, now ok -- yulong
		Integer[] isbnArray = (Integer[]) isbns.toArray(new Integer[0]); 
		int range = isbns.size();
		if (range < num) {
			throw new BookStoreException("Num is larger than the size of the given set");
		}
		if (num < 0)
			throw new BookStoreException("Num is less than 0");
		else {
			while (sampleSet.size() < num) {
				range = isbns.size();
				int random = randNum.nextInt(range);

				sampleSet.add(isbnArray[random]);
			}
		}
		return sampleSet;
	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 * @throws BookStoreException 
	 */
	public Set<StockBook> nextSetOfStockBooks(int num) throws BookStoreException {
		if (num < 0)
			throw new BookStoreException("Num is less than 0");
		Set<StockBook> setOfBooks = new HashSet<StockBook>();
		ArrayList<Integer> isbns = new ArrayList<Integer>();
		while(setOfBooks.size()<num) {
			StockBook book = this.generateStockBook();
			if (!isbns.contains(book.getISBN())) {
				setOfBooks.add(book);
				isbns.add(book.getISBN());
			}
			
		}
		return setOfBooks;
	}
	private StockBook generateStockBook() {
		return new ImmutableStockBook(this.generateISBN(), this.generateRandomString(), this.generateRandomString(),  randNum.nextFloat()*1000f+10f, randNum.nextInt(1000)+1, randNum.nextLong(), randNum.nextLong(), randNum.nextLong(), randNum.nextBoolean());
	}
	private Integer generateISBN() {
		return new Integer(randNum.nextInt(1000000)+100);
	}
	private String generateRandomString() {
		return new BigInteger(130, randNum).toString(32);
	}
}
