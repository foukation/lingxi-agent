
package com.fxzs.lingxiagent.model.chat.callback;
public interface StsCallback {
     void progress(long percent);
     void callback(String path);
}
