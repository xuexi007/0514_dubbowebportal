# 基于哪个镜像
FROM java:8
# 拷贝文件到容器，也可以直接写成ADD xxxxx.jar /app.jar
COPY 0514_dubbowebportal-0.0.1-SNAPSHOT.jar /app.jar
# 声明需要暴露的端口
EXPOSE 9001
#配置环境变量，接受外部传递参数
#服务器监听端口
ENV PORT=""
# 配置容器启动后执行的命令
ENTRYPOINT ["sh","-c","java $PORT   -jar /app.jar"]