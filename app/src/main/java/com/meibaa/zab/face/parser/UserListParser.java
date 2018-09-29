/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.meibaa.zab.face.parser;


import com.meibaa.zab.face.exception.FaceError;
import com.meibaa.zab.face.model.FaceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserListParser implements Parser<List<FaceModel>> {
    @Override
    public List<FaceModel> parse(String json) throws FaceError {
        try {
            JSONObject jsonObject = new JSONObject(json);
            ArrayList<FaceModel> faceModels = new ArrayList<>();
            JSONArray resultArray = jsonObject.getJSONArray("result");
            for (int i = 0;i < resultArray.length();i++) {
                FaceModel faceModel = new FaceModel();
                JSONObject faceObject = resultArray.getJSONObject(i);
                faceModel.setUid(faceObject.getString("uid"));
                faceModel.setUserInfo(faceObject.getString("user_info"));
                faceModels.add(faceModel);
            }
            return faceModels;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
