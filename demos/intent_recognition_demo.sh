#!/bin/bash
# 意图识别功能演示脚本

echo "=== 意图识别功能演示 ==="
echo

# 检查是否可以访问API
API_BASE="http://localhost:8080/chataidesign"

echo "1. 测试API连通性..."
curl -s "${API_BASE}/actuator/health" > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ API服务正常运行"
else
    echo "✗ API服务未运行，请先启动应用"
    echo "  启动命令: ./mvnw spring-boot:run"
    exit 1
fi

echo
echo "2. 获取支持的意图类型..."
curl -s "${API_BASE}/api/intent/types" | jq '.data'

echo
echo "3. 测试学习相关意图识别..."
curl -s -X POST "${API_BASE}/api/intent/recognize" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "我想系统学习Java开发，应该从哪里开始？",
    "memory_id": 1,
    "enable_intent_recognition": true,
    "min_confidence": 0.7
  }' | jq '{
    "intent": .intent_classification.primary_intent,
    "confidence": .intent_classification.confidence,
    "description": .intent_classification.intent_description
  }'

echo
echo "4. 测试技术问题意图识别..."
curl -s -X POST "${API_BASE}/api/intent/recognize" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Spring Boot中的@Autowired注解是如何工作的？",
    "memory_id": 2,
    "enable_intent_recognition": true,
    "min_confidence": 0.7
  }' | jq '{
    "intent": .intent_classification.primary_intent,
    "confidence": .intent_classification.confidence,
    "suggested_action": .intent_classification.suggested_action
  }'

echo
echo "5. 测试面试相关意图识别..."
curl -s -X POST "${API_BASE}/api/intent/recognize" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "Java多线程面试会问哪些问题？",
    "memory_id": 3,
    "enable_intent_recognition": true,
    "min_confidence": 0.7
  }' | jq '{
    "intent": .intent_classification.primary_intent,
    "confidence": .intent_classification.confidence,
    "needs_confirmation": .intent_classification.needs_confirmation
  }'

echo
echo "6. 批量意图识别测试..."
curl -s -X POST "${API_BASE}/api/intent/batch-recognize" \
  -H "Content-Type: application/json" \
  -d '[
    "如何学习Python？",
    "Spring框架有什么特点？", 
    "面试准备建议"
  ]' | jq '.data[].primary_intent'

echo
echo "7. 获取统计信息..."
curl -s "${API_BASE}/api/intent/statistics" | jq '.data'

echo
echo "=== 演示完成 ==="
echo
echo "更多使用方法请参考 INTENT_RECOGNITION_GUIDE.md"