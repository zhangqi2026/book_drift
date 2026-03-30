package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.book_drift.constant.ActivityConstant;
import com.book_drift.service.BookInfoService;
import com.book_drift.service.BookTagService;
import com.book_drift.service.SysUserService;
import com.book_drift.service.UserActivityService;
import com.book_drift.service.UserMedalService;
import com.book_drift.util.QrCodeUtil;
import com.book_drift.vo.BookInfoVO;
import com.book_drift.vo.BookTagVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 书籍信息服务实现类
 * </p>
 */
@Service
@Transactional
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookInfoService {

    @Resource
    private SysUserService sysUserService;
    
    @Resource
    private SysUserMapper sysUserMapper;
    
    @Resource
    private UserMedalService userMedalService;
    
    @Resource
    private UserActivityService userActivityService;
    
    @Resource
    private BookClaimRecordMapper bookClaimRecordMapper;
    
    @Resource
    private BookNoteMapper bookNoteMapper;
    
    @Resource
    private BookNoteLikeMapper bookNoteLikeMapper;

    @Resource
    private BookTagService bookTagService;

    @Override
    public Page<BookInfoVO> pageQuery(Integer pageNum, Integer pageSize, String bookName) {
        return pageQuery(pageNum, pageSize, bookName, null);
    }

    @Override
    public Page<BookInfoVO> pageQueryWithTags(Integer pageNum, Integer pageSize, String bookName, List<Integer> tagIds) {
        // 构建查询条件
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        
        // 书名模糊查询（可选条件）
        if (StringUtils.isNotBlank(bookName)) {
            queryWrapper.like("book_name", bookName);
        }
        
        queryWrapper.orderByDesc("id"); // 按 ID 降序排序
        
        // 如果有标签筛选，先获取符合标签的书籍 ID
        List<Integer> filteredBookIds = null;
        if (tagIds != null && !tagIds.isEmpty()) {
            Page<Integer> bookIdsPage = bookTagService.getBookIdsByTags(tagIds, 1, 10000);
            filteredBookIds = bookIdsPage.getRecords();
            
            if (filteredBookIds.isEmpty()) {
                // 没有符合标签的书籍，返回空结果
                Page<BookInfoVO> emptyPage = new Page<>(pageNum, pageSize, 0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
            
            queryWrapper.in("id", filteredBookIds);
        }
        
        // 执行分页查询
        Page<BookInfo> page = new Page<>(pageNum, pageSize);
        Page<BookInfo> bookPage = this.getBaseMapper().selectPage(page, queryWrapper);
        
        // 获取所有书籍 ID，批量查询标签
        List<Integer> bookIds = bookPage.getRecords().stream()
                .map(BookInfo::getId)
                .collect(Collectors.toList());
        
        // 批量查询每本书的标签
        Map<Integer, List<BookTagVO>> bookTagsMap = bookIds.stream()
                .collect(Collectors.toMap(
                    bookId -> bookId,
                    bookId -> bookTagService.getBookTags(bookId)
                ));
        
        // 将 BookInfo 转换为 BookInfoVO，并填充捐赠人信息和标签
        List<BookInfoVO> voList = bookPage.getRecords().stream()
                .map(book -> {
                    BookInfoVO vo = convertToVOWithDonor(book);
                    vo.setTags(bookTagsMap.get(book.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 创建返回的分页对象
        Page<BookInfoVO> voPage = new Page<>(bookPage.getCurrent(), bookPage.getSize(), bookPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public Page<BookInfoVO> pageQuery(Integer pageNum, Integer pageSize, String bookName, Integer donorId) {
        return pageQuery(pageNum, pageSize, bookName, donorId, null);
    }
    
    @Override
    public Page<BookInfoVO> pageQuery(Integer pageNum, Integer pageSize, String bookName, Integer donorId, Integer bookStatus) {
        // 构建查询条件
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        
        // 书名模糊查询（可选条件）
        if (StringUtils.isNotBlank(bookName)) {
            queryWrapper.like("book_name", bookName);
        }
        
        // 按捐赠人 ID 查询（可选条件）
        if (donorId != null) {
            queryWrapper.eq("donor_id", donorId);
        }
        
        // 按书籍状态查询（可选条件）
        if (bookStatus != null) {
            queryWrapper.eq("book_status", bookStatus);
        }
        
        queryWrapper.orderByDesc("id"); // 按 ID 降序排序
        
        // 执行分页查询
        Page<BookInfo> page = new Page<>(pageNum, pageSize);
        Page<BookInfo> bookPage = this.getBaseMapper().selectPage(page, queryWrapper);
        
        // 获取所有书籍 ID，批量查询标签
        List<Integer> bookIds = bookPage.getRecords().stream()
                .map(BookInfo::getId)
                .collect(Collectors.toList());
        
        // 批量查询每本书的标签
        Map<Integer, List<BookTagVO>> bookTagsMap = bookIds.stream()
                .collect(Collectors.toMap(
                    bookId -> bookId,
                    bookId -> bookTagService.getBookTags(bookId)
                ));
        
        // 将 BookInfo 转换为 BookInfoVO，并填充捐赠人信息和标签
        List<BookInfoVO> voList = bookPage.getRecords().stream()
                .map(book -> {
                    BookInfoVO vo = convertToVOWithDonor(book);
                    vo.setTags(bookTagsMap.get(book.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
        
        // 创建返回的分页对象
        Page<BookInfoVO> voPage = new Page<>(bookPage.getCurrent(), bookPage.getSize(), bookPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public BookInfoVO getById(Integer id) {
        BookInfo bookInfo = super.getById(id);
        if (bookInfo == null) {
            return null;
        }
        return convertToVO(bookInfo);
    }

    @Override
    public Integer saveWithScore(BookInfo bookInfo) {
        if (bookInfo.getBookQrcode() == null || bookInfo.getBookQrcode().isEmpty()) {
            bookInfo.setBookQrcode(QrCodeUtil.generateUniqueQrCode());
        }
        boolean result = super.save(bookInfo);
        Integer score = null;
        
        // 增加用户活跃度（发布书籍）
        if (result && bookInfo.getDonorId() != null) {
            score = userActivityService.addActivity(
                bookInfo.getDonorId(), 
                ActivityConstant.ACTIVITY_TYPE_PUBLISH_BOOK, 
                bookInfo.getId()
            );
        }
        
        // 注意：捐赠勋章不再自动解锁，需要用户在前端手动点击解锁
        // 如果需要在捐赠后检查并解锁勋章，可以取消以下注释
        // if (result && bookInfo.getDonorId() != null) {
        //     checkAndUnlockDonationMedal(bookInfo.getDonorId());
        // }
        
        return score;
    }
    
    @Override
    public boolean save(BookInfo bookInfo) {
        saveWithScore(bookInfo);
        return true;
    }

    @Override
    public boolean update(BookInfo bookInfo) {
        return super.updateById(bookInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Integer id) {
        // 1. 检查书籍是否存在
        BookInfo bookInfo = super.getById(id);
        if (bookInfo == null) {
            throw new RuntimeException("书籍不存在");
        }
        
        // 2. 检查书籍是否正在被借阅（已认领状态）
        if (bookInfo.getBookStatus() == 2) { // 2-已认领
            throw new RuntimeException("该书籍正在被借阅，无法删除");
        }
        
        // 3. 检查是否有未归还的借阅记录
        QueryWrapper<BookClaimRecord> borrowQuery = new QueryWrapper<>();
        borrowQuery.eq("book_id", id).isNull("return_time");
        long unreturnedCount = bookClaimRecordMapper.selectCount(borrowQuery);
        
        if (unreturnedCount > 0) {
            throw new RuntimeException("该书籍还有" + unreturnedCount + "条未归还的借阅记录，无法删除");
        }
        
        // 4. 删除该书籍的所有借阅记录（已归还的）
        QueryWrapper<BookClaimRecord> recordQuery = new QueryWrapper<>();
        recordQuery.eq("book_id", id);
        bookClaimRecordMapper.delete(recordQuery);
        
        // 5. 删除该书籍的所有读书笔记
        QueryWrapper<BookNote> noteQuery = new QueryWrapper<>();
        noteQuery.eq("book_id", id);
        List<Integer> noteIds = bookNoteMapper.selectList(noteQuery)
                .stream().map(BookNote::getId).collect(Collectors.toList());
        bookNoteMapper.delete(noteQuery);
        
        // 6. 删除该书籍的所有笔记点赞记录
        if (!noteIds.isEmpty()) {
            QueryWrapper<BookNoteLike> likeQuery = new QueryWrapper<>();
            likeQuery.in("note_id", noteIds);
            bookNoteLikeMapper.delete(likeQuery);
        }
        
        // 7. 删除书籍
        return super.removeById(id);
    }

    @Override
    public boolean updateHolderAndStatus(Integer bookId, Integer holderId, Date borrowDeadline, Integer status) {
        BookInfo bookInfo = super.getById(bookId);
        if (bookInfo == null) {
            return false;
        }
        
        bookInfo.setCurrentHolderId(holderId);
        bookInfo.setBorrowDeadline(borrowDeadline);
        bookInfo.setBookStatus(status);
        
        return super.updateById(bookInfo);
    }

    /**
     * 将 BookInfo 转换为 BookInfoVO
     * @param bookInfo 书籍实体
     * @return 书籍 VO
     */
    private BookInfoVO convertToVO(BookInfo bookInfo) {
        BookInfoVO vo = new BookInfoVO();
        BeanUtils.copyProperties(bookInfo, vo);
        
        // 格式化捐赠时间
        if (bookInfo.getDonateTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            vo.setDonateTime(sdf.format(bookInfo.getDonateTime()));
        }
        
        return vo;
    }
    
    /**
     * 将 BookInfo 转换为 BookInfoVO，并填充捐赠人信息
     * @param bookInfo 书籍实体
     * @return 书籍 VO（包含捐赠人信息）
     */
    private BookInfoVO convertToVOWithDonor(BookInfo bookInfo) {
        BookInfoVO vo = convertToVO(bookInfo);
        
        // 根据 donorId 查询捐赠人信息
        if (bookInfo.getDonorId() != null) {
            SysUser donor = sysUserMapper.getUserById(bookInfo.getDonorId());
            if (donor != null) {
                vo.setDonorName(donor.getName());
            }
        }
        
        return vo;
    }
    
    /**
     * 检查并解锁捐赠勋章
     * @param donorId 捐赠人 ID
     */
    @Override
    public void checkAndUnlockDonationMedal(Integer donorId) {
        // 查询用户的捐赠书籍总数
        QueryWrapper<BookInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("donor_id", donorId);
        long donationCount = this.getBaseMapper().selectCount(queryWrapper);
        
        // 根据捐赠数量判断可解锁的勋章
        Long medalId = null;
        String medalName = null;
        
        if (donationCount >= 1 && donationCount < 10) {
            medalId = 1L;
            medalName = "初出茅庐";
        } else if (donationCount >= 10 && donationCount < 20) {
            medalId = 2L;
            medalName = "小有成就";
        } else if (donationCount >= 20 && donationCount < 50) {
            medalId = 3L;
            medalName = "书香门第";
        } else if (donationCount >= 50) {
            medalId = 4L;
            medalName = "藏书家";
        }
        
        if (medalId != null) {
            // 检查用户是否已经拥有该勋章
            UserMedal existingMedal = userMedalService.getByUserIdAndMedalId(donorId, medalId.intValue());
            if (existingMedal == null) {
                // 创建新勋章记录
                UserMedal userMedal = new UserMedal();
                userMedal.setUserId(donorId);
                userMedal.setMedalName(medalName);
                userMedal.setMedalType(1); // 1-捐赠类型勋章
                userMedal.setRequiredCount(medalId.intValue()); // 设置所需数量
                userMedal.setUnlockTime(new Date());
                userMedalService.save(userMedal);
            }
        }
    }
    
    @Override
    public BookInfoVO getByQrcode(String bookQrcode) {
        BookInfo bookInfo = this.getBaseMapper().getByQrcode(bookQrcode);
        if (bookInfo == null) {
            return null;
        }
        BookInfoVO vo = convertToVOWithDonor(bookInfo);
        List<BookTagVO> tags = bookTagService.getBookTags(bookInfo.getId());
        vo.setTags(tags);
        return vo;
    }
    
    @Override
    public String generateQrCodeImage(Integer bookId) {
        BookInfo bookInfo = super.getById(bookId);
        if (bookInfo == null) {
            return null;
        }
        return QrCodeUtil.parseQrCodeBase64(bookInfo.getBookQrcode(), 300, 300);
    }
}
