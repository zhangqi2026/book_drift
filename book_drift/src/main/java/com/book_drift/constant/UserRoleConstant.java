package com.book_drift.constant;

/**
 * 用户角色常量
 */
public class UserRoleConstant {

    /**
     * 管理员
     */
    public static final Integer ADMIN = 1;

    /**
     * 普通用户
     */
    public static final Integer USER = 2;

    /**
     * 获取角色名称
     * @param role 角色值
     * @return 角色名称
     */
    public static String getRoleName(Integer role) {
        if (role == null) {
            return "未知";
        }
        if (role.equals(ADMIN)) {
            return "管理员";
        } else if (role.equals(USER)) {
            return "普通用户";
        }
        return "未知";
    }
}
