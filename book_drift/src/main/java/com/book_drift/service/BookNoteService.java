package com.book_drift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.BookNote;
import com.book_drift.vo.BookNoteVO;

/**
 * <p>
 * 读书笔记服务接口
 * </p>
 */
public interface BookNoteService {

    /**
     * 分页查询笔记列表（支持书籍 ID 条件）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param bookId 书籍 ID（可选）
     * @return 分页结果
     */
    Page<BookNoteVO> pageQuery(Integer pageNum, Integer pageSize, Integer bookId);

    /**
     * 根据 ID 查询笔记
     * @param id 笔记 ID
     * @return 笔记信息
     */
    BookNoteVO getById(Integer id);

    /**
     * 保存笔记
     * @param bookNote 笔记信息
     * @return 是否成功
     */
    boolean save(BookNote bookNote);
    
    /**
     * 保存笔记并返回积分信息
     * @param bookNote 笔记信息
     * @return 增加的积分
     */
    Integer saveWithScore(BookNote bookNote);

    /**
     * 更新笔记
     * @param bookNote 笔记信息
     * @return 是否成功
     */
    boolean update(BookNote bookNote);

    /**
     * 删除笔记
     * @param id 笔记 ID
     * @return 是否成功
     */
    boolean delete(Integer id);

    /**
     * 点赞笔记
     * @param id 笔记 ID
     * @return 是否成功
     */
    boolean likeNote(Integer id);

    /**
     * 取消点赞笔记
     * @param id 笔记 ID
     * @return 是否成功
     */
    boolean unlikeNote(Integer id);
    
    /**
     * 用户点赞（带记录）并返回积分信息
     * @param noteId 笔记 ID
     * @param userId 用户 ID
     * @return 增加的积分（笔记作者的积分）
     */
    Integer likeNoteWithRecordWithScore(Integer noteId, Integer userId);

    /**
     * 分页查询笔记列表（支持书籍 ID 和当前用户 ID 条件）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param bookId 书籍 ID（可选）
     * @param currentUserId 当前用户 ID（可选，用于标记是否已点赞）
     * @return 分页结果
     */
    Page<BookNoteVO> pageQuery(Integer pageNum, Integer pageSize, Integer bookId, Integer currentUserId);

    /**
     * 根据 ID 查询笔记（带用户点赞检查）
     * @param id 笔记 ID
     * @param currentUserId 当前用户 ID
     * @return 笔记信息
     */
    BookNoteVO getByIdWithUserCheck(Integer id, Integer currentUserId);

    /**
     * 用户点赞（带记录）
     * @param noteId 笔记 ID
     * @param userId 用户 ID
     * @return 是否成功
     */
    boolean likeNoteWithRecord(Integer noteId, Integer userId);

    /**
     * 用户取消点赞（带记录）
     * @param noteId 笔记 ID
     * @param userId 用户 ID
     * @return 是否成功
     */
    boolean unlikeNoteWithRecord(Integer noteId, Integer userId);
    
    /**
     * 用户取消点赞（带记录）并返回分数变化
     * @param noteId 笔记 ID
     * @param userId 用户 ID
     * @return 减少的积分（笔记作者的积分）
     */
    Integer unlikeNoteWithRecordWithScore(Integer noteId, Integer userId);
}
