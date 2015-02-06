package com.seafile.seadroid2.transfer;

import com.google.common.collect.Lists;
import com.seafile.seadroid2.ConcurrentAsyncTask;
import com.seafile.seadroid2.account.Account;
import com.seafile.seadroid2.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Download task manager
 * <p/>
 * Created by Logan on 15/2/4.
 */
public class DownloadTaskManager extends TransferManager {
    private static final String DEBUG_TAG = "DownloadTaskManager";

    /**
     * Add a new download task.
     * call this method to execute a task instantly.
     */
    public int addTask(Account account, String repoName, String repoID, String path) {
        Iterator<? extends TransferTask> iter = allTaskList.iterator();
        while (iter.hasNext()) {
            TransferTask task = iter.next();
            if (isSameTask(task, repoID, path)) {
                if (task.getState() == TaskState.CANCELLED || task.getState() == TaskState.FAILED
                        || task.getState() == TaskState.FINISHED) {
                    // If there is a duplicate, but it has failed or been
                    // cancelled, remove it first
                    iter.remove();
                    break;
                } else {
                    // A duplicate task is downloading
                    return task.getTaskID();
                }
            }
        }

        TransferTask task = new DownloadTask(++notificationID, account, repoName, repoID, path, listener);
        allTaskList.add(task);
        ConcurrentAsyncTask.execute(task);
        return task.getTaskID();
    }

    public int addTaskToQue(Account account, String repoName, String repoID, String path) {
        DownloadTask downloadTask = new DownloadTask(++notificationID, account, repoName, repoID, path, listener);
        super.addTaskToQue(downloadTask);
        return notificationID;
    }

    /**
     * get all download task info under a specific directory.
     *
     * @param repoID
     * @param dir    valid dir should be something like this "/DIRNAME/", instead of "/DIRNAME",
     *               in order to ensure the param being consistent with its caller
     * @return List<DownloadTaskInfo>
     */
    public List<DownloadTaskInfo> getTaskInfoListByPath(String repoID, String dir) {
        ArrayList<DownloadTaskInfo> infos = Lists.newArrayList();
        for (TransferTask task : allTaskList) {
            if (!task.getRepoID().equals(repoID))
                continue;

            String parentDir = Utils.getParentPath(task.getPath());
            String validDir;

            if (!parentDir.equals("/"))
                validDir = parentDir + "/";
            else
                validDir = parentDir;

            if (validDir.equals(dir))
                infos.add(((DownloadTask) task).getTaskInfo());
        }

        return infos;
    }

    public void retry(int taskID) {
        DownloadTask task = (DownloadTask) getTask(taskID);
        if (task == null || !task.retryDownload())
            return;
        // remove in all task list
        removeInAllTaskList(taskID);

        // create a new one to avoid IllegalStateException
        addTaskToQue(task.getAccount(), task.getRepoName(), task.getRepoID(), task.getPath());
    }

    private boolean isSameTask(TransferTask taskInQue, String... params) {
        if (params == null || params.length != 2)
            return false;

        return taskInQue.getRepoID().equals(params[0])
                && taskInQue.getPath().equals(params[1]);
    }

}
