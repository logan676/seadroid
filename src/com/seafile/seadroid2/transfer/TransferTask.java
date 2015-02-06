package com.seafile.seadroid2.transfer;

import android.os.AsyncTask;
import com.seafile.seadroid2.SeafException;
import com.seafile.seadroid2.account.Account;

import java.io.File;

/**
 * Base class for transferring data
 *
 * Created by Logan on 15/2/5.
 */
public abstract class TransferTask extends AsyncTask<Void, Long, File> {

    protected int taskID;
    protected Account account;
    protected String repoName;
    protected String repoID;
    protected String path;
    protected long totalSize, finished;
    protected TaskState state;
    protected SeafException err;
    protected TransferListener transferStateListener;

    public TransferTask(int taskID, Account account, String repoName, String repoID, String path,
                        TransferListener transferStateListener) {
        this.account = account;
        this.repoName = repoName;
        this.repoID = repoID;
        this.path = path;
        this.state = TaskState.INIT;

        // The size of the file would be known in the first progress update
        this.totalSize = -1;
        this.taskID = taskID;
        this.transferStateListener = transferStateListener;
    }

    public void cancel() {
        if (state != TaskState.INIT && state != TaskState.TRANSFERRING) {
            return;
        }
        state = TaskState.CANCELLED;
        super.cancel(true);
    }

    public boolean retryDownload() {
        return state == TaskState.CANCELLED || state == TaskState.FAILED;
    }

    protected abstract TransferTaskInfo getTaskInfo();

    public int getTaskID() {
        return taskID;
    }

    public TaskState getState() {
        return state;
    }

    public Account getAccount() {
        return account;
    }

    public String getRepoName() {
        return repoName;
    }

    public String getRepoID() {
        return repoID;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getFinished() {
        return finished;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TransferTask))
            return false;
        if (obj == this)
            return true;

        TransferTask transferTask = (TransferTask) obj;
        return transferTask.account.getSignature().equals(account.getSignature())
                && Integer.compare(transferTask.taskID, taskID) == 0
                && transferTask.repoID.equals(repoID)
                && transferTask.path.equals(path);
    }

    @Override
    public String toString() {
        return "email " + account.getEmail() + " server " + account.getServer() + " taskID " + taskID + " repoID " + repoID +
                " repoName " + repoName + " path " + path;
    }

}
