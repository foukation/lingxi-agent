package com.fxzs.lingxiagent.model.honor;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.honor.dto.BodyData;
import com.fxzs.lingxiagent.model.honor.dto.CommandsData;
import com.fxzs.lingxiagent.model.honor.dto.TripHonorRes;
import com.fxzs.lingxiagent.model.honor.repository.HonorRepositoryImpl;
import com.fxzs.lingxiagent.model.honor.repository.StreamHandler;

public class HonorRetrofitUse {

    private static HonorRetrofitUse instance;
    HonorRepositoryImpl honorHttp;

    private HonorRetrofitUse(Context context) {
        honorHttp = new HonorRepositoryImpl(context);
    }
    
    public static synchronized HonorRetrofitUse getInstance(Context context) {
        if (instance == null) {
            instance = new HonorRetrofitUse(context);
        }
        return instance;
    }

    public void execTripHonorUse() {
        honorHttp.updateRequestInfo(Constants.HONOR_TRIP);
        execTripHonor("帮我订个北京去西安三天的旅行规划", honorHttp);
    }

    public void execMeetHonorUse() {
        honorHttp.updateRequestInfo(Constants.HONOR_MEET);
        execTripHonor("帮我在北京西站和五道口和鸟巢中间选个餐厅", honorHttp);
    }

    public void execTripHonor(String inputString, HonorRepositoryImpl HonorHttp) {
        StringBuilder totalCotText = new StringBuilder();
        StringBuilder totalText = new StringBuilder();
        final boolean[] isFirCot = {true};
        final int[] frameTime = {0};
        final int[] totalTime = {0};
        Log.d("HonorRetrofitUse", "execTripHonor start ");
        HonorHttp.sendStreamRequest(inputString, new StreamHandler() {
            @Override
            public void onStreamStop() {
                Log.d("HonorRetrofitUse", "onStreamStop ");
            }

            @Override
            public void onDataChunk(@NonNull TripHonorRes resp) {
                if (resp.getErrorCode().equals("0")) {
                    CommandsData commands = resp.getChoices().getMessage().getHybridContent().getCommands();
                    String type = commands.getHead().getNamespace();
                    BodyData body = commands.getBody();
                    String richText = body.getText();
                    if (type.equals("think")) {
                        Log.d("HonorRetrofitUse","消息进度onDataChunk COT %s" + richText);
                        totalCotText.append(richText);
                        frameTime[0] = richText.length() * 30;
                        totalTime[0] += frameTime[0];
                        if (isFirCot[0]) {
                            isFirCot[0] = false;
                        }
                    }
                    else if (isFirCot[0] && type.equals("rich_text")) {
                        Log.d("HonorRetrofitUse","消息进度onDataChunk RICH_TEXT %s" + richText);
                        if (richText != null) {
                            totalText.append(richText);
                        }
                    }
                    else if (!isFirCot[0] && type.equals("rich_text")) {
                        Log.d("HonorRetrofitUse","消息进度onDataChunk COT_SUMMARY %s" + richText);
                        if (richText != null) {
                            totalText.append(richText);
                        }
                    }
                    else if (type.equals("card")) {
                        Log.d("HonorRetrofitUse","消息进度onDataChunk CARD %s" + body);
                    }
                }
            }

            @Override
            public void onStreamComplete() {
                String cotText = String.valueOf(totalCotText);
                HonorHttp.updateMessages("assistant", String.valueOf(totalText), "text");
                Log.d("HonorRetrofitUse","消息进度onStreamComplete " + cotText);
            }

            @Override
            public void onError(@NonNull String errMsg) {
                Log.d("HonorRetrofitUse", "onError " + errMsg );
            }
        });
    }
}