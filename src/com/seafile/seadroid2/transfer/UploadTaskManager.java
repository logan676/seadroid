package com.seafile.seadroid2.transfer;

import com.seafile.seadroid2.account.Account;

import java.util.List;

/**
 * Upload task manager
 * <p/>
 * Created by Logan on 15/2/4.
 */
public class UploadTaskManager extends TransferManager {
    private static final String DEBUG_TAG = "UploadTaskManager";

    public int addTaskToQue(Account account, String repoID, String repoName, String dir, String filePath, boolean isUpdate, boolean isCopyToLocal) {
        UploadTask task = new UploadTask(++notificationID, account, repoID, repoName, dir, filePath, isUpdate, isCopyToLocal, listener);
        super.addTaskToQue(task);
        return notificationID;
    }

    public void cancelAllCameraUploadTasks() {
        List<UploadTaskInfo> uploadTaskInfos = (List<UploadTaskInfo>) getAllTaskInfoList();
        for (UploadTaskInfo uploadTaskInfo : uploadTaskInfos) {
            // use isCopyToLocal as a flag to mark a camera photo upload task if false
            // mark a file upload task if true
            if (!uploadTaskInfo.isCopyToLocal) {
                cancel(uploadTaskInfo.taskID);
            }
        }
    }

    public void retry(int taskID) {
        UploadTask task = (UploadTask) getTask(taskID);
        if (task == null || !task.retryUpload())
            return;
        // remove in all task list
        removeInAllTaskList(taskID);

        // create a new one to avoid IllegalStateException
        addTaskToQue(task.getAccount(), task.getRepoID(), task.getRepoName(), task.getDir(), task.getPath(), task.isUpdate(), task.isCopyToLocal());
    }

}
