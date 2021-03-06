/**
 * Copyright 2013 Niall Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acertainbookstore.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

public class ReentrantReadWriteUpdateLock implements ReadWriteUpdateLock {

    final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    final Lock updateMutex = new ReentrantLock();
    final ReadLock readLock = new ReadLock();
    final UpdateLock updateLock = new UpdateLock();
    final WriteLock writeLock = new WriteLock();

    @Override
    public Lock updateLock() {
	return updateLock;
    }

    @Override
    public Lock readLock() {
	return readLock;
    }

    @Override
    public Lock writeLock() {
	return writeLock;
    }

    static abstract class HoldCountLock implements Lock {

	static class HoldCount {
	    int value;
	}

	final ThreadLocal<HoldCount> threadHoldCount = new ThreadLocal<HoldCount>() {
	    @Override
	    protected HoldCount initialValue() {
		return new HoldCount();
	    }
	};

	final Lock backingLock;

	public HoldCountLock(Lock backingLock) {
	    this.backingLock = backingLock;
	}

	HoldCount holdCount() {
	    return threadHoldCount.get();
	}

	@Override
	public void lock() {
	    validatePreconditions();
	    backingLock.lock();
	    holdCount().value++;
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
	    validatePreconditions();
	    backingLock.lockInterruptibly();
	    holdCount().value++;
	}

	@Override
	public boolean tryLock() {
	    validatePreconditions();
	    if (backingLock.tryLock()) {
		holdCount().value++;
		return true;
	    }
	    return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit)
		throws InterruptedException {
	    validatePreconditions();
	    if (backingLock.tryLock(time, unit)) {
		holdCount().value++;
		return true;
	    }
	    return false;
	}

	@Override
	public void unlock() {
	    backingLock.unlock();
	    holdCount().value--;
	}

	@Override
	public Condition newCondition() {
	    throw new UnsupportedOperationException(
		    "This lock does not support conditions");
	}

	abstract void validatePreconditions();
    }

    class ReadLock extends HoldCountLock {

	public ReadLock() {
	    super(readWriteLock.readLock());
	}

	void validatePreconditions() {
	    if (updateLock.holdCount().value > 0) {
		throw new IllegalStateException(
			"Cannot acquire read lock, as this thread previously acquired and must first release the update lock");
	    }
	}
    }

    class UpdateLock extends HoldCountLock {

	public UpdateLock() {
	    super(updateMutex);
	}

	void validatePreconditions() {
	    if (readLock.holdCount().value > 0) {
		throw new IllegalStateException(
			"Cannot acquire update lock, as this thread previously acquired and must first release the read lock");
	    }
	}
    }

    class WriteLock implements Lock {

	@Override
	public void lock() {
	    validatePreconditions();
	    // Acquire UPDATE lock again, even if calling thread might already
	    // hold it.
	    // This allow threads to go from both NONE -> WRITE and from UPDATE
	    // -> WRITE.
	    // This also ensures that only the thread holding the single UPDATE
	    // lock,
	    // can request the WRITE lock...
	    Locks.lockAll(updateLock, readWriteLock.writeLock());
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
	    validatePreconditions();
	    Locks.lockInterruptiblyAll(updateLock, readWriteLock.writeLock());
	}

	@Override
	public boolean tryLock() {
	    validatePreconditions();
	    return Locks.tryLockAll(updateLock, readWriteLock.writeLock());
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit)
		throws InterruptedException {
	    validatePreconditions();
	    return Locks.tryLockAll(time, unit, updateLock,
		    readWriteLock.writeLock());
	}

	@Override
	public void unlock() {
	    Locks.unlockAll(readWriteLock.writeLock(), updateLock);
	}

	@Override
	public Condition newCondition() {
	    throw new UnsupportedOperationException(
		    "This lock does not support conditions");
	}

	void validatePreconditions() {
	    if (readLock.holdCount().value > 0) {
		throw new IllegalStateException(
			"Cannot acquire write lock, as this thread previously acquired and must first release the read lock");
	    }
	}
    }
}
