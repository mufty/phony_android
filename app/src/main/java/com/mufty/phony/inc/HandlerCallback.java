package com.mufty.phony.inc;

import com.mufty.phony.gson.Message;

public interface HandlerCallback {
    public void handleIncomingMessage(Message msg);
}
