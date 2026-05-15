#!/usr/bin/expect -f

# 自动化SSH部署脚本
set timeout 30
set password "DgTcFf6z9cv0uUFB"
set server "root@wdzd1450232.bohrium.tech"

# 上传文件
spawn scp aip-deploy.tar.gz $server:/root/
expect {
    "password:" {
        send "$password\r"
        exp_continue
    }
    "100%" {
        puts "\n✅ 文件上传成功"
    }
}

# 连接并部署
spawn ssh $server
expect "password:"
send "$password\r"

expect "#"
send "cd /root && rm -rf aip && mkdir -p aip\r"

expect "#"
send "tar -xzf aip-deploy.tar.gz -C aip/\r"

expect "#"
send "cd aip && pip3 install fastapi uvicorn websockets -q\r"

expect "#"
send "cd cloud-server && pkill -f 'python.*main.py' || true\r"

expect "#"
send "nohup python3 src/main.py > server.log 2>&1 &\r"

expect "#"
send "sleep 3 && curl -s http://127.0.0.1:8080/\r"

expect "#"
send "exit\r"

expect eof
