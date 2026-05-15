"""
APK自动下载脚本（Python版本）
使用GitHub API下载最新构建的APK
"""
import requests
import os
import json
from pathlib import Path
from datetime import datetime

# 配置
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN", "")  # 从环境变量读取
REPO = "mesunrise/aip"
WORKFLOW_NAME = "build-apk.yml"
DOWNLOAD_DIR = Path("/personal/ai_workspace/aip/apk-releases")
LATEST_FILE = DOWNLOAD_DIR / "latest.apk"

def get_latest_successful_run():
    """获取最新成功的构建"""
    url = f"https://api.github.com/repos/{REPO}/actions/workflows/{WORKFLOW_NAME}/runs"
    params = {
        "status": "completed",
        "per_page": 1
    }
    headers = {}
    if GITHUB_TOKEN:
        headers["Authorization"] = f"token {GITHUB_TOKEN}"
    
    response = requests.get(url, params=params, headers=headers)
    response.raise_for_status()
    
    runs = response.json()["workflow_runs"]
    for run in runs:
        if run["conclusion"] == "success":
            return run
    
    return None

def download_artifact(run_id):
    """下载构建产物"""
    # 获取artifacts列表
    url = f"https://api.github.com/repos/{REPO}/actions/runs/{run_id}/artifacts"
    headers = {}
    if GITHUB_TOKEN:
        headers["Authorization"] = f"token {GITHUB_TOKEN}"
    
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    
    artifacts = response.json()["artifacts"]
    
    # 查找app-debug artifact
    for artifact in artifacts:
        if artifact["name"] == "app-debug":
            # 下载artifact
            download_url = artifact["archive_download_url"]
            
            print(f"📥 下载APK (ID: {artifact['id']})...")
            
            response = requests.get(download_url, headers=headers, stream=True)
            response.raise_for_status()
            
            # 保存为zip文件
            zip_file = DOWNLOAD_DIR / f"app-debug-{run_id}.zip"
            with open(zip_file, 'wb') as f:
                for chunk in response.iter_content(chunk_size=8192):
                    f.write(chunk)
            
            # 解压zip文件
            import zipfile
            with zipfile.ZipFile(zip_file, 'r') as zip_ref:
                zip_ref.extractall(DOWNLOAD_DIR)
            
            # 删除zip文件
            zip_file.unlink()
            
            # 重命名APK
            apk_file = DOWNLOAD_DIR / "app-debug.apk"
            if apk_file.exists():
                timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
                new_name = DOWNLOAD_DIR / f"app-debug-{timestamp}-{run_id}.apk"
                apk_file.rename(new_name)
                
                # 创建latest链接
                if LATEST_FILE.exists() or LATEST_FILE.is_symlink():
                    LATEST_FILE.unlink()
                LATEST_FILE.symlink_to(new_name.name)
                
                print(f"✅ APK已下载: {new_name.name}")
                return new_name
            
            break
    
    return None

def cleanup_old_versions():
    """清理旧版本（保留最近5个）"""
    apk_files = sorted(DOWNLOAD_DIR.glob("app-debug-*.apk"), key=lambda x: x.stat().st_mtime, reverse=True)
    
    for old_file in apk_files[5:]:
        print(f"🧹 删除旧版本: {old_file.name}")
        old_file.unlink()

def main():
    print("🔍 检查最新构建...")
    
    # 创建下载目录
    DOWNLOAD_DIR.mkdir(parents=True, exist_ok=True)
    
    # 获取最新构建
    run = get_latest_successful_run()
    if not run:
        print("❌ 未找到成功的构建")
        return
    
    run_id = run["id"]
    print(f"✅ 找到构建 ID: {run_id}")
    
    # 检查是否已下载
    marker_file = DOWNLOAD_DIR / f".downloaded_{run_id}"
    if marker_file.exists():
        print("ℹ️  此版本已下载")
        return
    
    # 下载APK
    apk_file = download_artifact(run_id)
    
    if apk_file:
        # 标记已下载
        marker_file.touch()
        
        # 清理旧版本
        cleanup_old_versions()
        
        # 清理旧标记
        markers = sorted(DOWNLOAD_DIR.glob(".downloaded_*"), key=lambda x: x.stat().st_mtime, reverse=True)
        for old_marker in markers[5:]:
            old_marker.unlink()
        
        print("✅ 完成！")
    else:
        print("❌ 下载失败")

if __name__ == "__main__":
    main()
