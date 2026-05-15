"""
APK下载服务
提供HTTP接口供App下载最新APK
"""
from fastapi import FastAPI
from fastapi.responses import FileResponse, JSONResponse
from pathlib import Path
import os

# APK存储目录
APK_DIR = Path("/personal/ai_workspace/aip/apk-releases")
LATEST_APK = APK_DIR / "latest.apk"

@app.get("/api/apk/latest")
async def get_latest_apk_info():
    """获取最新APK信息"""
    if not LATEST_APK.exists():
        return JSONResponse(
            status_code=404,
            content={"error": "APK not found"}
        )
    
    # 获取文件信息
    stat = LATEST_APK.stat()
    real_path = LATEST_APK.resolve()
    
    return {
        "version": "latest",
        "filename": real_path.name,
        "size": stat.st_size,
        "size_mb": round(stat.st_size / 1024 / 1024, 2),
        "modified": stat.st_mtime,
        "download_url": "/api/apk/download"
    }

@app.get("/api/apk/download")
async def download_latest_apk():
    """下载最新APK"""
    if not LATEST_APK.exists():
        return JSONResponse(
            status_code=404,
            content={"error": "APK not found"}
        )
    
    real_path = LATEST_APK.resolve()
    
    return FileResponse(
        path=str(real_path),
        media_type="application/vnd.android.package-archive",
        filename="app-debug.apk",
        headers={
            "Content-Disposition": "attachment; filename=app-debug.apk"
        }
    )

@app.get("/api/apk/list")
async def list_apk_versions():
    """列出所有APK版本"""
    if not APK_DIR.exists():
        return {"versions": []}
    
    versions = []
    for apk_file in sorted(APK_DIR.glob("app-debug-*.apk"), reverse=True):
        stat = apk_file.stat()
        versions.append({
            "filename": apk_file.name,
            "size": stat.st_size,
            "size_mb": round(stat.st_size / 1024 / 1024, 2),
            "modified": stat.st_mtime
        })
    
    return {"versions": versions}
