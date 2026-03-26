package com.book_drift.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.book_drift.domain.BookTag;
import com.book_drift.vo.BookTagVO;

import java.util.List;

/**
 * <p>
 * 书籍标签服务接口
 * </p>
 */
public interface BookTagService {

    /**
     * 分页查询标签列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param tagName 标签名称（模糊查询）
     * @return 分页结果
     */
    Page<BookTagVO> pageQuery(Integer pageNum, Integer pageSize, String tagName);

    /**
     * 获取所有标签
     * @return 标签列表
     */
    List<BookTagVO> getAllTags();

    /**
     * 根据 ID 查询标签
     * @param id 标签 ID
     * @return 标签信息
     */
    BookTagVO getById(Integer id);

    /**
     * 保存标签
     * @param bookTag 标签信息
     * @return 是否成功
     */
    boolean save(BookTag bookTag);

    /**
     * 更新标签
     * @param bookTag 标签信息
     * @return 是否成功
     */
    boolean update(BookTag bookTag);

    /**
     * 删除标签
     * @param id 标签 ID
     * @return 是否成功
     */
    boolean delete(Integer id);

    /**
     * 给书籍绑定标签
     * @param bookId 书籍 ID
     * @param tagIds 标签 ID 列表
     * @return 是否成功
     */
    boolean bindTagsToBook(Integer bookId, List<Integer> tagIds);

    /**
     * 获取书籍的标签列表
     * @param bookId 书籍 ID
     * @return 标签列表
     */
    List<BookTagVO> getBookTags(Integer bookId);

    /**
     * 根据标签 ID 列表筛选书籍
     * @param tagIds 标签 ID 列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Integer> getBookIdsByTags(List<Integer> tagIds, Integer pageNum, Integer pageSize);
}
