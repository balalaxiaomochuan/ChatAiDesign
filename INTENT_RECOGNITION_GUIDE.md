# 意图识别功能使用指南

## 功能概述

本项目现已集成基于大模型的意图识别功能，能够自动识别用户输入的意图类型，为用户提供更精准的服务体验。

## 核心特性

### 1. 智能意图分类
- **学习相关**: 学习路线规划、技能提升、项目指导
- **求职相关**: 简历优化、面试准备、求职咨询  
- **技术问答**: 代码审查、调试帮助、技术问题解答
- **系统交互**: 问候、反馈、系统查询

### 2. 置信度评估
- 为每个识别结果提供置信度评分(0.0-1.0)
- 可配置最小置信度阈值
- 自动标记低置信度结果需要人工确认

### 3. 上下文感知
- 支持对话历史上下文理解
- 可提供额外的背景信息辅助识别
- 根据上下文动态调整识别策略

### 4. 性能优化
- Redis缓存机制，避免重复计算
- 可配置的缓存过期时间
- 实时统计监控

## API接口说明

### 意图识别专用接口 (`/api/intent`)

#### 1. 单个意图识别
```http
POST /api/intent/recognize
Content-Type: application/json

{
    "prompt": "我想学习Java编程，应该从哪里开始？",
    "memory_id": 1,
    "enable_intent_recognition": true,
    "min_confidence": 0.7,
    "context": "用户是编程初学者"
}
```

**响应示例:**
```json
{
    "code": 200,
    "message": "Success",
    "intent_classification": {
        "intentId": "uuid-string",
        "primaryIntent": "LEARNING_PATH",
        "confidence": 0.95,
        "userInput": "我想学习Java编程，应该从哪里开始？",
        "intentDescription": "学习路线规划",
        "entities": "{}",
        "processedAt": "2024-01-01T12:00:00",
        "needsConfirmation": false,
        "suggestedAction": "检测到学习路线咨询，将为您提供个性化的学习建议"
    },
    "intent_processed": true
}
```

#### 2. 批量意图识别
```http
POST /api/intent/batch-recognize
Content-Type: application/json

["如何学习Spring？", "Java多线程面试题", "帮我检查这段代码"]
```

#### 3. 获取支持的意图类型
```http
GET /api/intent/types
```

#### 4. 获取统计信息
```http
GET /api/intent/statistics
```

#### 5. 清除缓存
```http
DELETE /api/intent/cache
```

### 增强版聊天接口 (`/api/ai`)

#### 带意图识别的流式聊天
```http
POST /api/ai/stream-with-intent
Content-Type: application/json

{
    "prompt": "推荐一个适合新手的Java项目",
    "memory_id": 1,
    "enable_intent_recognition": true,
    "min_confidence": 0.6
}
```

#### 获取意图识别统计
```http
GET /api/ai/intent-stats
```

## 配置参数

在 `application.yml` 中可配置以下参数：

```yaml
intent:
  recognition:
    # 是否启用意图识别功能
    enabled: true
    
    # 缓存配置
    cache:
      enabled: true
      ttl-minutes: 30
      
    # 默认置信度阈值
    default-confidence-threshold: 0.7
    
    # 最大输入长度限制
    max-input-length: 1000
    
    # 日志级别
    log-level: INFO
```

## 使用示例

### Java客户端调用示例

```java
// 创建意图识别请求
IntentPromptReq promptReq = new IntentPromptReq();
promptReq.setPrompt("我想系统学习Spring Boot开发");
promptReq.setMemoryId(1);
promptReq.setEnableIntentRecognition(true);
promptReq.setMinConfidence(0.7);

// 调用意图识别服务
IntentClassification result = intentRecognitionService.recognizeIntent(promptReq);

// 处理识别结果
if (result.getPrimaryIntent() == IntentType.LEARNING_PATH) {
    System.out.println("识别为学习路线咨询");
    System.out.println("置信度: " + result.getConfidence());
    System.out.println("建议动作: " + result.getSuggestedAction());
}
```

### Python客户端调用示例

```python
import requests
import json

# 意图识别API调用
url = "http://localhost:8080/chataidesign/api/intent/recognize"
headers = {"Content-Type": "application/json"}

payload = {
    "prompt": "Java面试经常考哪些多线程问题？",
    "memory_id": 1,
    "enable_intent_recognition": True,
    "min_confidence": 0.7
}

response = requests.post(url, headers=headers, json=payload)
result = response.json()

print(f"识别意图: {result['intent_classification']['primary_intent']}")
print(f"置信度: {result['intent_classification']['confidence']}")
```

## 意图类型详解

| 意图编码 | 显示名称 | 描述 | 典型用例 |
|---------|---------|------|----------|
| `learning_path` | 学习路线规划 | 用户询问编程学习路径、学习计划 | "如何学习Python？" |
| `project_guidance` | 项目指导 | 用户寻求项目开发建议、项目选型 | "推荐几个练手项目" |
| `technical_question` | 技术问题 | 用户提出具体的技术问题 | "Spring Boot启动流程？" |
| `interview_preparation` | 面试准备 | 用户准备面试相关问题 | "Java多线程面试题" |
| `resume_optimization` | 简历优化 | 用户需要简历修改建议 | "帮我优化技术简历" |
| `greeting` | 问候 | 用户打招呼、寒暄 | "你好" |
| `other` | 其他 | 无法明确分类的意图 | 各种其他问题 |

## 性能监控

通过 `/api/intent/statistics` 接口可以获取实时性能统计：

```
意图识别统计 - 总请求数: 156, 缓存命中: 89, 模型调用: 67, 缓存命中率: 57.05%
```

## 故障排除

### 常见问题

1. **意图识别结果不准确**
   - 检查输入文本是否足够清晰
   - 提供更多上下文信息
   - 调整置信度阈值

2. **响应速度慢**
   - 检查缓存是否正常工作
   - 查看网络连接状态
   - 确认大模型API可用性

3. **缓存不生效**
   - 检查Redis连接状态
   - 确认缓存配置正确
   - 查看日志中的缓存相关信息

### 日志查看

```bash
# 查看意图识别相关日志
grep "IntentRecognition" logs/application.log

# 查看缓存命中情况
grep "cache hit" logs/application.log
```

## 扩展开发

### 添加新的意图类型

1. 在 `IntentType.java` 枚举中添加新的意图类型
2. 更新意图识别提示模板中的意图描述
3. 在 `EnhancedAiChatServiceImpl` 中添加对应的处理逻辑

### 自定义处理逻辑

```java
@Service
public class CustomIntentHandler {
    
    public String handleLearningPathIntent(IntentClassification intent, String message) {
        // 自定义学习路线处理逻辑
        return "为您定制的学习路线...";
    }
}
```

## 注意事项

1. 意图识别依赖大模型API，需要确保API密钥有效
2. 建议开启缓存以提升性能和降低API调用成本
3. 对于置信度较低的结果，建议进行人工复核
4. 定期清理缓存避免内存占用过高
5. 监控统计信息及时发现性能问题

---
*本文档最后更新时间: 2024年*