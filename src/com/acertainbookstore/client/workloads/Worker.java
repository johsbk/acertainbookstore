/**
 * 
 */
package com.acertainbookstore.client.workloads;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

/**
 * 
 * Worker represents the workload runner which runs the workloads with
 * parameters using WorkloadConfiguration and then reports the results
 * 
 */
public class Worker implements Callable<WorkerRunResult> {
	private WorkloadConfiguration configuration = null;
	private int numSuccessfulFrequentBookStoreInteraction = 0;
	private int numTotalFrequentBookStoreInteraction = 0;

	public Worker(WorkloadConfiguration config) {
		configuration = config;
	}

	/**
	 * Run the appropriate interaction while trying to maintain the configured
	 * distributions
	 * 
	 * Updates the counts of total runs and successful runs for customer
	 * interaction
	 * 
	 * @param chooseInteraction
	 * @return
	 */
	private boolean runInteraction(float chooseInteraction) {
		try {
			if (chooseInteraction < configuration
					.getPercentRareStockManagerInteraction()) {
				runRareStockManagerInteraction();
			} else if (chooseInteraction < configuration
					.getPercentFrequentStockManagerInteraction()) {
				runFrequentStockManagerInteraction();
			} else {
				numTotalFrequentBookStoreInteraction++;
				runFrequentBookStoreInteraction();
				numSuccessfulFrequentBookStoreInteraction++;
			}
		} catch (BookStoreException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Run the workloads trying to respect the distributions of the interactions
	 * and return result in the end
	 */
	public WorkerRunResult call() throws Exception {
		int count = 1;
		long startTimeInNanoSecs = 0;
		long endTimeInNanoSecs = 0;
		int successfulInteractions = 0;
		long timeForRunsInNanoSecs = 0;

		Random rand = new Random();
		float chooseInteraction;

		// Perform the warmup runs
		while (count++ <= configuration.getWarmUpRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			runInteraction(chooseInteraction);
		}

		count = 1;
		numTotalFrequentBookStoreInteraction = 0;
		numSuccessfulFrequentBookStoreInteraction = 0;

		// Perform the actual runs
		startTimeInNanoSecs = System.nanoTime();
		while (count++ <= configuration.getNumActualRuns()) {
			chooseInteraction = rand.nextFloat() * 100f;
			if (runInteraction(chooseInteraction)) {
				successfulInteractions++;
			}
		}
		endTimeInNanoSecs = System.nanoTime();
		timeForRunsInNanoSecs += (endTimeInNanoSecs - startTimeInNanoSecs);
		return new WorkerRunResult(successfulInteractions,
				timeForRunsInNanoSecs, configuration.getNumActualRuns(),
				numSuccessfulFrequentBookStoreInteraction,
				numTotalFrequentBookStoreInteraction);
	}

	/**
	 * Runs the new stock acquisition interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runRareStockManagerInteraction() throws BookStoreException {
		StockManager stockManager = configuration.getStockManager();
		BookSetGenerator bsg = configuration.getBookSetGenerator();
		Set<StockBook> booksToAdd = bsg.nextSetOfStockBooks(configuration.getNumBooksToAdd());
		List<StockBook> allBooks = stockManager.getBooks();
		
		Set<StockBook> missingBooks = new HashSet<StockBook>();
		Iterator<StockBook> it = booksToAdd.iterator();
		while(it.hasNext()) {
			StockBook book = it.next();
			Boolean found = false;
			for(StockBook oldbook : allBooks) {
				if (oldbook.getISBN()==book.getISBN()) {
					found = true;
					break;
				}
			}
			if (!found) {
				missingBooks.add(book);
			}
		}

		stockManager.addBooks(missingBooks);
		return;
	}

	/**
	 * Runs the stock replenishment interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runFrequentStockManagerInteraction() throws BookStoreException {
		// TODO: Add code for Stock Replenishment Interaction
		StockManager stockManager = configuration.getStockManager();
		List<StockBook> allBooks = stockManager.getBooks();
		int k = configuration.getNumLackStock();
		int numCopies = configuration.getNumAddCopies();
		
		if (k>allBooks.size()) {
			throw new BookStoreException("Num lack stock is higher than the number of books in the store");
		}
		
		PriorityQueue<StockBook> pq = new PriorityQueue<StockBook>(allBooks.size(), new Comparator<StockBook>() {

			@Override
			public int compare(StockBook o1, StockBook o2) {
				
				return new Integer(o1.getNumCopies()).compareTo(new Integer(o2.getNumCopies()));
			}
			
		});
		Set<BookCopy> bookCopies = new HashSet<BookCopy>();
		pq.addAll(allBooks);
		for (int i=0;i<k;i++) {
			StockBook book = pq.poll();
			bookCopies.add(new BookCopy(book.getISBN(), numCopies));
		}
		stockManager.addCopies(bookCopies);
	}

	/**
	 * Runs the customer interaction
	 * 
	 * @throws BookStoreException
	 */
	private void runFrequentBookStoreInteraction() throws BookStoreException {
		// TODO: Add code for Customer Interaction
		BookStore bookStore = configuration.getBookStore();
		BookSetGenerator bsg = configuration.getBookSetGenerator();
		int numBooks = configuration.getNumEditorPicksToGet();
		int numBooksToBuy = configuration.getNumBooksToBuy();
		List<Book> editorPicks = bookStore.getEditorPicks(numBooks);
		if (numBooksToBuy>editorPicks.size()) {
			throw new BookStoreException("Num books to buy is higher than the number of editor picks in the store");
		}
		Set<Integer> isbns = new HashSet<Integer>();
		for (Book book : editorPicks) {
			isbns.add(book.getISBN());
		}
		Set<Integer> isbnsTobuy = bsg.sampleFromSetOfISBNs(isbns, numBooksToBuy);
		Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		for (Integer isbn : isbnsTobuy) {
			booksToBuy.add(new BookCopy(isbn, 1));
		}
		bookStore.buyBooks(booksToBuy);
	}

}
