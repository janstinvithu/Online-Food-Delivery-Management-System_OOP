package com.fooddelivery.persistence;

import com.fooddelivery.persistence.dto.SequencesData;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataStoreManager {

    private static final Logger log = LoggerFactory.getLogger(DataStoreManager.class);

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final FileDataStore store;
    private ApplicationData cache;
    private boolean writesEnabled = true;

    public DataStoreManager(FileDataStore store) {
        this.store = store;
        this.cache = store.loadOrCreate();
    }

    public <T> T read(Function<ApplicationData, T> work) {
        lock.readLock().lock();
        try {
            return work.apply(cache);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> T update(Function<ApplicationData, T> work) {
        lock.writeLock().lock();
        try {
            if (!writesEnabled) {
                throw new DataStoreException("JSON writes are disabled because the data file could not be recovered safely");
            }
            try {
                T result = work.apply(cache);
                cache.alignSequences();
                store.save(cache);
                return result;
            } catch (RuntimeException ex) {
                SequencesData highWater = cache.getSequences() == null
                        ? new SequencesData()
                        : cache.getSequences().copy();
                restoreAfterFailure(highWater);
                throw ex;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void restoreAfterFailure(SequencesData highWater) {
        try {
            ApplicationData disk = store.readExisting();
            disk.getSequences().raiseTo(highWater);
            this.cache = disk;
        } catch (RuntimeException reloadFailure) {
            writesEnabled = false;
            log.error("Failed to reload the JSON store after a rejected write. Further writes are blocked.", reloadFailure);
        }
    }
}
