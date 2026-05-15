#!/usr/bin/env python3
"""
检查远程服务器状态
"""
import paramiko

SERVER = "wdzd1450232.bohrium.tech"
USERNAME = "root"
PASSWORD = "DgTcFf6z9cv0uUFB"

def check_server():
    """检查服务器状态"""
    print("=" * 50)
    print("检查远程服务器状态")
    print("=" * 50)
    print()
    
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    
    try:
        print("🔗 连接服务器...")
        ssh.connect(SERVER, 22, USERNAME, PASSWORD)
        print("✅ SSH连接成功")
        print()
        
        # 检查服务器进程
        print("🔍 检查服务器进程...")
        stdin, stdout, stderr = ssh.exec_command("ps aux | grep 'python.*main.py' | grep -v grep")
        result = stdout.read().decode()
        if result:
            print("✅ 服务器进程运行中:")
            print(result)
        else:
            print("❌ 服务器进程未运行")
        print()
        
        # 检查端口监听
        print("🔍 检查端口8080...")
        stdin, stdout, stderr = ssh.exec_command("netstat -tlnp | grep 8080 || ss -tlnp | grep 8080")
        result = stdout.read().decode()
        if result:
            print("✅ 端口8080正在监听:")
            print(result)
        else:
            print("❌ 端口8080未监听")
        print()
        
        # 测试本地连接
        print("🧪 测试本地HTTP连接...")
        stdin, stdout, stderr = ssh.exec_command("curl -s -m 5 http://127.0.0.1:8080/")
        result = stdout.read().decode()
        if "running" in result:
            print("✅ 本地HTTP连接成功")
            print(f"响应: {result[:100]}")
        else:
            print("❌ 本地HTTP连接失败")
            print(f"响应: {result}")
        print()
        
        # 查看服务器日志
        print("📋 查看服务器日志（最后10行）...")
        stdin, stdout, stderr = ssh.exec_command("tail -10 /root/aip/cloud-server/server.log")
        result = stdout.read().decode()
        if result:
            print(result)
        else:
            print("无日志输出")
        print()
        
        # 检查防火墙
        print("🔍 检查防火墙状态...")
        stdin, stdout, stderr = ssh.exec_command("iptables -L -n 2>/dev/null || ufw status 2>/dev/null || echo '无法查看防火墙'")
        result = stdout.read().decode()
        print(result[:500])
        print()
        
    except Exception as e:
        print(f"❌ 错误: {e}")
    finally:
        ssh.close()

if __name__ == "__main__":
    check_server()
