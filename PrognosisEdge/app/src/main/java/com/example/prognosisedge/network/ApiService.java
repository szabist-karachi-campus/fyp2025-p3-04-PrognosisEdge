package com.example.prognosisedge.network;

import com.example.prognosisedge.models.*;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Login API
    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    // Send OTP API
    @POST("otp/send_otp")
    Call<Void> sendOTP(@Body OtpRequest otpRequest);

    // Verify OTP API
    @POST("otp/verify_otp")
    Call<Void> verifyOTP(@Body OtpRequest otpRequest);

    // Reset Password API
    @POST("reset_password")
    Call<Void> resetPassword(@Body ResetPasswordRequest request);

    // All Machines API
    @GET("machines/get_all_machines")
    Call<AddMachineResponse> getAllMachines();

    // Add Machine API
    @POST("machines/add_machine")
    Call<AddMachineResponse> addMachine(@Body AddMachineRequest request);

    // Edit Machine API
    @PUT("machines/edit_machine")
    Call<AddMachineResponse> editMachine(@Body EditMachineRequest request);

    // Delete Machine API
    @DELETE("machines/delete_machine")
    Call<DeleteMachineResponse> deleteMachine(@retrofit2.http.Query("serial_number") String serialNumber);

    // Fetch machine names by type
    @GET("machines/get_machines_by_type")
    Call<MachineNamesResponse> getMachinesByType(@retrofit2.http.Query("type") String machineType);

    // Schedule Maintenance API
    @POST("task/schedule_maintenance")
    Call<ScheduleMaintenanceResponse> scheduleMaintenance(@Body ScheduleMaintenanceRequest request);

    // Fetch Task Count API
    @GET("task/fetch_task_counts")
    Call<TaskCountsResponse> fetchTaskCounts();

    // Fetch Tasks API
    @GET("task/fetch_tasks_by_status")
    Call<TasksResponse> fetchTasks(@Query("status") String status);

    // Fetch specific task details
    @GET("/api/task/{task_id}")
    Call<TaskDetailResponse> fetchTaskDetails(@Path("task_id") int taskId);

    // Update Work Order API
    @PUT("/api/task/update_task/{task_id}")
    Call<Void> updateWorkOrder(
            @Path("task_id") int taskId,
            @Body UpdateTaskRequest request
    );

    // Fetch Maintenance History API
    @GET("/api/records/fetch_maintenance_history")
    Call<MaintenanceHistoryResponse> fetchMaintenanceHistory(
            @Query("machine") String machine,
            @Query("status") String status,
            @Query("engineer") String engineer
    );

    // Fetch Engineers API
    @GET("/api/records/fetch_engineers")
    Call<EngineersResponse> fetchEngineers();

    // Fetch SS Logs API
    @GET("/api/records/fetch_logs")
    Call<MaintenanceHistoryResponse> fetchMaintenanceLogs(
            @Query("machine") String machine,
            @Query("status") String status,
            @Query("engineer") String engineer
    );

    // Create User
    @POST("/api/admin/create_user")
    Call<CreateUserResponse> createUser(@Body CreateUserRequest request);

    // Get User
    @POST("/api/admin/get_user")
    Call<GetUserResponse> getUser(@Body GetUserRequest request);

    // Update User
    @PUT("/api/admin/update_user")
    Call<UpdateUserResponse> updateUser(@Body UpdateUserRequest request);

    // Deactivate User
    @PUT("/api/admin/deactivate_user")
    Call<DeactivateUserResponse> deactivateUser(@Body DeactivateUserRequest request);

    // Fetch Users
    @GET("/api/admin/get_all_users")
    Call<GetAllUsersResponse> getAllUsers();

    // Supervisor Add or Update Comment
    @POST("/api/records/update_comment")
    Call<UpdateCommentResponse> updateComment(@Body UpdateCommentRequest request);

    //Prediction
    @GET("predict/fetch_all")
    Call<List<PredictionResponse>> getAllPredictions();

    //Reports
    @POST("/api/reports/machines")
    Call<MachineReportResponse> generateMachineReport(@Body MachineReportRequest request);

    @POST("/api/reports/workorders")
    Call<WorkOrderReportResponse> generateWorkOrderReport(@Body WorkOrderReportRequest request);
}
