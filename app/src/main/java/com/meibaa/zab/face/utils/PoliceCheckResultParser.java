/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.meibaa.zab.face.utils;

import android.util.Log;


import com.meibaa.zab.face.exception.FaceError;
import com.meibaa.zab.face.model.LivenessVsIdcardResult;
import com.meibaa.zab.face.parser.Parser;

import org.json.JSONException;
import org.json.JSONObject;

public class PoliceCheckResultParser implements Parser<LivenessVsIdcardResult> {

    @Override
    public LivenessVsIdcardResult parse(String json) throws FaceError {

        Log.i("PoliceCheckResultParser", "LivenessVsIdcardResult->" + json);
        try {
            JSONObject jsonObject = new JSONObject(json);

            if (jsonObject.has("error_code")) {
                FaceError error = new FaceError(jsonObject.optInt("error_code"),
                        jsonObject.optString("error_msg"));
                if (error.getErrorCode() != 0) {
                    throw error;
                }
            }

            LivenessVsIdcardResult result = new LivenessVsIdcardResult();
            result.setLogId(jsonObject.optLong("log_id"));
            result.setJsonRes(json);

            JSONObject resultObject = jsonObject.optJSONObject("result");
            if (resultObject != null) {
                double score = resultObject.optDouble("score");
                result.setScore(score);
            }

            return result;
        } catch (JSONException e) {
            e.printStackTrace();
            FaceError error = new FaceError(FaceError.ErrorCode.JSON_PARSE_ERROR,
                    "Json parse error:" + json, e);
            throw error;
        }
    }
}
