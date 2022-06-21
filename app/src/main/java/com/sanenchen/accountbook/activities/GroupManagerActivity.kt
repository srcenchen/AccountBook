package com.sanenchen.accountbook.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanenchen.accountbook.databases.AppDatabase
import com.sanenchen.accountbook.databases.PasswordGroup
import com.sanenchen.accountbook.ui.theme.AccountBookTheme
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.IconButton
import kiwi.orbit.compose.ui.controls.Text
import kiwi.orbit.compose.ui.controls.TopAppBar
import kotlinx.coroutines.launch

class GroupManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccountBookTheme {
                val addGroup = remember { mutableStateOf(false) }
                Scaffold(topBar = { TopAppBar(title = { Text("分组管理") }, onNavigateUp = { this.finish() }) }, floatingActionButton = {
                    FloatingActionButton(
                        onClick = { addGroup.value = true },
                        content = { Icon(imageVector = Icons.Filled.Add, contentDescription = "添加分组") }
                    )
                }) {
                    Column(modifier = Modifier.padding(it).background(OrbitTheme.colors.surface.main).fillMaxSize()) {
                        if (addGroup.value)
                            AddGroup(addGroup)
                        MainUI()
                    }
                }
            }
        }
    }

    @Composable
    fun MainUI() {
        // 获取组别
        val groupList = AppDatabase.database.PasswordGroupDao().queryAll().collectAsState(listOf()).value
        for (item in groupList) {
            GroupItem(
                title = item.groupName,
                savedNumOfPassword = AppDatabase.database.PasswordDataDao().queryWithGroup(item.id)
                    .collectAsState(listOf()).value.size.toString(),
                groupID = item.id
            )
        }
    }

    @Composable
    fun GroupItem(title: String, savedNumOfPassword: String, groupID: Int) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp)) {
                    Text(title, fontSize = 20.sp)
                    Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text("已保存至此分组的密码有 $savedNumOfPassword 个", color = OrbitTheme.colors.content.minor) // 账号
                }
            }
            val changeGroup = remember { mutableStateOf(false) }
            if (changeGroup.value)
                ChangeGroupName(changeGroup = changeGroup, groupID = groupID, title)
            IconButton( // 修改
                onClick = {
                    changeGroup.value = true
                }, modifier = Modifier
                    .padding(end = 0.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    tint = OrbitTheme.colors.primary.normal,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton( // 删除
                onClick = {
                    changeGroupID(groupID = groupID)
                    AppDatabase.database.PasswordGroupDao().dropGroup(id = groupID)
                }, modifier = Modifier
                    .padding(end = 16.dp)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = OrbitTheme.colors.critical.normal,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    /**
     * 添加分组
     */
    @Composable
    fun AddGroup(addGroup: MutableState<Boolean>) {
        var groupName by remember { mutableStateOf("") }
        if (addGroup.value)
            AlertDialog(
                onDismissRequest = { addGroup.value = false },
                title = { Text("新建分组") },
                text = {
                    kiwi.orbit.compose.ui.controls.TextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("请输入分组名") },
                        error = if (groupName.isEmpty()) {
                            { Text("分组名不能为空") }
                        } else null
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (groupName.isNotEmpty()) {
                            val passwordGroup = PasswordGroup(0, groupName)
                            AppDatabase.database.PasswordGroupDao().insertPasswordGroup(passwordGroup)
                            addGroup.value = false
                        }
                    }, content = { Text("好了") })
                },
                dismissButton = { TextButton(onClick = {addGroup.value = false}, content = { Text("取消") }) }
            )
    }

    /**
     * 修改分组名
     */
    @Composable
    fun ChangeGroupName(changeGroup: MutableState<Boolean>, groupID: Int, groupNamed: String) {
        var groupName by remember { mutableStateOf(groupNamed) }
        if (changeGroup.value)
            AlertDialog(
                onDismissRequest = { changeGroup.value = false },
                title = { Text("修改分组名") },
                text = {
                    kiwi.orbit.compose.ui.controls.TextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("请输入分组名") },
                        error = if (groupName.isEmpty()) {
                            { Text("分组名不能为空") }
                        } else null
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (groupName.isNotEmpty()) {
                            val passwordGroup = PasswordGroup(groupID, groupName)
                            AppDatabase.database.PasswordGroupDao().update(passwordGroup)
                            changeGroup.value = false
                        }
                    }, content = { Text("好了") })
                },
                dismissButton = { TextButton(onClick = {changeGroup.value = false}, content = { Text("取消") }) }
            )
    }

    /**
     * 将所有被删除的组所属GroupID 改为-1（无分组）
     */
    private fun changeGroupID(groupID: Int) {
        val dataList = AppDatabase.database.PasswordDataDao().queryWithGroupCommon(groupID)
        for (item in dataList) {
            item.groupID = -1
        }
        AppDatabase.database.PasswordDataDao().updateList(dataList)
    }
}