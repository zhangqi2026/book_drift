package com.book_drift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.BookClaimRecord;
import com.book_drift.vo.BookClaimRecordVO;

/**
 * <p>
 * 书籍认领记录服务接口
 * </p>
 */
public interface BookClaimRecordService {

    /**
     * 分页查询认领记录（支持用户 ID 条件）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param userId 用户 ID（可选）
     * @return 分页结果
     */
    Page<BookClaimRecordVO> pageQuery(Integer pageNum, Integer pageSize, Integer userId);

    /**
     * 根据 ID 查询认领记录
     * @param id 记录 ID
     * @return 认领记录信息
     */
    BookClaimRecordVO getById(Integer id);

    /**
     * 保存认领记录
     * @param bookClaimRecord 认领记录
     * @return 是否成功
     */
    boolean save(BookClaimRecord bookClaimRecord);

    /**
     * 更新认领记录
     * @param bookClaimRecord 认领记录
     * @return 是否成功
     */
    boolean update(BookClaimRecord bookClaimRecord);

    /**
     * 删除认领记录
     * @param id 记录 ID
     * @return 是否成功
     */
    boolean delete(Integer id);

    /**
     * 查询用户当前未归还的书籍数量（用于限制每人最多 3 本）
     * @param userId 用户 ID
     * @return 当前借阅数量
     */
    Integer countCurrentBorrowed(Integer userId);

    /**
     * 分页查询书籍的历史借阅记录（按书籍 ID）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param bookId 书籍 ID
     * @return 分页结果
     */
    Page<BookClaimRecordVO> pageQueryByBookId(Integer pageNum, Integer pageSize, Integer bookId);

    /**
     * 借书（认领书籍）
     * @param bookId 书籍 ID
     * @param userId 用户 ID
     * @return 是否成功
     */
    boolean claimBook(Integer bookId, Integer userId);
    
    /**
     * 借书（认领书籍）并返回积分信息
     * @param bookId 书籍 ID
     * @param userId 用户 ID
     * @return 增加的积分（认领人的积分）
     */
    Integer claimBookWithScore(Integer bookId, Integer userId);

    /**
     * 还书（归还书籍）
     * @param recordId 借书记录 ID
     * @return 是否成功
     */
    boolean returnBook(Integer recordId);
    
    /**
     * 还书（归还书籍）并返回积分信息
     * @param recordId 借书记录 ID
     * @return 增加的积分
     */
    Integer returnBookWithScore(Integer recordId);
}
