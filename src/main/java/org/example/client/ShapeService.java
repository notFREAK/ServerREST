package org.example.client;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

import org.example.common.ApiResponse;

public interface ShapeService {
    @POST("/shapes")
    Call<ApiResponse<Void>> addShape(@Body AddShapeRequest request);

    @GET("/shapes")
    Call<ApiResponse<java.util.List<java.util.Map<java.lang.String, java.lang.Object>>>> getAllShapes();

    @GET("/shapes/{id}")
    Call<ApiResponse<ShapeResponse>> getShape(@Path("id") int id);

    @DELETE("/shapes/{id}")
    Call<ApiResponse<Void>> deleteShape(@Path("id") int id);

    class AddShapeRequest {
        private String type;
        private Object data;

        public AddShapeRequest(String type, Object data) {
            this.type = type;
            this.data = data;
        }
        public String getType() { return type; }
        public Object getData() { return data; }
    }

    class ShapeResponse {
        private int id;
        private String type;
        private Object data;
        public int getId() { return id; }
        public String getType() { return type; }
        public Object getData() { return data; }

        @Override
        public String toString() {
            return "ShapeResponse{id=" + id + ", type=" + type + ", data=" + data + "}";
        }
    }
}
