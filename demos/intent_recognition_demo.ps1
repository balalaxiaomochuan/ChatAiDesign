# 意图识别功能演示 (PowerShell版本)

# API基础地址
$API_BASE = "http://localhost:8080/chataidesign"

Write-Host "=== 意图识别功能演示 ===" -ForegroundColor Green
Write-Host ""

# 1. 测试API连通性
Write-Host "1. 测试API连通性..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-WebRequest -Uri "$API_BASE/actuator/health" -Method GET -ErrorAction Stop
    Write-Host "✓ API服务正常运行" -ForegroundColor Green
} catch {
    Write-Host "✗ API服务未运行，请先启动应用" -ForegroundColor Red
    Write-Host "  启动命令: .\mvnw.cmd spring-boot:run" -ForegroundColor Gray
    exit 1
}

Write-Host ""
Write-Host "2. 获取支持的意图类型..." -ForegroundColor Yellow
try {
    $typesResponse = Invoke-RestMethod -Uri "$API_BASE/api/intent/types" -Method GET
    Write-Host $typesResponse.data -ForegroundColor Cyan
} catch {
    Write-Host "获取意图类型失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "3. 测试学习相关意图识别..." -ForegroundColor Yellow
$body = @{
    prompt = "我想系统学习Java开发，应该从哪里开始？"
    memory_id = 1
    enable_intent_recognition = $true
    min_confidence = 0.7
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$API_BASE/api/intent/recognize" -Method POST -Body $body -ContentType "application/json"
    Write-Host "意图类型: $($response.intent_classification.primary_intent)" -ForegroundColor Cyan
    Write-Host "置信度: $($response.intent_classification.confidence)" -ForegroundColor Cyan
    Write-Host "描述: $($response.intent_classification.intent_description)" -ForegroundColor Cyan
} catch {
    Write-Host "意图识别失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "4. 测试技术问题意图识别..." -ForegroundColor Yellow
$body = @{
    prompt = "Spring Boot中的@Autowired注解是如何工作的？"
    memory_id = 2
    enable_intent_recognition = $true
    min_confidence = 0.7
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$API_BASE/api/intent/recognize" -Method POST -Body $body -ContentType "application/json"
    Write-Host "意图类型: $($response.intent_classification.primary_intent)" -ForegroundColor Cyan
    Write-Host "置信度: $($response.intent_classification.confidence)" -ForegroundColor Cyan
    Write-Host "建议动作: $($response.intent_classification.suggested_action)" -ForegroundColor Cyan
} catch {
    Write-Host "意图识别失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "5. 测试面试相关意图识别..." -ForegroundColor Yellow
$body = @{
    prompt = "Java多线程面试会问哪些问题？"
    memory_id = 3
    enable_intent_recognition = $true
    min_confidence = 0.7
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$API_BASE/api/intent/recognize" -Method POST -Body $body -ContentType "application/json"
    Write-Host "意图类型: $($response.intent_classification.primary_intent)" -ForegroundColor Cyan
    Write-Host "置信度: $($response.intent_classification.confidence)" -ForegroundColor Cyan
    Write-Host "需要确认: $($response.intent_classification.needs_confirmation)" -ForegroundColor Cyan
} catch {
    Write-Host "意图识别失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "6. 批量意图识别测试..." -ForegroundColor Yellow
$body = '["如何学习Python？", "Spring框架有什么特点？", "面试准备建议"]'

try {
    $response = Invoke-RestMethod -Uri "$API_BASE/api/intent/batch-recognize" -Method POST -Body $body -ContentType "application/json"
    Write-Host "批量识别结果:" -ForegroundColor Cyan
    $response.data | ForEach-Object {
        Write-Host "  - $($_.primary_intent)" -ForegroundColor Gray
    }
} catch {
    Write-Host "批量识别失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "7. 获取统计信息..." -ForegroundColor Yellow
try {
    $statsResponse = Invoke-RestMethod -Uri "$API_BASE/api/intent/statistics" -Method GET
    Write-Host $statsResponse.data -ForegroundColor Cyan
} catch {
    Write-Host "获取统计信息失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== 演示完成 ===" -ForegroundColor Green
Write-Host ""
Write-Host "更多使用方法请参考 INTENT_RECOGNITION_GUIDE.md" -ForegroundColor Gray