# 使用华为云镜像（使用已验证可用的标签）
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/openjdk:17

# 设置维护者信息
LABEL maintainer="xiezichuan_372@163.com"

# 设置工作目录
WORKDIR /app

# 复制jar文件到容器中
COPY target/ChatAiDesign-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 运行应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
