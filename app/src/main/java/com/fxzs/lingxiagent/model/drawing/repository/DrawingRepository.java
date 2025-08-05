package com.fxzs.lingxiagent.model.drawing.repository;

import androidx.lifecycle.LiveData;

import com.fxzs.lingxiagent.model.drawing.api.GenerateImageRequest;
import com.fxzs.lingxiagent.model.drawing.api.PageResult;
import com.fxzs.lingxiagent.model.drawing.api.SampleListRequest;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingImageDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSessionDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingStyleDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingSampleDto;
import com.fxzs.lingxiagent.model.drawing.dto.DrawingCategoryDto;

import java.util.List;
import java.util.Map;

/**
 * AI绘画仓库接口
 */
public interface DrawingRepository {
    
    /**
     * 创建图片会话
     */
    LiveData<Result<Long>> createImageSession(String sessionName);
    
    /**
     * 获取风格列表
     */
    LiveData<Result<List<DrawingStyleDto>>> getStyles();
    
    /**
     * 异步生成图片
     */
    LiveData<Result<DrawingImageDto>> generateImage(GenerateImageRequest request);
    
    /**
     * 同步生成图片
     */
    LiveData<Result<DrawingImageDto>> generateImageSync(GenerateImageRequest request);
    
    /**
     * 查询图片详情
     */
    LiveData<Result<DrawingImageDto>> getImageDetail(DrawingImageDto imageDto);
    
    /**
     * 获取我的会话列表
     */
    LiveData<Result<PageResult<DrawingSessionDto>>> getMySessions(Map<String, Object> params);
    
    /**
     * 获取示例列表
     */
    LiveData<Result<PageResult<DrawingSampleDto>>> getSampleList(SampleListRequest request);
    
    /**
     * 更新会话
     */
    LiveData<Result<String>> updateSession(DrawingSessionDto sessionDto);
    
    /**
     * 获取我的绘图分页
     */
    LiveData<Result<PageResult<DrawingImageDto>>> getMyImages(Map<String, Object> params);
    
    /**
     * 删除绘画
     */
    LiveData<Result<Void>> deleteImage(Long imageId);
    
    /**
     * 清空会话历史
     */
    LiveData<Result<Void>> clearSession(Long sessionId);
    
    /**
     * 删除所有会话
     */
    LiveData<Result<Void>> deleteAllSessions();
    
    /**
     * 获取图片分类列表（示例图片）
     */
    LiveData<Result<List<DrawingSampleDto>>> getImageCatList();
    
    /**
     * 获取分类列表
     */
    LiveData<Result<List<DrawingCategoryDto>>> getCategoryList();
    
    /**
     * 获取会话列表（新接口）
     */
    LiveData<Result<PageResult<DrawingImageDto>>> getSessionList(Map<String, Object> params);
    
    /**
     * 根据会话ID获取会话详情
     */
    LiveData<Result<DrawingSessionDto>> getSessionDetailById(Long sessionId);
    /**
     * 根据会话ID获取会话详情
     */
    LiveData<Result<String>> deleteAllSessions(Long sessionId);
    
    /**
     * 结果封装类
     */
    class Result<T> {
        private final T data;
        private final String error;
        private final boolean success;
        
        public Result(T data, String error, boolean success) {
            this.data = data;
            this.error = error;
            this.success = success;
        }
        
        public static <T> Result<T> success(T data) {
            return new Result<>(data, null, true);
        }
        
        public static <T> Result<T> error(String error) {
            return new Result<>(null, error, false);
        }
        
        public T getData() {
            return data;
        }
        
        public String getError() {
            return error;
        }
        
        public boolean isSuccess() {
            return success;
        }
    }
}