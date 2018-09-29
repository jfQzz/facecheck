/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.meibaa.zab.face.parser;


import com.meibaa.zab.face.exception.FaceError;

/**
 * JSON解析
 * @param <T>
 */
public interface Parser<T> {
    T parse(String json) throws FaceError;
}
