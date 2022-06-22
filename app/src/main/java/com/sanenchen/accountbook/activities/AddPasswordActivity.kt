package com.sanenchen.accountbook.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sanenchen.accountbook.R
import com.sanenchen.accountbook.databases.AppDatabase
import com.sanenchen.accountbook.databases.PasswordData
import com.sanenchen.accountbook.databases.PasswordGroup
import com.sanenchen.accountbook.ui.theme.AccountBookTheme
import kiwi.orbit.compose.ui.controls.*

/**
 * 添加密码
 * @author sanenchen
 */
class AddPasswordActivity : ComponentActivity() {
    private lateinit var trySave: MutableState<Boolean>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccountBookTheme {
                trySave = remember { mutableStateOf(false) }
                val title = remember { mutableStateOf("") }
                val user = remember { mutableStateOf("") }
                val password = remember { mutableStateOf("") }
                val remark = remember { mutableStateOf("") }
                // 分组
                val group = AppDatabase.database.PasswordGroupDao().queryAll()
                val groupList = arrayListOf<PasswordGroup>()
                groupList.add(PasswordGroup(-1, "无分组"))
                for (item in group.collectAsState(listOf()).value) groupList.add(item)
                val selected = remember { mutableStateOf(groupList[0]) }
                Scaffold(topBar = {
                    TopAppBar(elevation = 0.dp, title = { Text("编辑密码") }, onNavigateUp = { this.finish() }, actions = {
                        Button(onClick = {
                            trySave.value = true
                            if (title.value.isNotEmpty() && user.value.isNotEmpty() && password.value.isNotEmpty()) {
                                val passwordData = PasswordData(
                                        0, title.value, user.value, password.value, remark.value, 1, selected.value.id
                                )
                                AppDatabase.database.PasswordDataDao().insertPasswordData(passwordData) // 放入数据
                                this@AddPasswordActivity.finish()
                            }
                        }, content = { Text("保存") }, modifier = Modifier.padding(end = 8.dp))
                    })
                }) {
                    Column(Modifier.padding(it)) {
                        MainUI(title, user, password, remark, selected, groupList)
                    }
                }
            }
        }
    }

    @Composable
    fun MainUI(title: MutableState<String>, user: MutableState<String>, password: MutableState<String>, remark: MutableState<String>,
               selected: MutableState<PasswordGroup>, groupList: List<PasswordGroup>
    ) {
        TextField(value = title.value,
                onValueChange = { title.value = it },
                label = { Text("该密码用在何处？") },
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                error = if (title.value.isEmpty() && trySave.value) {
                    { Text("此处不能为空") }
                } else null)
        // 阴影
        Spacer(
                modifier = Modifier
                        .zIndex(0.5f)
                        .shadow(1.dp)
                        .fillMaxWidth()
                        .height(1.dp)
        )
        Column(
                Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
        ) {
            TextField(value = user.value,
                    onValueChange = { user.value = it },
                    leadingIcon = { Icon(imageVector = Icons.Filled.AccountCircle, contentDescription = "账号") },
                    label = { Text("用户名 & 账号") },
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    error = if (user.value.isEmpty() && trySave.value) {
                        { Text("此处不能为空") }
                    } else null)
            TextField(value = password.value,
                    onValueChange = { password.value = it },
                    leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_key_24), contentDescription = "密码") },
                    label = { Text("密码") },
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    error = if (password.value.isEmpty() && trySave.value) {
                        { Text("此处不能为空") }
                    } else null)
            TextField(value = remark.value,
                    onValueChange = { remark.value = it },
                    leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_contact_page_24), contentDescription = null) },
                    label = { Text("备注") },
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                SelectField(modifier = Modifier
                        .padding(end = 8.dp)
                        .weight(1f),
                        value = selected.value.groupName,
                        options = groupList,
                        onOptionSelect = { selected.value = it },
                        placeholder = { Text("无分组") },
                        label = { Text("分组") }) {
                    Text(it.groupName)
                }
                ButtonPrimarySubtle(onClick = { startActivity(Intent(this@AddPasswordActivity, GroupManagerActivity::class.java)) },
                        content = { Text("管理分组") },
                        modifier = Modifier.padding(top = 24.dp)
                )
            }
        }
    }
}