package com.huawei.chataidesign.entity;

import lombok.Getter;

/**
 * 意图类型枚举
 * 定义系统支持的所有意图类别
 */
@Getter
public enum IntentType {
    
    // 学习相关意图
    LEARNING_PATH("learning_path", "学习路线规划", "用户询问编程学习路径、学习计划"),
    PROJECT_GUIDANCE("project_guidance", "项目指导", "用户寻求项目开发建议、项目选型"),
    SKILL_IMPROVEMENT("skill_improvement", "技能提升", "用户想了解如何提升特定技能"),
    
    // 求职相关意图
    JOB_SEARCH("job_search", "求职咨询", "用户询问求职相关问题"),
    RESUME_OPTIMIZATION("resume_optimization", "简历优化", "用户需要简历修改建议"),
    INTERVIEW_PREPARATION("interview_preparation", "面试准备", "用户准备面试相关问题"),
    
    // 技术问答意图
    TECHNICAL_QUESTION("technical_question", "技术问题", "用户提出具体的技术问题"),
    CODE_REVIEW("code_review", "代码审查", "用户需要代码审查或改进建议"),
    DEBUGGING_HELP("debugging_help", "调试帮助", "用户遇到bug需要帮助解决"),
    
    // 系统操作意图
    SYSTEM_QUERY("system_query", "系统查询", "用户询问系统功能、使用方法"),
    FEEDBACK("feedback", "反馈建议", "用户提供反馈或建议"),
    GREETING("greeting", "问候", "用户打招呼、寒暄"),
    
    // 其他意图
    OTHER("other", "其他", "无法明确分类的意图"),
    UNCLEAR("unclear", "不明确", "意图模糊，需要澄清");
    
    private final String code;
    private final String displayName;
    private final String description;
    
    IntentType(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * 根据编码获取意图类型
     */
    public static IntentType fromCode(String code) {
        for (IntentType intentType : IntentType.values()) {
            if (intentType.getCode().equals(code)) {
                return intentType;
            }
        }
        return OTHER;
    }
    
    /**
     * 获取所有意图类型的描述信息
     */
    public static String getAllIntentDescriptions() {
        StringBuilder sb = new StringBuilder();
        sb.append("支持的意图类型包括：\n");
        for (IntentType intentType : IntentType.values()) {
            sb.append("- ").append(intentType.getCode())
              .append(": ").append(intentType.getDisplayName())
              .append(" (").append(intentType.getDescription()).append(")\n");
        }
        return sb.toString();
    }
}