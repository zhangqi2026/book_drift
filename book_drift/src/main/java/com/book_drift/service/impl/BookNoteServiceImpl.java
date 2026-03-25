package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.domain.BookNote;
import com.book_drift.domain.BookNoteLike;
import com.book_drift.mapper.BookNoteLikeMapper;
import com.book_drift.mapper.BookNoteMapper;
import com.book_drift.service.BookNoteService;
import com.book_drift.service.SysUserService;
import com.book_drift.vo.BookNoteVO;
import com.book_drift.vo.SysUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 读书笔记服务实现类
 * </p>
 */
@Service
@Transactional
public class BookNoteServiceImpl extends ServiceImpl<BookNoteMapper, BookNote> implements BookNoteService {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private BookNoteLikeMapper bookNoteLikeMapper;

    @Override
    public Page<BookNoteVO> pageQuery(Integer pageNum, Integer pageSize, Integer bookId) {
        return pageQuery(pageNum, pageSize, bookId, null);
    }

    public Page<BookNoteVO> pageQuery(Integer pageNum, Integer pageSize, Integer bookId, Integer currentUserId) {
        // 构建查询条件
        QueryWrapper<BookNote> queryWrapper = new QueryWrapper<>();
        
        // 按书籍 ID 筛选（可选条件）
        if (bookId != null) {
            queryWrapper.eq("book_id", bookId);
        }
        
        queryWrapper.orderByDesc("id"); // 按 ID 降序排序
        
        // 执行分页查询
        Page<BookNote> page = new Page<>(pageNum, pageSize);
        Page<BookNote> notePage = this.getBaseMapper().selectPage(page, queryWrapper);
        
        // 将 BookNote 转换为 BookNoteVO
        List<BookNoteVO> voList = notePage.getRecords().stream()
                .map(note -> convertToVO(note, currentUserId))
                .collect(Collectors.toList());
        
        // 创建返回的分页对象
        Page<BookNoteVO> voPage = new Page<>(notePage.getCurrent(), notePage.getSize(), notePage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public BookNoteVO getById(Integer id) {
        return getByIdWithUserCheck(id, null);
    }

    public BookNoteVO getByIdWithUserCheck(Integer id, Integer currentUserId) {
        BookNote bookNote = super.getById(id);
        if (bookNote == null) {
            return null;
        }
        return convertToVO(bookNote, currentUserId);
    }

    @Override
    public boolean save(BookNote bookNote) {
        // 设置创建时间为当前时间
        bookNote.setCreateTime(new Date());
        return super.save(bookNote);
    }

    @Override
    public boolean update(BookNote bookNote) {
        return super.updateById(bookNote);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        // 1. 查询笔记是否存在
        BookNote bookNote = super.getById(id);
        if (bookNote == null) {
            throw new RuntimeException("笔记不存在");
        }
        
        // 2. 删除该笔记的所有点赞记录
        QueryWrapper<BookNoteLike> likeQuery = new QueryWrapper<>();
        likeQuery.eq("note_id", id);
        bookNoteLikeMapper.delete(likeQuery);
        
        // 3. 删除笔记
        return super.removeById(id);
    }

    @Override
    public boolean likeNote(Integer id) {
        // 查询笔记
        BookNote bookNote = super.getById(id);
        if (bookNote == null) {
            return false;
        }
        
        // 点赞数 +1
        Integer likeCount = bookNote.getLikeCount();
        if (likeCount == null) {
            likeCount = 0;
        }
        bookNote.setLikeCount(likeCount + 1);
        
        // 更新笔记
        return super.updateById(bookNote);
    }

    /**
     * 检查用户是否已点赞
     * @param noteId 笔记 ID
     * @param userId 用户 ID
     * @return 是否已点赞
     */
    public boolean hasUserLiked(Integer noteId, Integer userId) {
        if (noteId == null || userId == null) {
            return false;
        }
        
        QueryWrapper<BookNoteLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", noteId)
                    .eq("user_id", userId);
        
        BookNoteLike like = bookNoteLikeMapper.selectOne(queryWrapper);
        return like != null;
    }

    /**
     * 用户点赞（带记录）
     * @param noteId 笔记 ID
     * @param userId 用户 ID
     * @return 是否成功
     */
    public boolean likeNoteWithRecord(Integer noteId, Integer userId) {
        // 检查是否已点赞
        if (hasUserLiked(noteId, userId)) {
            return false; // 已经点过赞了
        }
        
        // 查询笔记
        BookNote bookNote = super.getById(noteId);
        if (bookNote == null) {
            return false;
        }
        
        // 点赞数 +1
        Integer likeCount = bookNote.getLikeCount();
        if (likeCount == null) {
            likeCount = 0;
        }
        bookNote.setLikeCount(likeCount + 1);
        
        // 更新笔记
        boolean updateResult = super.updateById(bookNote);
        
        // 添加点赞记录
        if (updateResult) {
            BookNoteLike like = new BookNoteLike();
            like.setNoteId(noteId);
            like.setUserId(userId);
            like.setCreateTime(new Date());
            bookNoteLikeMapper.insert(like);
        }
        
        return updateResult;
    }

    @Override
    public boolean unlikeNote(Integer id) {
        // 查询笔记
        BookNote bookNote = super.getById(id);
        if (bookNote == null) {
            return false;
        }
        
        // 点赞数 -1，最小为 0
        Integer likeCount = bookNote.getLikeCount();
        if (likeCount == null || likeCount <= 0) {
            likeCount = 0;
        } else {
            bookNote.setLikeCount(likeCount - 1);
        }
        
        // 更新笔记
        return super.updateById(bookNote);
    }

    /**
     * 用户取消点赞（带记录）
     * @param noteId 笔记 ID
     * @param userId 用户 ID
     * @return 是否成功
     */
    public boolean unlikeNoteWithRecord(Integer noteId, Integer userId) {
        // 检查是否已点赞
        if (!hasUserLiked(noteId, userId)) {
            return false; // 没有点过赞，无法取消
        }
        
        // 查询笔记
        BookNote bookNote = super.getById(noteId);
        if (bookNote == null) {
            return false;
        }
        
        // 点赞数 -1，最小为 0
        Integer likeCount = bookNote.getLikeCount();
        if (likeCount == null || likeCount <= 0) {
            likeCount = 0;
        } else {
            bookNote.setLikeCount(likeCount - 1);
        }
        
        // 更新笔记
        boolean updateResult = super.updateById(bookNote);
        
        // 删除点赞记录
        if (updateResult) {
            QueryWrapper<BookNoteLike> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("note_id", noteId)
                        .eq("user_id", userId);
            bookNoteLikeMapper.delete(queryWrapper);
        }
        
        return updateResult;
    }

    /**
     * 将 BookNote 转换为 BookNoteVO
     * @param bookNote 笔记实体
     * @param currentUserId 当前登录用户 ID（可选）
     * @return 笔记 VO
     */
    private BookNoteVO convertToVO(BookNote bookNote, Integer currentUserId) {
        BookNoteVO vo = new BookNoteVO();
        BeanUtils.copyProperties(bookNote, vo);
        
        // 查询用户信息并设置用户名
        if (bookNote.getUserId() != null) {
            SysUserVO sysUserVO = sysUserService.getById(bookNote.getUserId());
            if (sysUserVO != null) {
                vo.setUserName(sysUserVO.getName());
            }
        }
        
        // 格式化创建时间
        if (bookNote.getCreateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            vo.setCreateTime(sdf.format(bookNote.getCreateTime()));
        }
        
        // 设置当前用户是否已点赞
        if (currentUserId != null) {
            vo.setIsLiked(hasUserLiked(bookNote.getId(), currentUserId));
        } else {
            vo.setIsLiked(false);
        }
        
        return vo;
    }
}
