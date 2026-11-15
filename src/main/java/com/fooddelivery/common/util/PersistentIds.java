package com.fooddelivery.common.util;

public final class PersistentIds {

    private PersistentIds() {
    }

    public static Long assign(Long currentId, Long persistentId) {
        if (persistentId == null || persistentId <= 0) {
            throw new IllegalArgumentException("Persistent id must be a positive number");
        }
        if (currentId != null && !currentId.equals(persistentId)) {
            throw new IllegalStateException("Persistent id is already assigned");
        }
        return persistentId;
    }
}
