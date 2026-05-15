#!/usr/bin/env python3
"""
从GitHub Actions下载最新成功构建的APK
"""

import os
import sys
import json
import requests
import zipfile
from pathlib import Path

# 配置
REPO = "mesunrise/aip"
APK_DIR = Path("apk-releases")
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN", "")

def print_color(text, color="green"):
    """彩色输出"""
    colors = {
        "red": "\033[0;31m",
        "green": "\033[0;32m",
        "yellow": "\033[1;33m",
        "blue": "\033[0;34m",
        "nc": "\033[0m"
    }
    print(f"{colors.get(color, '')}{text}{colors['nc']}")

def get_latest_successful_run():
    """获取最新成功的workflow run"""
    url = f"https://api.github.com/repos/{REPO}/actions/runs"
    params = {"status": "success", "per_page": 1}
    headers = {}
    
    if GITHUB_TOKEN:
        headers["Authorization"] = f"token {GITHUB_TOKEN}"
    
    response = requests.get(url, params=params, headers=headers)
    response.raise_for_status()
    
    data = response.json()
    if not data["workflow_runs"]:
        return None
    
    return data["workflow_runs"][0]

def get_artifacts(run_id):
    """获取指定run的artifacts"""
    url = f"https://api.github.com/repos/{REPO}/actions/runs/{run_id}/artifacts"
    headers = {}
    
    if GITHUB_TOKEN:
        headers["Authorization"] = f"token {GITHUB_TOKEN}"
    
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    
    data = response.json()
    return data["artifacts"]

def download_artifact(artifact_id, output_path):
    """下载artifact"""
    url = f"https://api.github.com/repos/{REPO}/actions/artifacts/{artifact_id}/zip"
    headers = {}
    
    if GITHUB_TOKEN:
        headers["Authorization"] = f"token {GITHUB_TOKEN}"
    
    response = requests.get(url, headers=headers, stream=True)
    response.raise_for_status()
    
    with open(output_path, "wb") as f:
        for chunk in response.iter_content(chunk_size=8192):
            f.write(chunk)

def main():
    print("📥 从GitHub Actions下载APK")
    print("=" * 50)
    
    # 检查GitHub Token
    if not GITHUB_TOKEN:
        print_color("⚠️  未设置GITHUB_TOKEN环境变量", "yellow")
        print("\n请设置GitHub Personal Access Token:")
        print("  export GITHUB_TOKEN=your_token_here")
        print("\n或者手动下载：")
        print(f"  https://github.com/{REPO}/actions")
        sys.exit(1)
    
    # 创建APK目录
    APK_DIR.mkdir(exist_ok=True)
    
    try:
        # 获取最新成功的构建
        print("🔍 查找最新成功的构建...")
        run = get_latest_successful_run()
        
        if not run:
            print_color("❌ 未找到成功的构建", "red")
            sys.exit(1)
        
        run_id = run["id"]
        run_number = run["run_number"]
        commit_msg = run["head_commit"]["message"].split("\n")[0]
        
        print_color(f"✅ 找到构建: Run #{run_number} (ID: {run_id})", "green")
        print(f"   Commit: {commit_msg[:60]}")
        
        # 获取artifacts
        print("\n📦 获取artifacts...")
        artifacts = get_artifacts(run_id)
        
        if not artifacts:
            print_color("❌ 未找到artifacts", "red")
            sys.exit(1)
        
        artifact = artifacts[0]
        artifact_id = artifact["id"]
        artifact_name = artifact["name"]
        
        print_color(f"✅ 找到artifact: {artifact_name} (ID: {artifact_id})", "green")
        
        # 下载artifact
        print("\n⬇️  下载artifact...")
        zip_path = APK_DIR / "app-debug.zip"
        download_artifact(artifact_id, zip_path)
        print_color("✅ 下载完成", "green")
        
        # 解压
        print("\n📦 解压APK...")
        with zipfile.ZipFile(zip_path, "r") as zip_ref:
            zip_ref.extractall(APK_DIR)
        
        zip_path.unlink()  # 删除zip文件
        
        # 重命名为latest.apk
        apk_path = APK_DIR / "app-debug.apk"
        latest_path = APK_DIR / "latest.apk"
        
        if apk_path.exists():
            if latest_path.exists():
                latest_path.unlink()
            apk_path.rename(latest_path)
            
            size_mb = latest_path.stat().st_size / 1024 / 1024
            
            print_color(f"✅ APK已保存到: {latest_path}", "green")
            print(f"\n📊 文件信息:")
            print(f"  路径: {latest_path}")
            print(f"  大小: {size_mb:.2f} MB")
            print(f"\n🎉 下载完成！")
        else:
            print_color("❌ 未找到app-debug.apk", "red")
            sys.exit(1)
            
    except requests.exceptions.RequestException as e:
        print_color(f"❌ 网络错误: {e}", "red")
        sys.exit(1)
    except Exception as e:
        print_color(f"❌ 错误: {e}", "red")
        sys.exit(1)

if __name__ == "__main__":
    main()
