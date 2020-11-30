package com.x.selfchat.ui.dashboard;

/**
 *
 * @author mx
 *
 */
public interface ILogger {
    void i(String tag, String log);
    void e(String tag, String log);
    void postError(String error);
}
