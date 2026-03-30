package com.book_drift.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.book_drift.domain.BookInfo;
import com.book_drift.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 书籍信息表 Mapper 接口
 * </p>
 */
@Mapper
public interface BookInfoMapper extends BaseMapper<BookInfo> {

    /**
     * 根据 ID 查询捐赠人信息
     * @param donorId 捐赠人 ID
     * @return 捐赠人信息
     */
    @Select("SELECT * FROM sys_user WHERE id = #{donorId}")
    SysUser getDonorById(Integer donorId);
    
    /**
     * 根据二维码查询书籍
     * @param bookQrcode 书籍二维码
     * @return 书籍信息
     */
    @Select("SELECT * FROM book_info WHERE book_qrcode = #{bookQrcode}")
    BookInfo getByQrcode(String bookQrcode);
}
