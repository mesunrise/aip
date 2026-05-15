"""
GitHub Actions构建监控脚本（Python版本）
功能：实时监控最新构建状态，构建完成后自动下载APK
"""
import requests
import time
import sys
from datetime import datetime

REPO = "mesunrise/aip"
CHECK_INTERVAL = 30  # 检查间隔（秒）
GITHUB_TOKEN = ""  # 可选：设置token以访问详细日志

def get_latest_run():
    """获取最新构建信息"""
    url = f"https://api.github.com/repos/{REPO}/actions/runs?per_page=1"
    headers = {}
    if GITHUB_TOKEN:
        headers["Authorization"] = f"token {GITHUB_TOKEN}"
    
    try:
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        runs = response.json()["workflow_runs"]
        return runs[0] if runs else None
    except Exception as e:
        print(f"❌ API请求失败: {e}")
        return None

def monitor_build():
    """监控构建状态"""
    print("🔍 开始监控GitHub Actions构建...")
    print(f"📦 仓库: {REPO}")
    print(f"⏱️  检查间隔: {CHECK_INTERVAL}秒")
    print("")
    
    last_run_id = None
    
    while True:
        run = get_latest_run()
        
        if not run:
            time.sleep(CHECK_INTERVAL)
            continue
        
        run_id = run["id"]
        status = run["status"]
        conclusion = run.get("conclusion", "N/A")
        created_at = run["created_at"]
        html_url = run["html_url"]
        
        # 检测到新构建
        if last_run_id != run_id:
            print(f"\n🆕 检测到新构建: {run_id}")
            last_run_id = run_id
        
        # 显示状态
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        print(f"[{timestamp}]")
        print(f"  Run ID: {run_id}")
        print(f"  Status: {status}")
        print(f"  Conclusion: {conclusion}")
        print(f"  Created: {created_at}")
        print(f"  URL: {html_url}")
        
        # 检查是否完成
        if status == "completed":
            print("")
            if conclusion == "success":
                print("✅ 构建成功！")
                print("📥 下载APK：")
                print(f"   1. 访问: {html_url}")
                print("   2. 滚动到底部 'Artifacts'")
                print("   3. 下载 'app-debug'")
                
                # 如果有token，尝试自动下载
                if GITHUB_TOKEN:
                    print("\n🤖 尝试自动下载APK...")
                    download_apk(run_id)
                
                return 0
            elif conclusion == "failure":
                print("❌ 构建失败！")
                print(f"🔍 查看日志: {html_url}")
                
                # 如果有token，尝试获取错误信息
                if GITHUB_TOKEN:
                    print("\n🔍 获取错误信息...")
                    get_build_errors(run_id)
                
                return 1
            else:
                print(f"⚠️  构建完成但状态异常: {conclusion}")
                return 2
        
        print("  ⏳ 构建进行中...")
        print("")
        
        # 等待下一次检查
        time.sleep(CHECK_INTERVAL)

def download_apk(run_id):
    """下载APK（需要token）"""
    try:
        # 获取artifacts
        url = f"https://api.github.com/repos/{REPO}/actions/runs/{run_id}/artifacts"
        headers = {"Authorization": f"token {GITHUB_TOKEN}"}
        
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        
        artifacts = response.json()["artifacts"]
        
        for artifact in artifacts:
            if artifact["name"] == "app-debug":
                print(f"📦 找到APK artifact: {artifact['name']}")
                print(f"📊 大小: {artifact['size_in_bytes'] / 1024 / 1024:.2f}MB")
                
                # 下载
                download_url = artifact["archive_download_url"]
                print(f"📥 下载中...")
                
                response = requests.get(download_url, headers=headers, stream=True)
                response.raise_for_status()
                
                filename = f"app-debug-{run_id}.zip"
                with open(filename, 'wb') as f:
                    for chunk in response.iter_content(chunk_size=8192):
                        f.write(chunk)
                
                print(f"✅ 下载完成: {filename}")
                print(f"📂 解压: unzip {filename}")
                
                # 自动解压
                import zipfile
                with zipfile.ZipFile(filename, 'r') as zip_ref:
                    zip_ref.extractall(".")
                
                print(f"✅ 解压完成: app-debug.apk")
                return
        
        print("❌ 未找到app-debug artifact")
        
    except Exception as e:
        print(f"❌ 下载失败: {e}")

def get_build_errors(run_id):
    """获取构建错误信息（需要token）"""
    try:
        # 获取jobs
        url = f"https://api.github.com/repos/{REPO}/actions/runs/{run_id}/jobs"
        headers = {"Authorization": f"token {GITHUB_TOKEN}"}
        
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        
        jobs = response.json()["jobs"]
        
        for job in jobs:
            if job.get("conclusion") == "failure":
                print(f"\n❌ 失败的Job: {job['name']}")
                print(f"🔗 日志: {job['html_url']}")
                
                # 查找失败的步骤
                for step in job["steps"]:
                    if step.get("conclusion") == "failure":
                        print(f"  ❌ 失败步骤: {step['name']}")
        
    except Exception as e:
        print(f"❌ 获取错误信息失败: {e}")

if __name__ == "__main__":
    try:
        exit_code = monitor_build()
        sys.exit(exit_code)
    except KeyboardInterrupt:
        print("\n\n⚠️  监控已停止")
        sys.exit(0)
