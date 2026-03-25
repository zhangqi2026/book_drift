package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.domain.BookClaimRecord;
import com.book_drift.domain.BookInfo;
import com.book_drift.domain.SysUser;
import com.book_drift.mapper.BookClaimRecordMapper;
import com.book_drift.mapper.BookInfoMapper;
import com.book_drift.service.BookClaimRecordService;
import com.book_drift.service.BookInfoService;
import com.book_drift.service.SysUserService;
import com.book_drift.vo.BookClaimRecordVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 书籍认领记录服务实现类
 * </p>
 */
@Service
@Transactional
public class BookClaimRecordServiceImpl extends ServiceImpl<BookClaimRecordMapper, BookClaimRecord> implements BookClaimRecordService {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private BookInfoService bookInfoService;
    
    @Resource
    private BookInfoMapper bookInfoMapper;

    @Override
    public Page<BookClaimRecordVO> pageQuery(Integer pageNum, Integer pageSize, Integer userId) {
        // 构建查询条件
        QueryWrapper<BookClaimRecord> queryWrapper = new QueryWrapper<>();
        
        // 按用户 ID 筛选（可选条件）
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        
        queryWrapper.orderByDesc("id"); // 按 ID 降序排序
        
        // 执行分页查询并填充书籍和用户信息
        return executePageQuery(pageNum, pageSize, queryWrapper, 1);
    }

    @Override
    public BookClaimRecordVO getById(Integer id) {
        BookClaimRecord bookClaimRecord = super.getById(id);
        if (bookClaimRecord == null) {
            return null;
        }
        return convertToVO(bookClaimRecord);
    }

    @Override
    public boolean save(BookClaimRecord bookClaimRecord) {
        boolean result = super.save(bookClaimRecord);
        // 保存借书记录后，增加用户的借书次数
        if (result && bookClaimRecord.getUserId() != null) {
            sysUserService.increaseBorrowCount(bookClaimRecord.getUserId());
        }
        return result;
    }

    @Override
    public boolean update(BookClaimRecord bookClaimRecord) {
        return super.updateById(bookClaimRecord);
    }

    @Override
    public boolean delete(Integer id) {
        return super.removeById(id);
    }

    @Override
    public Integer countCurrentBorrowed(Integer userId) {
        QueryWrapper<BookClaimRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                    .isNull("return_time"); // 未归还的记录
        return this.getBaseMapper().selectCount(queryWrapper);
    }

