package com.book_drift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.BookInfo;
import com.book_drift.vo.BookInfoVO;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 书籍信息服务接口
 * </p>
 */
public interface BookInfoService {

    /**
     * 分页查询书籍列表（支持书名条件）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param bookName 书名（可选，模糊查询）
     * @return 分页结果
     */
    Page<BookInfoVO> pageQuery(Integer pageNum, Integer pageSize, String bookName);

    /**
     * 分页查询书籍列表（支持书名和捐赠人 ID 条件）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param bookName 书名（可选，模糊查询）
     * @param donorId 捐赠人 ID（可选）
     * @return 分页结果
     */
    Page<BookInfoVO> pageQuery(Integer pageNum, Integer pageSize, String bookName, Integer donorId);
    
    /**
     * 分页查询书籍列表（支持书名、捐赠人 ID 和书籍状态条件）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param bookName 书名（可选，模糊查询）
     * @param donorId 捐赠人 ID（可选）
     * @param bookStatus 书籍状态（可选）
     * @return 分页结果
     */
    Page<BookInfoVO> pageQuery(Integer pageNum, Integer pageSize, String bookName, Integer donorId, Integer bookStatus);

    /**
     * 分页查询书籍列表（支持标签筛选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param bookName 书名（可选，模糊查询）
     * @param tagIds 标签 ID 列表（可选）
     * @return 分页结果
     */
    Page<BookInfoVO> pageQueryWithTags(Integer pageNum, Integer pageSize, String bookName, List<Integer> tagIds);

    /**
     * 根据 ID 查询书籍
     * @param id 书籍 ID
     * @return 书籍信息
     */
    BookInfoVO getById(Integer id);

    /**
     * 保存书籍
     * @param bookInfo 书籍信息
     * @return 是否成功
     */
    boolean save(BookInfo bookInfo);
    
    /**
     * 保存书籍并返回增加的积分
     * @param bookInfo 书籍信息
     * @return 增加的积分
     */
    Integer saveWithScore(BookInfo bookInfo);

    /**
     * 更新书籍
     * @param bookInfo 书籍信息
     * @return 是否成功
     */
    boolean update(BookInfo bookInfo);

    /**
     * 删除书籍
     * @param id 书籍 ID
     * @return 是否成功
     */
    boolean delete(Integer id);

    /**
     * 更新书籍状态和当前持有人（借书时使用）
     * @param bookId 书籍 ID
     * @param holderId 当前持有人 ID
     * @param borrowDeadline 借阅到期时间
     * @param status 书籍状态
     * @return 是否成功
     */
    boolean updateHolderAndStatus(Integer bookId, Integer holderId, Date borrowDeadline, Integer status);
    
    /**
     * 检查并解锁捐赠勋章
     * @param donorId 捐赠人 ID
     */
    void checkAndUnlockDonationMedal(Integer donorId);
    
    /**
     * 通过二维码获取书籍信息
     * @param bookQrcode 书籍二维码
     * @return 书籍信息
     */
    BookInfoVO getByQrcode(String bookQrcode);
    
    /**
     * 生成书籍二维码图片(Base64格式)
     * @param bookId 书籍ID
     * @return Base64编码的二维码图片
     */
    String generateQrCodeImage(Integer bookId);
}
