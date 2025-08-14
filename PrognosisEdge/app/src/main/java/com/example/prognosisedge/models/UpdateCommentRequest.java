package com.example.prognosisedge.models;

public class UpdateCommentRequest {
    private int task_id;
    private String comment;

    public UpdateCommentRequest(int task_id, String comment) {
        this.task_id = task_id;
        this.comment = comment;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
