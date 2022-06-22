package com.sanenchen.accountbook.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.room.Room
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.sanenchen.accountbook.components.PasswordBookComponent
import com.sanenchen.accountbook.databases.AppDatabase
import com.sanenchen.accountbook.ui.theme.AccountBookTheme
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.*
import kotlinx.coroutines.launch

/**
 * 主界面
 * @author sanenchen
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 再次初始化数据库，防止长时间挂后台，导致闪退
        // 在此之外切切不可初始化，否则将会导致数据监听失效
        AppDatabase.database =
            Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").allowMainThreadQueries().build()
        setContent {
            AccountBookTheme {
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text("Password") }, elevation = 0.dp, actions = {
                            IconButton(onClick = {
                                val intent = Intent(this@MainActivity, PasswordEditActivity::class.java)
                                intent.putExtra("title", "")
                                intent.putExtra("user", "")
                                intent.putExtra("password", "")
                                intent.putExtra("remark", "")
                                intent.putExtra("groupName", "")
                                startActivity(intent)
                            }) { // 添加按钮
                                Icon(imageVector = Icons.Filled.Add, contentDescription = "新建")
                            }
                            IconButton(onClick = { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) }) { // 设置按钮
                                Icon(imageVector = Icons.Filled.Settings, contentDescription = "设置")
                            }
                        })
                }) {
                    Column(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize()
                    ) {
                        AllowedScreenShot()
                        RowIndex()
                    }
                }
            }
        }
    }


    /**
     * TabBar
     */
    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun RowIndex() {
        // 获取组别
        val groupList = AppDatabase.database.PasswordGroupDao().queryAll().collectAsState(listOf()).value

        val state = rememberPagerState(0)
        val scope = rememberCoroutineScope()
        ScrollableTabRow(
            modifier = Modifier
                .shadow(2.dp)
                .zIndex(1f),
            selectedTabIndex = state.currentPage,
            backgroundColor = OrbitTheme.colors.surface.main,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.currentPage]),
                    color = OrbitTheme.colors.primary.normal
                )
            },
            divider = {}
        ) {
            Tab(selected = state.currentPage == 0, onClick = { scope.launch { state.animateScrollToPage(0) } }, text = { Text("全部") })
            for (index in groupList.indices)
                Tab(
                    selected = state.currentPage == index + 1,
                    onClick = { scope.launch { state.animateScrollToPage(index + 1) } },
                    text = { Text(groupList[index].groupName) })
        }

        HorizontalPager(count = groupList.size + 1, state = state, modifier = Modifier.fillMaxSize()) { tabIndex ->
            Box(
                Modifier.fillMaxSize()
            ) {
                if (tabIndex == 0)
                    PasswordBookComponent(-1, this@MainActivity)
                else
                    PasswordBookComponent(groupList[tabIndex - 1].id, this@MainActivity)
            }
        }
    }

    /**
     * 判断是否可以截图
     */
    @Composable
    fun AllowedScreenShot() {
        for (item in AppDatabase.database.SettingInformationDao().queryAllFlow().collectAsState(initial = listOf()).value)
            if (!item.allowScreenShot)
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) // 禁止截图
            else
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) // 允许截图
    }
}