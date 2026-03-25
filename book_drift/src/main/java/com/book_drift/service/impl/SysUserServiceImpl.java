package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.constant.UserRoleConstant;
import com.book_drift.domain.BookClaimRecord;
import com.book_drift.domain.BookInfo;
import com.book_drift.domain.BookNote;
import com.book_drift.domain.BookNoteLike;
import com.book_drift.domain.SysUser;
import com.book_drift.domain.UserMedal;
import com.book_drift.mapper.BookClaimRecordMapper;
import com.book_drift.mapper.BookInfoMapper;
import com.book_drift.mapper.BookNoteLikeMapper;
import com.book_drift.mapper.BookNoteMapper;
import com.book_drift.mapper.SysUserMapper;
import com.book_drift.mapper.UserMedalMapper;
import com.book_drift.service.SysUserService;
import com.book_drift.service.UserMedalService;
import com.book_drift.vo.SysUserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统用户服务实现类
 * </p>
 */
@Service
@Transactional
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private UserMedalService userMedalService;
    
    @Resource
    private BookClaimRecordMapper bookClaimRecordMapper;
    
    @Resource
    private BookInfoMapper bookInfoMapper;
    
    @Resource
    private UserMedalMapper userMedalMapper;
    
    @Resource
    private BookNoteMapper bookNoteMapper;
    
    @Resource
    private BookNoteLikeMapper bookNoteLikeMapper;

    @Override
    public Page<SysUserVO> pageQuery(Integer pageNum, Integer pageSize, String name) {
        // 构建查询条件
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        
        // 支持按姓名或学号模糊查询（可选条件）
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.and(wrapper -> 
                wrapper.like("name", name)
                       .or()
                       .like("student_id", name)
            );
        }
        
        queryWrapper.orderByDesc("id"); // 按 ID 降序排序
        
        // 执行分页查询
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> userPage = this.getBaseMapper().selectPage(page, queryWrapper);
        
        // 将 SysUser 转换为 SysUserVO
        List<SysUserVO> voList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 创建返回的分页对象
        Page<SysUserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public SysUserVO getById(Integer id) {
        SysUser sysUser = super.getById(id);
        if (sysUser == null) {
            return null;
        }
        return convertToVO(sysUser);
    }

    @Override
    public boolean save(SysUser sysUser) {
        return super.save(sysUser);
    }

    @Override
    public boolean update(SysUser sysUser) {
        return super.updateById(sysUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        // 1. 检查用户是否有未归还的书籍 (book_claim_record 表中 return_time 为 NULL)
        QueryWrapper<BookClaimRecord> borrowQuery = new QueryWrapper<>();
        borrowQuery.eq("user_id", id).isNull("return_time");
        long unreturnedCount = bookClaimRecordMapper.selectCount(borrowQuery);
        
        if (unreturnedCount > 0) {
            throw new RuntimeException("该用户还有" + unreturnedCount + "本未归还的书籍，无法删除");
        }
        
        // 2. 检查用户是否有待认领的书籍 (book_info 表中 book_status=1 或 2 且 current_holder_id=id)
        QueryWrapper<BookInfo> holdingQuery = new QueryWrapper<>();
        holdingQuery.eq("current_holder_id", id).in("book_status", 1, 2);
        long holdingCount = bookInfoMapper.selectCount(holdingQuery);
        
        if (holdingCount > 0) {
            throw new RuntimeException("该用户还有" + holdingCount + "本待认领或已认领的书籍，无法删除");
        }
        
        // 3. 删除该用户的读书笔记
        QueryWrapper<BookNote> noteQuery = new QueryWrapper<>();
        noteQuery.eq("user_id", id);
        List<Integer> noteIds = bookNoteMapper.selectList(noteQuery)
                .stream().map(BookNote::getId).collect(Collectors.toList());
        bookNoteMapper.delete(noteQuery);
        
        // 4. 删除该用户作为点赞者的记录
        QueryWrapper<BookNoteLike> userLikeQuery = new QueryWrapper<>();
        userLikeQuery.eq("user_id", id);
        bookNoteLikeMapper.delete(userLikeQuery);
        
        // 5. 删除该用户的笔记对应的点赞记录
        if (!noteIds.isEmpty()) {
            QueryWrapper<BookNoteLike> noteLikeQuery = new QueryWrapper<>();
            noteLikeQuery.in("note_id", noteIds);
            bookNoteLikeMapper.delete(noteLikeQuery);
        }
        
        // 6. 删除该用户的借阅记录（已归还的）
        QueryWrapper<BookClaimRecord> recordQuery = new QueryWrapper<>();
        recordQuery.eq("user_id", id);
        bookClaimRecordMapper.delete(recordQuery);
        
        // 7. 删除该用户的勋章记录
        QueryWrapper<UserMedal> medalQuery = new QueryWrapper<>();
        medalQuery.eq("user_id", id);
        userMedalMapper.delete(medalQuery);
        
        // 8. 将该用户捐赠的书籍的 donor_id 置为 NULL（保留书籍记录）
        QueryWrapper<BookInfo> donateBookQuery = new QueryWrapper<>();
        donateBookQuery.eq("donor_id", id);
        List<BookInfo> donateBooks = bookInfoMapper.selectList(donateBookQuery);
        for (BookInfo book : donateBooks) {
            book.setDonorId(null);
            bookInfoMapper.updateById(book);
        }
        
        // 9. 删除用户
        return super.removeById(id);
    }

    @Override
    public void increaseBorrowCount(Integer userId) {
        SysUser sysUser = super.getById(userId);
        if (sysUser != null) {
            // 初始化次数（如果为 null）
            if (sysUser.getBorrowCount() == null) {
                sysUser.setBorrowCount(0);
            }
            // 增加借书次数
            sysUser.setBorrowCount(sysUser.getBorrowCount() + 1);
            this.update(sysUser);
            
            // 注意：借阅勋章不再自动解锁，需要用户在前端手动点击解锁
            // 如果需要在增加借阅次数后检查并解锁勋章，可以取消以下注释
            // checkAndUnlockMedal(userId, sysUser.getBorrowCount());
        }
    }

    @Override
    public void checkAndUnlockMedal(Integer userId, Integer borrowCount) {
        // 根据借书次数判断可解锁的勋章
        Long medalId = null;
        String medalName = null;
        
        if (borrowCount >= 1 && borrowCount < 10) {
            medalId = 1L;
            medalName = "借阅新手";
        } else if (borrowCount >= 10 && borrowCount < 30) {
            medalId = 2L;
            medalName = "借阅达人";
        } else if (borrowCount >= 30 && borrowCount < 50) {
            medalId = 3L;
            medalName = "阅读之星";
        } else if (borrowCount >= 50 && borrowCount < 100) {
            medalId = 4L;
            medalName = "书香使者";
        } else if (borrowCount >= 100) {
            medalId = 5L;
            medalName = "图书博士";
        }
        
        if (medalId != null) {
            // 检查用户是否已经拥有该勋章
            UserMedal existingMedal = userMedalService.getByUserIdAndMedalId(userId, medalId.intValue());
            if (existingMedal == null) {
                // 创建新勋章记录
                UserMedal userMedal = new UserMedal();
                userMedal.setUserId(userId);
                userMedal.setMedalName(medalName);
                userMedal.setMedalType(2); // 2-借阅类型勋章
                userMedal.setRequiredCount(medalId.intValue()); // 设置所需数量
                userMedal.setUnlockTime(new Date());
                userMedalService.save(userMedal);
            }
        }
    }

    /**
     * 将 SysUser 转换为 SysUserVO
     * @param sysUser 用户实体
     * @return 用户 VO
     */
    private SysUserVO convertToVO(SysUser sysUser) {
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(sysUser, vo);
        return vo;
    }

    @Override
    public boolean isAdmin(Integer userId) {
        SysUser sysUser = super.getById(userId);
        return sysUser != null && UserRoleConstant.ADMIN.equals(sysUser.getRole());
    }

    @Override
    public void checkRole(Integer userId, Integer requiredRole) {
        SysUser sysUser = super.getById(userId);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 管理员拥有所有权限
        if (UserRoleConstant.ADMIN.equals(sysUser.getRole())) {
            return;
        }
        
        // 普通用户只能访问普通用户权限
        if (!UserRoleConstant.USER.equals(sysUser.getRole()) && !UserRoleConstant.USER.equals(requiredRole)) {
            throw new RuntimeException("权限不足，需要管理员权限");
        }
    }
}
