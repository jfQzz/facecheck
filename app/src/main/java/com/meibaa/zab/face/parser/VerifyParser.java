/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.meibaa.zab.face.parser;

import android.text.TextUtils;
import android.util.Log;


import com.meibaa.zab.face.exception.FaceError;
import com.meibaa.zab.face.model.FaceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VerifyParser implements Parser<FaceModel> {
    @Override
    public FaceModel parse(String json) throws FaceError {

        Log.d("VerifyParser",json);
        FaceModel faceModel = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            int errorCode = jsonObject.optInt("error_code");
            if (errorCode > 0) {
                String errorMsg = jsonObject.optString("error_msg");
                FaceError faceError = new FaceError(errorCode, errorMsg);
                throw faceError;
            }
            JSONArray resultArray = jsonObject.optJSONArray("result");
            faceModel = new FaceModel();
            if (resultArray != null) {
                faceModel.setScore(resultArray.getDouble(0));
            }
            JSONObject extInfo = jsonObject.optJSONObject("ext_info");
            if (extInfo != null) {
                String faceliveness = extInfo.optString("faceliveness");
                if (!TextUtils.isEmpty(faceliveness)) {
                    faceModel.setFaceliveness(faceliveness);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return faceModel;
    }
}