    @Override
    public Page<BookClaimRecordVO> pageQueryByBookId(Integer pageNum, Integer pageSize, Integer bookId) {
        QueryWrapper<BookClaimRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("book_id", bookId)
                    .orderByDesc("claim_time"); // 按认领时间降序
        
        // 执行分页查询，仅填充用户信息（需要显示认领人）
        return executePageQuery(pageNum, pageSize, queryWrapper, 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean claimBook(Integer bookId, Integer userId) {
        // 1. 检查用户当前借阅数量是否超过 3 本
        Integer currentBorrowed = countCurrentBorrowed(userId);
        if (currentBorrowed >= 3) {
            throw new RuntimeException("每人最多只能借阅 3 本书，您当前已借阅 " + currentBorrowed + " 本");
        }
        
        // 2. 检查书籍状态（1-待认领 或 3-已归还）
        BookInfo bookInfo = bookInfoMapper.selectById(bookId);
        if (bookInfo == null) {
            throw new RuntimeException("书籍不存在");
        }
        Integer status = bookInfo.getBookStatus();
        if (status != 1 && status != 3) {
            throw new RuntimeException("书籍当前不可被认领（状态：" + status + "）");
        }
        
        // 3. 检查是否为捐赠者本人认领（不允许）
        if (bookInfo.getDonorId().equals(userId)) {
            throw new RuntimeException("不能认领自己捐赠的书籍");
        }
        
        // 4. 创建借书记录
        BookClaimRecord record = new BookClaimRecord();
        record.setBookId(bookId);
        record.setUserId(userId);
        record.setClaimTime(new Date());
        
        // 计算 7 天后的到期时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        record.setDueTime(calendar.getTime());
        
        record.setReturnTime(null); // 未归还
        record.setIsOverdue(0); // 未超期
        
        boolean saveResult = super.save(record);
        if (!saveResult) {
            throw new RuntimeException("创建借书记录失败");
        }
        
        // 5. 更新书籍状态为已认领，设置当前持有人
        boolean updateResult = bookInfoService.updateHolderAndStatus(
            bookId, 
            userId, 
            record.getDueTime(), 
            2 // 2-已认领
        );
        
        if (!updateResult) {
            throw new RuntimeException("更新书籍状态失败");
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnBook(Integer recordId) {
        // 1. 查询借书记录
        BookClaimRecord record = super.getById(recordId);
        if (record == null) {
            throw new RuntimeException("借书记录不存在");
        }
        
        // 检查是否已归还
        if (record.getReturnTime() != null) {
            throw new RuntimeException("该书籍已归还");
        }
        
        // 2. 检查是否超期
        Date now = new Date();
        if (now.after(record.getDueTime())) {
            record.setIsOverdue(1); // 标记为超期
        }
        
        // 3. 更新归还时间
        record.setReturnTime(now);
        
        boolean updateResult = super.updateById(record);
        if (!updateResult) {
            throw new RuntimeException("更新借书记录失败");
        }
        
        // 4. 更新书籍状态为已归还（3），清空当前持有人
        boolean bookUpdateResult = bookInfoService.updateHolderAndStatus(
            record.getBookId(), 
            null, 
            null, 
            3 // 3-已归还
        );
        
        if (!bookUpdateResult) {
            throw new RuntimeException("更新书籍状态失败");
        }
        
        return true;
    }

    /**
     * 将 BookClaimRecord 转换为 BookClaimRecordVO
     * @param bookClaimRecord 记录实体
     * @return 记录 VO
     */
    private BookClaimRecordVO convertToVO(BookClaimRecord bookClaimRecord) {
        BookClaimRecordVO vo = new BookClaimRecordVO();
        BeanUtils.copyProperties(bookClaimRecord, vo);
        
        // 创建日期格式化对象
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        // 格式化认领时间
        if (bookClaimRecord.getClaimTime() != null) {
            vo.setClaimTime(sdf.format(bookClaimRecord.getClaimTime()));
        }
        
        // 格式化归还时间
        if (bookClaimRecord.getReturnTime() != null) {
            vo.setReturnTime(sdf.format(bookClaimRecord.getReturnTime()));
        }
        
        // 格式化应归还时间
        if (bookClaimRecord.getDueTime() != null) {
            vo.setDueTime(sdf.format(bookClaimRecord.getDueTime()));
        }
        
        return vo;
    }
    
    /**
     * 将 BookClaimRecord 转换为 BookClaimRecordVO，并填充书籍和用户信息
     * @param bookClaimRecord 记录实体
     * @return 记录 VO（包含书籍和用户信息）
     */
    private BookClaimRecordVO convertToVOWithBookInfo(BookClaimRecord bookClaimRecord) {
        // 先调用基础转换方法
        BookClaimRecordVO vo = convertToVO(bookClaimRecord);
        
        // 根据 bookId 查询书籍信息并填充
        if (bookClaimRecord.getBookId() != null) {
            BookInfo bookInfo = bookInfoMapper.selectById(bookClaimRecord.getBookId());
            if (bookInfo != null) {
                vo.setBookName(bookInfo.getBookName());
                vo.setAuthor(bookInfo.getAuthor());
                vo.setBookStatus(bookInfo.getBookStatus());
                
                // 根据 donorId 查询捐赠人姓名
                if (bookInfo.getDonorId() != null) {
                    SysUser donor = bookInfoMapper.getDonorById(bookInfo.getDonorId());
                    if (donor != null) {
                        vo.setDonorName(donor.getName());
                    }
                }
            }
        }
        
        // 根据 userId 查询认领人姓名
        if (bookClaimRecord.getUserId() != null) {
            SysUser user = bookInfoMapper.getDonorById(bookClaimRecord.getUserId());
            if (user != null) {
                vo.setClaimerName(user.getName());
            }
        }
        
        return vo;
    }
    
    /**
     * 将 BookClaimRecord 转换为 BookClaimRecordVO，仅填充用户信息
     * @param bookClaimRecord 记录实体
     * @return 记录 VO（仅包含用户信息）
     */
    private BookClaimRecordVO convertToVOWithUserInfo(BookClaimRecord bookClaimRecord) {
        // 先调用基础转换方法
        BookClaimRecordVO vo = convertToVO(bookClaimRecord);
        
        // 根据 userId 查询认领人姓名
        if (bookClaimRecord.getUserId() != null) {
            SysUser user = bookInfoMapper.getDonorById(bookClaimRecord.getUserId());
            if (user != null) {
                vo.setClaimerName(user.getName());
            }
        }
        
        return vo;
    }
    
    /**
     * 执行分页查询
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param queryWrapper 查询条件
     * @param fillType 填充类型：0-不填充 1-填充书籍和用户信息 2-仅填充用户信息
     * @return 分页结果
     */
    private Page<BookClaimRecordVO> executePageQuery(Integer pageNum, Integer pageSize, 
                                                      QueryWrapper<BookClaimRecord> queryWrapper, 
                                                      int fillType) {
        // 执行分页查询
        Page<BookClaimRecord> page = new Page<>(pageNum, pageSize);
        Page<BookClaimRecord> recordPage = this.getBaseMapper().selectPage(page, queryWrapper);
        
        // 转换为 VO
        List<BookClaimRecordVO> voList;
        if (fillType == 0) {
            // 不填充额外信息
            voList = recordPage.getRecords().stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
        } else if (fillType == 1) {
            // 填充书籍和用户信息
            voList = recordPage.getRecords().stream()
                    .map(this::convertToVOWithBookInfo)
                    .collect(Collectors.toList());
        } else {
            // 仅填充用户信息
            voList = recordPage.getRecords().stream()
                    .map(this::convertToVOWithUserInfo)
                    .collect(Collectors.toList());
        }
        
        // 创建返回的分页对象
        Page<BookClaimRecordVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }
}
