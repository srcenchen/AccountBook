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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.sanenchen.accountbook.components.PasswordBookComponent
import com.sanenchen.accountbook.databases.AppDatabase
import com.sanenchen.accountbook.ui.theme.AccountBookTheme
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccountBookTheme {
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text("账号本子") }, elevation = 0.dp, actions = {
                            IconButton(onClick = { startActivity(Intent(this@MainActivity, AddPasswordActivity::class.java)) }) { // 添加按钮
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
        val groupList = AppDatabase.database.PasswordGroupDao().queryAll()

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
                    PasswordBookComponent( -1)
                else
                    PasswordBookComponent(groupList[tabIndex - 1].id)
            }
        }
    }

    /**
     * 判断是否可以截图
     */
    @Composable
    fun AllowedScreenShot() {
        if (!AppDatabase.database.SettingInformationDao().queryAll()[0].allowScreenShot)
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) // 禁止截图
        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) // 允许截图
    }
}