/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.meibaa.zab.face.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DynamicParams implements RequestParams {

    private Map<String, String> params = new HashMap<>();
    private Map<String, File> fileMap = new HashMap<>();
    private String jsonParams = "";

    @Override
    public Map<String, File> getFileParams() {
        return fileMap;
    }

    @Override
    public Map<String, String> getStringParams() {
        return params;
    }

    @Override
    public String getJsonParams(){
        return jsonParams;
    }

    public void putParam(String key, String value) {
        if (value != null) {
            params.put(key, value);
        }
    }

    public void putParam(String key, int value) {

        params.put(key, String.valueOf(value));
    }


    public void putParam(String key, boolean value) {
        if (value) {
            putParam(key, "true");
        } else {
            putParam(key, "false");
        }
    }

    public void putFile(String key, File file) {
        fileMap.put(key, file);
    }


    public void setJsonParams(String jsonParams) {
        this.jsonParams = jsonParams;
    }

    public void setBase64Img(String base64Img) {
        putParam("image", base64Img);
    }

    public void setImgType(String imgType) {
        putParam("image_type", imgType);
    }

    public void setQualityControl(String qualControl) {
        putParam("quality_control", qualControl);
    }
    public void setLivenessControl(String liveControl) {
        putParam("liveness_control", liveControl);
    }



}
