package com.seafile.seadroid2.transfer;

import android.app.ActivityManager;
import android.os.AsyncTask;
import android.util.Log;

import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.ProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Upload blocks task
 */
public class UploadBlocksTask extends TransferTask {
    public static final String DEBUG_TAG = UploadBlocksTask.class.getSimpleName();

    private int version;
    private String dir;   // parent dir
    private boolean isUpdate;  // true if update an existing file
    private boolean isCopyToLocal; // false to turn off copy operation
    private UploadStateListener uploadStateListener;

    private DataManager dataManager;

    public UploadBlocksTask(int taskID, Account account, String repoID, String repoName,
                      String dir, String filePath, boolean isUpdate, boolean isCopyToLocal, int version,
                      UploadStateListener uploadStateListener) {
        super(taskID, account, repoName, repoID, filePath);
        this.dir = dir;
        this.isUpdate = isUpdate;
        this.version = version;
        this.isCopyToLocal = isCopyToLocal;
        this.uploadStateListener = uploadStateListener;

        this.totalSize = new File(filePath).length();
        this.finished = 0;

        this.dataManager = new DataManager(account);
    }

    public UploadTaskInfo getTaskInfo() {
        UploadTaskInfo info = new UploadTaskInfo(account, taskID, state, repoID,
                repoName, dir, path, isUpdate, isCopyToLocal,
                finished, totalSize, err);
        return info;
    }

    public void cancelUpload() {
        if (state != TaskState.INIT && state != TaskState.TRANSFERRING) {
            return;
        }
        state = TaskState.CANCELLED;
        super.cancel(true);
    }

    @Override
    protected void onPreExecute() {
        state = TaskState.TRANSFERRING;
    }

    @Override
    protected void onProgressUpdate(Long... values) {
        long uploaded = values[0];
        Log.d(DEBUG_TAG, "Uploaded " + uploaded);
        this.finished = uploaded;
        uploadStateListener.onFileUploadProgress(taskID);
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            ProgressMonitor monitor = new ProgressMonitor() {
                @Override
                public void onProgressNotify(long uploaded) {
                    publishProgress(uploaded);
                }

                @Override
                public boolean isCancelled() {
                    return UploadBlocksTask.this.isCancelled();
                }
            };

            Log.d(DEBUG_TAG, "Upload path: " + path);
            dataManager.uploadByBlocks(repoName, repoID, dir, path, monitor, isCopyToLocal, version, isUpdate);
        } catch (SeafException e) {
            Log.d(DEBUG_TAG, "Upload exception " + e.getCode() + " " + e.getMessage());
            err = e;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            err = SeafException.unknownException;
        } catch (IOException e) {
            e.printStackTrace();
            err = SeafException.networkException;
        }

        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        state = err == null ? TaskState.FINISHED : TaskState.FAILED;
        if (uploadStateListener != null) {
            if (err == null) {
                uploadStateListener.onFileUploaded(taskID);
            }
            else {
                uploadStateListener.onFileUploadFailed(taskID);
            }
        }
    }

    @Override
    protected void onCancelled() {
        if (uploadStateListener != null) {
            uploadStateListener.onFileUploadCancelled(taskID);
        }
        uploadStateListener.onFileUploadCancelled(taskID);
    }

    public String getDir() {
        return dir;
    }

    public boolean isCopyToLocal() {
        return isCopyToLocal;
    }

    public boolean isUpdate() {
        return isUpdate;
    }
}
