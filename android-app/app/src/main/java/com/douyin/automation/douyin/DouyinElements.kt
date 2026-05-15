package com.douyin.automation.douyin

/**
 * 抖音界面元素定义
 */
object DouyinElements {
    
    // ========== 主界面 ==========
    const val SEARCH_BUTTON_TEXT = "搜索"
    const val SEARCH_ICON_DESC = "搜索"
    
    // ========== 搜索界面 ==========
    const val SEARCH_INPUT_HINT = "搜索"
    const val SEARCH_EDIT_TEXT = "android.widget.EditText"
    
    // ========== 搜索结果 ==========
    const val RESULT_LIST_CLASS = "androidx.recyclerview.widget.RecyclerView"
    const val USER_ITEM_CLASS = "android.view.ViewGroup"
    
    // ========== 博主主页 ==========
    const val PROFILE_TAB_WORKS = "作品"
    const val PROFILE_TAB_LIKE = "喜欢"
    const val PROFILE_TAB_DYNAMIC = "动态"
    
    // ========== 通用元素 ==========
    const val BUTTON_CLASS = "android.widget.Button"
    const val TEXT_VIEW_CLASS = "android.widget.TextView"
    const val IMAGE_VIEW_CLASS = "android.widget.ImageView"
    const val EDIT_TEXT_CLASS = "android.widget.EditText"
    
    // ========== 界面标识 ==========
    object Screen {
        const val MAIN = "MainActivity"
        const val SEARCH = "SearchActivity"
        const val PROFILE = "ProfileActivity"
        const val VIDEO = "VideoActivity"
    }
}
