package com.seafile.seadroid2.transfer;

import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.data.DataManager;
import com.seafile.seadroid2.data.ProgressMonitor;

import java.io.File;

/**
 * Download task
 *
 * Created by Logan on 15/2/3.
 */
public class DownloadTask extends TransferTask {

    private String localPath;

    public DownloadTask(int taskID, Account account, String repoName, String repoID, String path,
                        TransferListener downloadStateListener) {
        super(taskID, account, repoName, repoID, path, downloadStateListener);
    }

    /**
     * When downloading a file, we don't know the file size in advance, so
     * we make use of the first progress update to return the file size.
     */
    @Override
    protected void onProgressUpdate(Long... values) {
        if (totalSize == -1) {
            totalSize = values[0];
            state = TaskState.TRANSFERRING;
            return;
        }
        finished = values[0];
        transferStateListener.onFileDownloadProgress(taskID);
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            DataManager dataManager = new DataManager(account);
            return dataManager.getFile(repoName, repoID, path,
                    new ProgressMonitor() {

                        @Override
                        public void onProgressNotify(long total) {
                            publishProgress(total);
                        }

                        @Override
                        public boolean isCancelled() {
                            return DownloadTask.this.isCancelled();
                        }
                    }
            );
        } catch (SeafException e) {
            err = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(File file) {
        if (transferStateListener != null) {
            if (file != null) {
                state = TaskState.FINISHED;
                localPath = file.getPath();
                transferStateListener.onFileDownloaded(taskID);
            } else {
                state = TaskState.FAILED;
                if (err == null)
                    err = SeafException.unknownException;
                transferStateListener.onFileDownloadFailed(taskID);
            }
        }
    }

    @Override
    protected void onCancelled() {
        state = TaskState.CANCELLED;
    }

    @Override
    public DownloadTaskInfo getTaskInfo() {
        DownloadTaskInfo info = new DownloadTaskInfo(account, taskID, state, repoID,
                repoName, path, localPath, totalSize, finished, err);
        return info;
    }

    public String getPath() {
        return path;
    }

    public String getLocalPath() {
        return localPath;
    }
}