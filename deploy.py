#!/usr/bin/env python3
"""
远程服务器部署脚本
使用paramiko库进行SSH连接和文件传输
"""
import paramiko
import os
import time

# 服务器配置
SERVER = "wdzd1450232.bohrium.tech"
PORT = 22
USERNAME = "root"
PASSWORD = "DgTcFf6z9cv0uUFB"
REMOTE_DIR = "/root/aip"

def deploy():
    """部署到远程服务器"""
    print("=" * 50)
    print("部署到远程服务器")
    print(f"服务器: {SERVER}")
    print("=" * 50)
    print()
    
    # 创建SSH客户端
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    
    try:
        # 连接服务器
        print("🔗 连接服务器...")
        ssh.connect(SERVER, PORT, USERNAME, PASSWORD)
        print("✅ 连接成功")
        print()
        
        # 上传文件
        print("📤 上传文件...")
        sftp = ssh.open_sftp()
        sftp.put("aip-deploy.tar.gz", "/root/aip-deploy.tar.gz")
        sftp.close()
        print("✅ 上传完成")
        print()
        
        # 解压并部署
        print("📦 解压文件...")
        commands = [
            "cd /root",
            "rm -rf aip",
            "mkdir -p aip",
            "tar -xzf aip-deploy.tar.gz -C aip/",
            "cd aip",
        ]
        
        for cmd in commands:
            stdin, stdout, stderr = ssh.exec_command(cmd)
            stdout.channel.recv_exit_status()
        
        print("✅ 解压完成")
        print()
        
        # 安装依赖
        print("📦 安装Python依赖...")
        stdin, stdout, stderr = ssh.exec_command(
            "cd /root/aip && pip3 install fastapi uvicorn websockets -q"
        )
        stdout.channel.recv_exit_status()
        print("✅ 依赖安装完成")
        print()
        
        # 停止旧服务器
        print("🛑 停止旧服务器...")
        ssh.exec_command("pkill -f 'python.*main.py'")
        time.sleep(2)
        print("✅ 旧服务器已停止")
        print()
        
        # 启动新服务器
        print("🚀 启动服务器...")
        ssh.exec_command(
            "cd /root/aip/cloud-server && nohup python3 src/main.py > server.log 2>&1 &"
        )
        time.sleep(3)
        print("✅ 服务器已启动")
        print()
        
        # 测试连接
        print("🧪 测试本地连接...")
        stdin, stdout, stderr = ssh.exec_command("curl -s http://127.0.0.1:8080/")
        result = stdout.read().decode()
        
        if "running" in result:
            print("✅ 本地连接成功!")
            print(f"响应: {result[:100]}...")
        else:
            print("❌ 本地连接失败")
            print(f"响应: {result}")
        
        print()
        print("=" * 50)
        print("部署完成")
        print("=" * 50)
        print()
        print("📱 App连接地址:")
        print(f"   ws://{SERVER}:8080/ws")
        print()
        print("🧪 测试外网连接:")
        print(f"   curl http://{SERVER}:8080/")
        print()
        
    except Exception as e:
        print(f"❌ 部署失败: {e}")
    finally:
        ssh.close()

if __name__ == "__main__":
    # 检查文件是否存在
    if not os.path.exists("aip-deploy.tar.gz"):
        print("❌ 找不到 aip-deploy.tar.gz")
        print("请先运行: tar --exclude='*.pyc' --exclude='__pycache__' -czf aip-deploy.tar.gz cloud-server/ test_*.py diagnose_connection.sh")
        exit(1)
    
    # 检查paramiko
    try:
        import paramiko
    except ImportError:
        print("❌ 需要安装paramiko")
        print("运行: pip3 install paramiko")
        exit(1)
    
    deploy()
