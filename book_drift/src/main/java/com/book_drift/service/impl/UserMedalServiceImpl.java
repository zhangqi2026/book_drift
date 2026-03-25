package com.book_drift.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.book_drift.domain.SysUser;
import com.book_drift.domain.UserMedal;
import com.book_drift.mapper.SysUserMapper;
import com.book_drift.mapper.UserMedalMapper;
import com.book_drift.service.UserMedalService;
import com.book_drift.vo.UserMedalVO;
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
 * 用户勋章服务实现类
 * </p>
 */
@Service
@Transactional
public class UserMedalServiceImpl extends ServiceImpl<UserMedalMapper, UserMedal> implements UserMedalService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public Page<UserMedalVO> pageQuery(Integer pageNum, Integer pageSize, Integer userId) {
        return pageQuery(pageNum, pageSize, userId, null);
    }

    @Override
    public Page<UserMedalVO> pageQuery(Integer pageNum, Integer pageSize, Integer userId, String medalName) {
        // 构建查询条件
        QueryWrapper<UserMedal> queryWrapper = new QueryWrapper<>();
        
        // 按用户 ID 筛选（可选条件）
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        
        // 按勋章名称模糊查询（可选条件）
        if (medalName != null && !medalName.trim().isEmpty()) {
            queryWrapper.like("medal_name", medalName);
        }
        
        queryWrapper.orderByDesc("id"); // 按 ID 降序排序
        
        // 执行分页查询
        Page<UserMedal> page = new Page<>(pageNum, pageSize);
        Page<UserMedal> medalPage = this.getBaseMapper().selectPage(page, queryWrapper);
        
        // 将 UserMedal 转换为 UserMedalVO
        List<UserMedalVO> voList = medalPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 创建返回的分页对象
        Page<UserMedalVO> voPage = new Page<>(medalPage.getCurrent(), medalPage.getSize(), medalPage.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public UserMedalVO getById(Integer id) {
        UserMedal userMedal = super.getById(id);
        if (userMedal == null) {
            return null;
        }
        return convertToVO(userMedal);
    }

    @Override
    public boolean save(UserMedal userMedal) {
        return super.save(userMedal);
    }

    @Override
    public boolean update(UserMedal userMedal) {
        return super.updateById(userMedal);
    }

    @Override
    public boolean delete(Integer id) {
        return super.removeById(id);
    }

    @Override
    public UserMedal getByUserIdAndMedalId(Integer userId, Integer medalId) {
        QueryWrapper<UserMedal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                    .eq("id", medalId);
        return this.getBaseMapper().selectOne(queryWrapper);
    }

    @Override
    public UserMedal getByUserIdTypeAndCount(Integer userId, Integer medalType, Integer requiredCount) {
        QueryWrapper<UserMedal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                    .eq("medal_type", medalType)
                    .eq("required_count", requiredCount);
        return this.getBaseMapper().selectOne(queryWrapper);
    }

    /**
     * 将 UserMedal 转换为 UserMedalVO
     * @param userMedal 勋章实体
     * @return 勋章 VO
     */
    private UserMedalVO convertToVO(UserMedal userMedal) {
        UserMedalVO vo = new UserMedalVO();
        BeanUtils.copyProperties(userMedal, vo);
        
        // 根据用户 ID 查询用户名
        if (userMedal.getUserId() != null) {
            SysUser user = sysUserMapper.getUserById(userMedal.getUserId());
            if (user != null) {
                vo.setUserName(user.getName());
            }
        }
        
        // 格式化解锁时间
        if (userMedal.getUnlockTime() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            vo.setUnlockTime(sdf.format(userMedal.getUnlockTime()));
        }
        
        return vo;
    }
}
