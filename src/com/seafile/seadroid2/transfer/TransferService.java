package com.seafile.seadroid2.transfer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.seafile.seadroid2.account.Account;

import java.util.List;

public class TransferService extends Service implements TransferListener {
    public static final String BROADCAST_ACTION = "com.seafile.seadroid.TX_BROADCAST";
    public static final String BROADCAST_FILE_DOWNLOAD_SUCCESS = "downloaded";
    public static final String BROADCAST_FILE_DOWNLOAD_FAILED = "downloadFailed";
    public static final String BROADCAST_FILE_DOWNLOAD_PROGRESS = "downloadProgress";

    public static final String BROADCAST_FILE_UPLOAD_SUCCESS = "uploaded";
    public static final String BROADCAST_FILE_UPLOAD_FAILED = "uploadFailed";
    public static final String BROADCAST_FILE_UPLOAD_PROGRESS = "uploadProgress";
    public static final String BROADCAST_FILE_UPLOAD_CANCELLED = "uploadCancelled";

    private static final String DEBUG_TAG = "TransferService";

    private final IBinder mBinder = new TransferBinder();
    // private TransferManager txManager;
    private DownloadTaskManager downloadTaskManager;
    private UploadTaskManager uploadTaskManager;

    @Override
    public void onCreate() {
        // txManager = new TransferManager();
        downloadTaskManager = new DownloadTaskManager();
        uploadTaskManager = new UploadTaskManager();
        // txManager.setListener(this);
        downloadTaskManager.setListener(this);
        uploadTaskManager.setListener(this);

    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG_TAG, "onDestroy");
        // txManager.unsetListener();
        downloadTaskManager.unsetListener();
        uploadTaskManager.unsetListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public class TransferBinder extends Binder {
        public TransferService getService() {
            return TransferService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Log.d(DEBUG_TAG, "onBind");
        return mBinder;
    }
    

    // -------------------------- upload task --------------------//
    public int addTaskToUploadQue(Account account, String repoID, String repoName, String dir,
                             String filePath, boolean isUpdate, boolean isCopyToLocal) {
        return uploadTaskManager.addTaskToQue(account, repoID, repoName, dir, filePath, isUpdate, isCopyToLocal);
    }

    /**
     * call this method to handle upload request, like file upload or camera upload.
     *
     * Note: use isCopyToLocal to mark automatic camera upload if true, or file upload if false.
     * @param account
     * @param repoID
     * @param repoName
     * @param dir
     * @param filePath
     * @param isUpdate
     * @param isCopyToLocal
     * @return
     */
    public int addUploadTask(Account account, String repoID, String repoName, String dir,
            String filePath, boolean isUpdate, boolean isCopyToLocal) {
        return addTaskToUploadQue(account, repoID, repoName, dir, filePath, isUpdate, isCopyToLocal);
    }

    /*public boolean isUploading() {
        return uploadTaskManager.isUploading();
    }*/

    public UploadTaskInfo getUploadTaskInfo(int taskID) {
        return (UploadTaskInfo) uploadTaskManager.getTaskInfo(taskID);
    }

    public List<UploadTaskInfo> getAllUploadTaskInfos() {
        return (List<UploadTaskInfo>) uploadTaskManager.getAllTaskInfoList();
    }

    public void removeAllUploadTasksByState(TaskState taskState) {
        uploadTaskManager.removeByState(taskState);

    }

    public void cancelUploadTaskInQue(int taskID) {
        uploadTaskManager.cancel(taskID);
        uploadTaskManager.doNext();
    }

    public void cancelAllUploadTasks() {
        uploadTaskManager.cancelAll();
    }

    public void cancelAllCameraUploadTasks() {
        uploadTaskManager.cancelAllCameraUploadTasks();
    }

    public void retryUploadTask(int taskID) {
        uploadTaskManager.retry(taskID);
    }

    public void removeUploadTask(int taskID) {
        uploadTaskManager.removeInAllTaskList(taskID);
    }

    // -------------------------- download task --------------------//

    public int addDownloadTask(Account account, String repoName, String repoID, String path) {
        return downloadTaskManager.addTask(account, repoName, repoID, path);
    }

    public int addTaskToDownloadQue(Account account, String repoName, String repoID, String path) {
        return downloadTaskManager.addTaskToQue(account, repoName, repoID, path);
    }

    /*public boolean isDownloading() {
        return downloadTaskManager.isDownloading();
    } */

    public List<DownloadTaskInfo> getAllDownloadTaskInfos() {
        return (List<DownloadTaskInfo>) downloadTaskManager.getAllTaskInfoList();
    }

    public List<DownloadTaskInfo> getDownloadTaskInfosByPath(String repoID, String dir) {
        return downloadTaskManager.getTaskInfoListByPath(repoID, dir);
    }

    public void removeDownloadTask(int taskID) {
        downloadTaskManager.removeInAllTaskList(taskID);
    }

    public void removeAllDownloadTasksByState(TaskState taskState) {
        downloadTaskManager.removeByState(taskState);

    }

    public void retryDownloadTask(int taskID) {
        downloadTaskManager.retry(taskID);
    }

    public DownloadTaskInfo getDownloadTaskInfo(int taskID) {
        return (DownloadTaskInfo) downloadTaskManager.getTaskInfo(taskID);
    }

    public void cancelDownloadTask(int taskID) {
        cancelDownloadTaskInQue(taskID);
    }

    public void cancelDownloadTaskInQue(int taskID) {
        downloadTaskManager.cancel(taskID);
        downloadTaskManager.doNext();
    }

    public void cancellAllDownloadTasks() {
        downloadTaskManager.cancelAll();
    }

    // -------------------------- listener method --------------------//
    @Override
    public void onFileUploadProgress(int taskID) {
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_UPLOAD_PROGRESS).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onFileUploaded(int taskID) {
        uploadTaskManager.remove(taskID);
        uploadTaskManager.doNext();
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_UPLOAD_SUCCESS).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onFileUploadCancelled(int taskID) {
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_UPLOAD_CANCELLED).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onFileUploadFailed(int taskID) {
        uploadTaskManager.remove(taskID);
        uploadTaskManager.doNext();
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_UPLOAD_FAILED).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onFileDownloadProgress(int taskID) {
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_DOWNLOAD_PROGRESS).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onFileDownloaded(int taskID) {
        downloadTaskManager.remove(taskID);
        downloadTaskManager.doNext();
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_DOWNLOAD_SUCCESS).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    @Override
    public void onFileDownloadFailed(int taskID) {
        downloadTaskManager.remove(taskID);
        downloadTaskManager.doNext();
        Intent localIntent = new Intent(BROADCAST_ACTION).putExtra("type",
                BROADCAST_FILE_DOWNLOAD_FAILED).putExtra("taskID", taskID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
