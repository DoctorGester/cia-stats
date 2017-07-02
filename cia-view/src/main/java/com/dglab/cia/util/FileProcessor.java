package com.dglab.cia.util;

/**
 * Created by shoujo on 2/2/2017.
 */
public interface FileProcessor<T> {
    T process(String fileData);
}
