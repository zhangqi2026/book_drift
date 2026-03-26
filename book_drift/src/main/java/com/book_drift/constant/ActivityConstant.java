package com.book_drift.constant;

/**
 * 活跃度常量类
 */
public class ActivityConstant {
    
    /**
     * 活动类型：发布书籍
     */
    public static final int ACTIVITY_TYPE_PUBLISH_BOOK = 1;
    
    /**
     * 活动类型：成功借出
     */
    public static final int ACTIVITY_TYPE_LEND_BOOK = 2;
    
    /**
     * 活动类型：成功归还/认领
     */
    public static final int ACTIVITY_TYPE_RETURN_CLAIM = 3;
    
    /**
     * 活动类型：发布笔记
     */
    public static final int ACTIVITY_TYPE_PUBLISH_NOTE = 4;
    
    /**
     * 活动类型：被点赞
     */
    public static final int ACTIVITY_TYPE_BE_LIKED = 5;
    
    /**
     * 发布书籍增加的活跃度分数：+10分
     */
    public static final int SCORE_PUBLISH_BOOK = 10;
    
    /**
     * 成功借出增加的活跃度分数：+15分
     */
    public static final int SCORE_LEND_BOOK = 15;
    
    /**
     * 成功归还/认领增加的活跃度分数：+8分
     */
    public static final int SCORE_RETURN_CLAIM = 8;
    
    /**
     * 发布笔记增加的活跃度分数：+5分
     */
    public static final int SCORE_PUBLISH_NOTE = 5;
    
    /**
     * 被点赞增加的活跃度分数：+1分
     */
    public static final int SCORE_BE_LIKED = 1;
    
    /**
     * 排行榜类型：日榜
     */
    public static final String RANK_TYPE_DAILY = "daily";
    
    /**
     * 排行榜类型：周榜
     */
    public static final String RANK_TYPE_WEEKLY = "weekly";
    
    /**
     * 排行榜类型：月榜
     */
    public static final String RANK_TYPE_MONTHLY = "monthly";
    
    /**
     * 排行榜类型：总榜
     */
    public static final String RANK_TYPE_TOTAL = "total";
}
