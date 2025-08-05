package com.fxzs.lingxiagent.model.drawing.api;

import com.fxzs.lingxiagent.model.common.BaseResponse;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingStyleDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSampleDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingCategoryDto;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * AI绘画API接口 - Volc Engine
 */
public interface DrawingApiService {
    
    /**
     * 创建图片会话
     */
    @POST("app-api/lt/ai/image/createImageSession")
    Call<BaseResponse<Long>> createImageSession(@Body CreateImageSessionRequest request);
    
    /**
     * 【Volc】获取风格列表
     */
    @POST("app-api/lt/ai/image/volc/styleList")
    Call<BaseResponse<List<DrawingStyleDto>>> getStyles();
    
    /**
     * 【Volc】生成图片（异步）
     */
    @POST("app-api/lt/ai/image/volc/imagine")
    Call<BaseResponse<Long>> generateImage(@Body GenerateImageRequest request);
    
    /**
     * 【Volc】同步生成图片
     */
    @POST("app-api/lt/ai/image/volc/imagine/wait")
    Call<BaseResponse<Object>> generateImageSync(@Body GenerateImageRequest request);
    
    /**
     * 【Volc】查询图片详情
     */
    @POST("app-api/lt/ai/image/volc/query")
    Call<BaseResponse<DrawingImageDto>> getImageDetail(@Body DrawingImageDto imageDto);
    
    /**
     * 【Volc】获取我的会话列表
     */
    @POST("app-api/lt/ai/image/getSessionList")
    Call<BaseResponse<PageResult<DrawingSessionDto>>> getMySessions(@Body Map<String, Object> params);
    
    /**
     * 根据会话ID获取会话详情
     */
    @GET("app-api/lt/ai/image/getSessionDetailById")
    Call<BaseResponse<DrawingSessionDto>> getSessionDetailById(@Query("id") Long sessionId);
    
    /**
     * 【Volc】根据分类ID示例分页列表
     */
    @POST("app-api/lt/ai/image/volc/sampleList")
    Call<BaseResponse<PageResult<DrawingSampleDto>>> getSampleList(@Body SampleListRequest request);
    
    /**
     * 【Volc】更新会话，根据会话ID
     */
    @POST("app-api/lt/ai/image/updateSession")
    Call<BaseResponse<String>> updateSession(@Body DrawingSessionDto sessionDto);
    
    /**
     * 【Volc】获取我的绘图记录列表
     */
    @GET("app-api/lt/ai/image/my/list")
    Call<BaseResponse<List<DrawingImageDto>>> getMyImageList();
    
    /**
     * 获取我的绘图分页
     */
    @GET("app-api/lt/ai/image/my/page")
    Call<BaseResponse<PageResult<DrawingImageDto>>> getMyImages(@QueryMap Map<String, Object> params);
    
    /**
     * 删除绘画
     */
    @DELETE("app-api/lt/ai/image/delete")
    Call<BaseResponse<Void>> deleteImage(@Query("id") Long imageId);
    
    /**
     * 清空会话历史
     */
    @DELETE("app-api/lt/ai/image/session/clear")
    Call<BaseResponse<Void>> clearSession(@Query("sessionId") Long sessionId);
    
    /**
     * 【Volc】删除所有会话
     */
    @DELETE("app-api/lt/ai/image/volc/session/deleteAll")
    Call<BaseResponse<Void>> deleteAllSessions();
    
    /**
     * 获取图片分类列表（示例图片）
     */
    @POST("app-api/lt/ai/image/getImageCatList")
    Call<BaseResponse<List<DrawingSampleDto>>> getImageCatList();
    
    /**
     * 【Volc】查询绘画分类列表
     */
    @POST("app-api/lt/ai/image/volc/catList")
    Call<BaseResponse<List<DrawingCategoryDto>>> getCategoryList();


    @POST("app-api/lt/ai/image/deleteAllSession")
    Call<BaseResponse<String>> deleteAllSessions(@Body Map<String,Object> map);
}