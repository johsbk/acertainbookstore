package com.acertainbookstore.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.concurrent.ReentrantReadWriteUpdateLock;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreUtility;

public class ConcurrentCertainBookStore implements BookStore, StockManager {
    private static ConcurrentCertainBookStore singleInstance;
    private static Map<Integer, BookStoreBook> bookMap;
    private final static ReentrantReadWriteUpdateLock bsLock = new ReentrantReadWriteUpdateLock();

    private ConcurrentCertainBookStore() {
    }

    public synchronized static ConcurrentCertainBookStore getInstance() {
	if (singleInstance != null) {
	    return singleInstance;
	} else {
	    singleInstance = new ConcurrentCertainBookStore();
	    bookMap = new HashMap<Integer, BookStoreBook>();
	}
	return singleInstance;
    }

    public synchronized static void reset() {
	bookMap = new HashMap<Integer, BookStoreBook>();
    }

    public void addBooks(Set<StockBook> bookSet) throws BookStoreException {

	if (bookSet == null) {
	    throw new BookStoreException(BookStoreConstants.NULL_INPUT);
	}
	try {
	    // Check if all are there
	    bsLock.updateLock().lock();
	    for (StockBook book : bookSet) {
		int ISBN = book.getISBN();
		String bookTitle = book.getTitle();
		String bookAuthor = book.getAuthor();
		int noCopies = book.getNumCopies();
		float bookPrice = book.getPrice();
		if (BookStoreUtility.isInvalidISBN(ISBN)
			|| BookStoreUtility.isEmpty(bookTitle)
			|| BookStoreUtility.isEmpty(bookAuthor)
			|| BookStoreUtility.isInvalidNoCopies(noCopies)
			|| bookPrice < 0.0) {
		    throw new BookStoreException(BookStoreConstants.BOOK
			    + book.toString() + BookStoreConstants.INVALID);
		} else if (bookMap.containsKey(ISBN)) {
		    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
			    + BookStoreConstants.DUPLICATED);
		}
	    }
	    bsLock.writeLock().lock();
	    for (StockBook book : bookSet) {
		int ISBN = book.getISBN();
		bookMap.put(ISBN, new BookStoreBook(book));
	    }
	} finally {
	    bsLock.writeLock().unlock();
	    bsLock.updateLock().unlock();
	}
	return;
    }

    public void addCopies(Set<BookCopy> bookCopiesSet)
	    throws BookStoreException {
	int ISBN, numCopies;

	if (bookCopiesSet == null) {
	    throw new BookStoreException(BookStoreConstants.NULL_INPUT);
	}
	try {
	    bsLock.updateLock().lock();
	    for (BookCopy bookCopy : bookCopiesSet) {
		ISBN = bookCopy.getISBN();
		numCopies = bookCopy.getNumCopies();
		if (BookStoreUtility.isInvalidISBN(ISBN))
		    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
			    + BookStoreConstants.INVALID);
		if (!bookMap.containsKey(ISBN))
		    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
			    + BookStoreConstants.INVALID);
		if (BookStoreUtility.isInvalidNoCopies(numCopies))
		    throw new BookStoreException(BookStoreConstants.NUM_COPIES
			    + numCopies + BookStoreConstants.INVALID);

	    }
	    bsLock.writeLock().lock();
	    BookStoreBook book;
	    // Update the number of copies
	    for (BookCopy bookCopy : bookCopiesSet) {
		ISBN = bookCopy.getISBN();
		numCopies = bookCopy.getNumCopies();
		book = bookMap.get(ISBN);
		book.addCopies(numCopies);
	    }

	} finally {
	    bsLock.writeLock().unlock();
	    bsLock.updateLock().unlock();
	}
    }

    public List<StockBook> getBooks() {
	List<StockBook> listBooks = new ArrayList<StockBook>();
	try {
	    bsLock.readLock().lock();
	    Collection<BookStoreBook> bookMapValues = bookMap.values();
	    for (BookStoreBook book : bookMapValues) {
		listBooks.add(book.immutableStockBook());
	    }
	} finally {
	    bsLock.readLock().unlock();
	}
	return listBooks;
    }

    public void updateEditorPicks(Set<BookEditorPick> editorPicks)
	    throws BookStoreException {
	// Check that all ISBNs that we add/remove are there first.
	if (editorPicks == null) {
	    throw new BookStoreException(BookStoreConstants.NULL_INPUT);
	}
	int ISBNVal;
	try {
	    bsLock.updateLock().lock();
	    for (BookEditorPick editorPickArg : editorPicks) {
		ISBNVal = editorPickArg.getISBN();
		if (BookStoreUtility.isInvalidISBN(ISBNVal))
		    throw new BookStoreException(BookStoreConstants.ISBN
			    + ISBNVal + BookStoreConstants.INVALID);
		if (!bookMap.containsKey(ISBNVal))
		    throw new BookStoreException(BookStoreConstants.ISBN
			    + ISBNVal + BookStoreConstants.NOT_AVAILABLE);
	    }
	    bsLock.writeLock().lock();
	    for (BookEditorPick editorPickArg : editorPicks) {
		bookMap.get(editorPickArg.getISBN()).setEditorPick(
			editorPickArg.isEditorPick());
	    }
	} finally {
	    bsLock.writeLock().unlock();
	    bsLock.updateLock().unlock();
	}
	return;
    }

    public void buyBooks(Set<BookCopy> bookCopiesToBuy)
	    throws BookStoreException {
	if (bookCopiesToBuy == null) {
	    throw new BookStoreException(BookStoreConstants.NULL_INPUT);
	}
	// Check that all ISBNs that we buy are there first.
	int ISBN;
	BookStoreBook book;
	Boolean saleMiss = false;
	try {
	    bsLock.updateLock().lock();
	    for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
		ISBN = bookCopyToBuy.getISBN();
		if (BookStoreUtility.isInvalidISBN(ISBN))
		    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
			    + BookStoreConstants.INVALID);
		if (!bookMap.containsKey(ISBN))
		    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
			    + BookStoreConstants.NOT_AVAILABLE);
		book = bookMap.get(ISBN);
		if (!book.areCopiesInStore(bookCopyToBuy.getNumCopies())) {
		    book.addSaleMiss(); // If we cannot sell the copies of the
					// book
					// its a miss
		    saleMiss = true;
		}
	    }

	    // We throw exception now since we want to see how many books in the
	    // order incurred misses which is used by books in demand
	    if (saleMiss)
		throw new BookStoreException(BookStoreConstants.BOOK
			+ BookStoreConstants.NOT_AVAILABLE);
	    bsLock.writeLock().lock();
	    // Then make purchase
	    for (BookCopy bookCopyToBuy : bookCopiesToBuy) {
		book = bookMap.get(bookCopyToBuy.getISBN());
		book.buyCopies(bookCopyToBuy.getNumCopies());
	    }
	} finally {
	    bsLock.writeLock().unlock();
	    bsLock.updateLock().unlock();
	}
	return;
    }

    public List<Book> getBooks(Set<Integer> isbnSet) throws BookStoreException {
	if (isbnSet == null) {
	    throw new BookStoreException(BookStoreConstants.NULL_INPUT);
	}
	List<Book> listBooks = new ArrayList<Book>();
	try {
	    bsLock.readLock().lock();
	    // Check that all ISBNs that we rate are there first.
	    for (Integer ISBN : isbnSet) {
		if (BookStoreUtility.isInvalidISBN(ISBN))
		    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
			    + BookStoreConstants.INVALID);
		if (!bookMap.containsKey(ISBN))
		    throw new BookStoreException(BookStoreConstants.ISBN + ISBN
			    + BookStoreConstants.NOT_AVAILABLE);
	    }

	    // Get the books
	    for (Integer ISBN : isbnSet) {
		listBooks.add(bookMap.get(ISBN).immutableBook());
	    }
	} finally {
	    bsLock.readLock().unlock();
	}
	return listBooks;
    }

    public List<Book> getEditorPicks(int numBooks) throws BookStoreException {
	if (numBooks < 0) {
	    throw new BookStoreException("numBooks = " + numBooks
		    + ", but it must be positive");
	}

	List<BookStoreBook> listAllEditorPicks = new ArrayList<BookStoreBook>();
	List<Book> listEditorPicks = new ArrayList<Book>();
	try {
	    bsLock.readLock().lock();
	    Iterator<Entry<Integer, BookStoreBook>> it = bookMap.entrySet()
		    .iterator();
	    BookStoreBook book;
	    // Get all books that are editor picks
	    while (it.hasNext()) {
		Entry<Integer, BookStoreBook> pair = (Entry<Integer, BookStoreBook>) it
			.next();
		book = (BookStoreBook) pair.getValue();
		if (book.isEditorPick()) {
		    listAllEditorPicks.add(book);
		}
	    }
	    // Find numBooks random indices of books that will be picked
	    Random rand = new Random();
	    Set<Integer> tobePicked = new HashSet<Integer>();
	    int rangePicks = listAllEditorPicks.size();
	    if (rangePicks < numBooks) {
		throw new BookStoreException("Only " + rangePicks
			+ " editor picks are available.");
	    }
	    int randNum;
	    while (tobePicked.size() < numBooks) {
		randNum = rand.nextInt(rangePicks);
		tobePicked.add(randNum);
	    }
	    // Get the numBooks random books
	    for (Integer index : tobePicked) {
		book = listAllEditorPicks.get(index);
		listEditorPicks.add(book.immutableBook());
	    }
	} finally {
	    bsLock.readLock().unlock();
	}
	return listEditorPicks;
    }

    @Override
    public List<Book> getTopRatedBooks(int numBooks) throws BookStoreException {
	// TODO Auto-generated method stub
    	// No negative number
    	if (numBooks <= 0) {
    	    throw new BookStoreException("numBooks = " + numBooks
    		    + ", but it must be positive");
    	}
    	Collection<BookStoreBook> books = bookMap.values();
    	if (books.isEmpty()) {
    	    throw new BookStoreException("No books in the bookstore");
    	}

    	List<BookStoreBook> listRatedBooks = new ArrayList<BookStoreBook>();
    	List<Book> listTopRatedBooks = new ArrayList<Book>();
    	try
    	{
    		bsLock.readLock().lock(); // block other from writing data
    		// we only count the book has a rating
        	for (BookStoreBook book : books) {
        	    if (book.getAverageRating() > 0)
        		listRatedBooks.add(book);
        	}

        	Comparator<BookStoreBook> comparator = new Comparator<BookStoreBook>() {
        	    public int compare(BookStoreBook book1, BookStoreBook book2) {
        		return Float.compare(book2.getAverageRating(),
        			book1.getAverageRating()); // get right order of books
        	    }
        	};

        	Collections.sort(listRatedBooks, comparator);

        	for (BookStoreBook book : listRatedBooks) {
        	    listTopRatedBooks.add(book.immutableStockBook());
        	    if (listTopRatedBooks.size() == numBooks)
        		break;
        	}
    	}
    	finally
    	{
    		bsLock.readLock().unlock();
    	}
    	
    	// return the certain amount of books by its rating
    	return listTopRatedBooks;
    }

    @Override
    public List<StockBook> getBooksInDemand() throws BookStoreException {
	// TODO Auto-generated method stub
    	bsLock.updateLock().lock();
    	List<StockBook> listBooksInDemand = new ArrayList<StockBook>();
    	Collection<BookStoreBook> books = bookMap.values();
    	if (books.isEmpty()) {
    	    throw new BookStoreException("No books in the bookstore");
    	}
    	try
    	{
    		for (BookStoreBook book : books) {
        	    if (book.hadSaleMiss()) {
        		listBooksInDemand.add(book.immutableStockBook());
        	    }
        	}
    	}
    	finally
    	{
    		bsLock.updateLock().unlock();
    	}
    	return listBooksInDemand;
    }

    @Override
    public void rateBooks(Set<BookRating> bookRating) throws BookStoreException {
	// TODO Auto-generated method stub
    	//lock update lock, only allow read
    	bsLock.updateLock().lock();
    	try
    	{
    		/* Check if the input is valid */
        	if (bookRating == null)
        	    throw new BookStoreException(BookStoreConstants.NULL_INPUT);
        	
        	int ISBN, rating;
        	BookStoreBook book;
        	for (BookRating br : bookRating) {
        	    ISBN = br.getISBN();
        	    rating = br.getRating();
        	    if (BookStoreUtility.isInvalidISBN(ISBN))
        		throw new BookStoreException(BookStoreConstants.ISBN + ISBN
        			+ BookStoreConstants.INVALID);
        	    if (!bookMap.containsKey(ISBN))
        		throw new BookStoreException(BookStoreConstants.ISBN + ISBN
        			+ BookStoreConstants.NOT_AVAILABLE);
        	    if (BookStoreUtility.isInvalidRating(rating))
        		throw new BookStoreException(BookStoreConstants.RATING + rating
        			+ BookStoreConstants.INVALID);
        	}
        	
        	try
        	{
        		bsLock.writeLock().lock(); // upgrade to write lock
        		/* Inputs are valid so, ready to rate the books change */
            	for (BookRating br : bookRating) {
            	    book = bookMap.get(br.getISBN());
            	    book.addRating(br.getRating());
            	}
        	}
        	finally
        	{
        		bsLock.writeLock().unlock(); // downgrade to update lock
        	}
        	
    	}
    	finally
    	{
    		bsLock.updateLock().unlock();
    	}

    	return;
    }

}
