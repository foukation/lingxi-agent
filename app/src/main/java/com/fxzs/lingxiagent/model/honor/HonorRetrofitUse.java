package com.fxzs.lingxiagent.model.honor;

import android.content.Context;
import androidx.annotation.NonNull;
import com.fxzs.lingxiagent.model.common.Constants;
import com.fxzs.lingxiagent.model.honor.dto.BodyData;
import com.fxzs.lingxiagent.model.honor.dto.CommandsData;
import com.fxzs.lingxiagent.model.honor.dto.TripHonorRes;
import com.fxzs.lingxiagent.model.honor.repository.HonorRepositoryImpl;
import com.fxzs.lingxiagent.model.honor.repository.StreamHandler;

import timber.log.Timber;

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

    public void execTripHonor(String inputString) {
        StringBuilder totalCotText = new StringBuilder();
        StringBuilder totalText = new StringBuilder();
        final boolean[] isFirCot = {true};
        final int[] frameTime = {0};
        final int[] totalTime = {0};
        honorHttp.sendStreamRequest(inputString, new StreamHandler() {
            @Override
            public void onStreamStop() {

            }

            @Override
            public void onDataChunk(@NonNull TripHonorRes resp) {
                Timber.tag("onDataChunk").d("onDataChunk:%s", resp.getChoices().getMessage().getHybridContent());
                if (resp.getErrorCode().equals("0")) {
                    CommandsData commands = resp.getChoices().getMessage().getHybridContent().getCommands();
                    String type = commands.getHead().getNamespace();
                    BodyData body = commands.getBody();
                    String richText = body.getText();
                    if (type.equals("think")) {
                        totalCotText.append(richText);
                        frameTime[0] = richText.length() * 30;
                        totalTime[0] += frameTime[0];
                        if (isFirCot[0]) {
                            isFirCot[0] = false;
                        }
                    }
                    else if (isFirCot[0] && type.equals("rich_text")) {
                        if (richText != null) {
                            totalText.append(richText);
                        }
                    }
                    else if (!isFirCot[0] && type.equals("rich_text")) {
                        if (richText != null) {
                            totalText.append(richText);
                        }
                    }
                    else if (type.equals("card")) {

                    }
                }
            }

            @Override
            public void onStreamComplete() {
                String cotText = String.valueOf(totalCotText);
                honorHttp.updateMessages("assistant", String.valueOf(totalText), "text");
            }

            @Override
            public void onError(@NonNull String errMsg) {
            }
        });
    }
}