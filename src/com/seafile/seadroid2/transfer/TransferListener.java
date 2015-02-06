package com.seafile.seadroid2.transfer;

/**
 * Transfer state listener
 *
 * Created by Logan on 15/2/3.
 */
public interface TransferListener {
    void onFileUploadProgress(int taskID);
    void onFileUploaded(int taskID);
    void onFileUploadCancelled(int taskID);
    void onFileUploadFailed(int taskID);

    void onFileDownloadProgress(int taskID);
    void onFileDownloaded(int taskID);
    void onFileDownloadFailed(int taskID);
}
