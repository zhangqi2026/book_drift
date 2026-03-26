package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.domain.BookTag;
import com.book_drift.domain.BookTagRelation;
import com.book_drift.mapper.BookTagMapper;
import com.book_drift.mapper.BookTagRelationMapper;
import com.book_drift.service.BookTagService;
import com.book_drift.vo.BookTagVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 书籍标签服务实现类
 * </p>
 */
@Service
@Transactional
public class BookTagServiceImpl extends ServiceImpl<BookTagMapper, BookTag> implements BookTagService {

    @Resource
    private BookTagRelationMapper bookTagRelationMapper;

    @Override
    public Page<BookTagVO> pageQuery(Integer pageNum, Integer pageSize, String tagName) {
        QueryWrapper<BookTag> queryWrapper = new QueryWrapper<>();
        
        if (tagName != null && !tagName.isEmpty()) {
            queryWrapper.like("tag_name", tagName);
        }
        
        queryWrapper.orderByDesc("id");
        
        Page<BookTag> page = new Page<>(pageNum, pageSize);
        Page<BookTag> tagPage = this.getBaseMapper().selectPage(page, queryWrapper);
        
        // 获取所有标签 ID
        List<Integer> tagIds = tagPage.getRecords().stream()
                .map(BookTag::getId)
                .collect(Collectors.toList());
        
        // 统计每个标签的书籍数量
        Map<Integer, Long> tagBookCountMap = countBooksByTags(tagIds);
        
        // 转换为 VO
        List<BookTagVO> voList = tagPage.getRecords().stream()
                .map(tag -> {
                    BookTagVO vo = new BookTagVO();
                    BeanUtils.copyProperties(tag, vo);
                    vo.setDescription(tag.getTagDesc());
                    vo.setBookCount(tagBookCountMap.getOrDefault(tag.getId(), 0L).intValue());
                    return vo;
                })
                .collect(Collectors.toList());
        
        Page<BookTagVO> voPage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public List<BookTagVO> getAllTags() {
        QueryWrapper<BookTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        
        List<BookTag> tagList = this.getBaseMapper().selectList(queryWrapper);
        
        // 获取所有标签 ID
        List<Integer> tagIds = tagList.stream()
                .map(BookTag::getId)
                .collect(Collectors.toList());
        
        // 统计每个标签的书籍数量
        Map<Integer, Long> tagBookCountMap = countBooksByTags(tagIds);
        
        return tagList.stream()
                .map(tag -> {
                    BookTagVO vo = new BookTagVO();
                    BeanUtils.copyProperties(tag, vo);
                    vo.setDescription(tag.getTagDesc());
                    vo.setBookCount(tagBookCountMap.getOrDefault(tag.getId(), 0L).intValue());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public BookTagVO getById(Integer id) {
        BookTag tag = this.getBaseMapper().selectById(id);
        if (tag == null) {
            return null;
        }
        
        BookTagVO vo = new BookTagVO();
        BeanUtils.copyProperties(tag, vo);
        vo.setDescription(tag.getTagDesc());
        
        // 统计该标签的书籍数量
        QueryWrapper<BookTagRelation> countWrapper = new QueryWrapper<>();
        countWrapper.eq("tag_id", id);
        long bookCount = bookTagRelationMapper.selectCount(countWrapper);
        vo.setBookCount((int) bookCount);
        
        return vo;
    }

    @Override
    public boolean save(BookTag bookTag) {
        // 如果 description 有值，同步到 tagDesc
        if (bookTag.getDescription() != null && bookTag.getTagDesc() == null) {
            bookTag.setTagDesc(bookTag.getDescription());
        }
        bookTag.setCreateTime(new Date());
        return this.getBaseMapper().insert(bookTag) > 0;
    }

    @Override
    public boolean update(BookTag bookTag) {
        // 如果 description 有值，同步到 tagDesc
        if (bookTag.getDescription() != null) {
            bookTag.setTagDesc(bookTag.getDescription());
        }
        return this.getBaseMapper().updateById(bookTag) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        // 先删除关联关系
        QueryWrapper<BookTagRelation> relationWrapper = new QueryWrapper<>();
        relationWrapper.eq("tag_id", id);
        bookTagRelationMapper.delete(relationWrapper);
        
        // 再删除标签
        return this.getBaseMapper().deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean bindTagsToBook(Integer bookId, List<Integer> tagIds) {
        // 先删除旧的关联关系
        QueryWrapper<BookTagRelation> oldRelationWrapper = new QueryWrapper<>();
        oldRelationWrapper.eq("book_id", bookId);
        bookTagRelationMapper.delete(oldRelationWrapper);
        
        // 添加新的关联关系
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Integer tagId : tagIds) {
                BookTagRelation relation = new BookTagRelation();
                relation.setBookId(bookId);
                relation.setTagId(tagId);
                bookTagRelationMapper.insert(relation);
            }
        }
        
        return true;
    }

    @Override
    public List<BookTagVO> getBookTags(Integer bookId) {
        QueryWrapper<BookTagRelation> relationWrapper = new QueryWrapper<>();
        relationWrapper.eq("book_id", bookId);
        
        List<BookTagRelation> relations = bookTagRelationMapper.selectList(relationWrapper);
        
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Integer> tagIds = relations.stream()
                .map(BookTagRelation::getTagId)
                .collect(Collectors.toList());
        
        QueryWrapper<BookTag> tagWrapper = new QueryWrapper<>();
        tagWrapper.in("id", tagIds);
        
        List<BookTag> tags = this.getBaseMapper().selectList(tagWrapper);
        
        return tags.stream()
                .map(tag -> {
                    BookTagVO vo = new BookTagVO();
                    BeanUtils.copyProperties(tag, vo);
                    vo.setDescription(tag.getTagDesc());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<Integer> getBookIdsByTags(List<Integer> tagIds, Integer pageNum, Integer pageSize) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new Page<>(pageNum, pageSize, 0);
        }
        
        // 先获取所有关联关系
        QueryWrapper<BookTagRelation> relationWrapper = new QueryWrapper<>();
        relationWrapper.in("tag_id", tagIds);
        
        List<BookTagRelation> relations = bookTagRelationMapper.selectList(relationWrapper);
        
        // 统计每个书籍 ID 出现的次数
        Map<Integer, Long> bookIdCountMap = relations.stream()
                .collect(Collectors.groupingBy(BookTagRelation::getBookId, Collectors.counting()));
        
        // 筛选出同时包含所有标签的书籍
        int tagCount = tagIds.size();
        List<Integer> matchingBookIds = bookIdCountMap.entrySet().stream()
                .filter(entry -> entry.getValue().intValue() == tagCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 分页
        int total = matchingBookIds.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        
        List<Integer> pagedBookIds = matchingBookIds.subList(start, end);
        
        Page<Integer> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(pagedBookIds);
        
        return page;
    }

    /**
     * 统计每个标签的书籍数量
     */
    private Map<Integer, Long> countBooksByTags(List<Integer> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new java.util.HashMap<>();
        }
        
        QueryWrapper<BookTagRelation> relationWrapper = new QueryWrapper<>();
        relationWrapper.in("tag_id", tagIds);
        
        List<BookTagRelation> relations = bookTagRelationMapper.selectList(relationWrapper);
        
        return relations.stream()
                .collect(Collectors.groupingBy(BookTagRelation::getTagId, Collectors.counting()));
    }
}
