package com.sanenchen.accountbook.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.sanenchen.accountbook.databases.AppDatabase
import com.sanenchen.accountbook.databases.PasswordData
import com.sanenchen.accountbook.databases.PasswordGroup
import com.sanenchen.accountbook.databases.SettingInformation
import com.sanenchen.accountbook.ui.theme.AccountBookTheme
import kiwi.orbit.compose.icons.Icons
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.*

/**
 * 欢迎页面 设置安全密码
 * @author sanenchen
 */
class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 判断是否已经注册过了
        // 首先，将数据库初始化好，接下来的数据库都会是这一个，只初始化一次本数据库
        AppDatabase.database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").allowMainThreadQueries().build()
        if(AppDatabase.database.SettingInformationDao().queryAll().isNotEmpty()){
            // 跳转
            startActivity(Intent(this@WelcomeActivity, VerifyActivity::class.java))
            this@WelcomeActivity.finish()
        }
        setContent {
            AccountBookTheme {
                Scaffold(topBar = { TopAppBar(title = { Text("设置密码信息") }) }) { it ->
                    Column(
                        Modifier
                            .padding(it)
                            .padding(all = 16.dp)
                    ) {
                        // 设置密码
                        var passwordFirst by remember { mutableStateOf("") }
                        var passwordFinal by remember { mutableStateOf("") }
                        var isFirstError by remember { mutableStateOf(false) }
                        var isSecondError by remember { mutableStateOf(false) }
                        TextField(
                            value = passwordFirst,
                            onValueChange = { passwordFirst = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            label = { Text("安全密码 至少6位") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(painter = Icons.Security, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        TextField(
                            value = passwordFinal,
                            onValueChange = { passwordFinal = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            label = { Text("再次输入安全密码") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            leadingIcon = { Icon(painter = Icons.Security, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation()
                        )
                        isFirstError = passwordFirst.length < 6
                        isSecondError = passwordFinal != passwordFirst
                        // 关于指纹识别
                        var isUsedFingerPrint by remember { mutableStateOf(isFingerPrint()) }
                        Row(
                            verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Switch(
                                checked = isUsedFingerPrint,
                                onCheckedChange = { isUsedFingerPrint = !isUsedFingerPrint },
                                enabled = isFingerPrint()
                            )
                            Text(
                                text = "生物认证",
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 14.sp,
                                style = OrbitTheme.typography.bodyNormalMedium
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.End, modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            var isShowingWarningDialog by remember { mutableStateOf(false) }
                            ButtonPrimary(onClick = {
                                if (isFirstError || isSecondError) {
                                    Toast.makeText(this@WelcomeActivity, "啊嘞，有什么东西填错了哎", Toast.LENGTH_SHORT).show()
                                } else {
                                    // 插入相关数据
                                    val settingInformation = SettingInformation(
                                        0,
                                        passwordFinal,
                                        isUsedFingerPrint,
                                        allowScreenShot = false,
                                        directShowPassword = false
                                    )
                                    val passwordGroup = PasswordGroup(0, "社交")
                                    val passwordData = PasswordData(
                                        0,
                                        "Sample Account",
                                        "sampleUser",
                                        "samplePassword",
                                        "",
                                        1,
                                        AppDatabase.database.PasswordGroupDao().insertPasswordGroup(passwordGroup).toInt()
                                    )
                                    // 导入 Sample 数据
                                    AppDatabase.database.PasswordDataDao().insertPasswordData(passwordData)
                                    AppDatabase.database.SettingInformationDao().insertSettingInfo(settingInformation)
                                    Log.i("DataBase", AppDatabase.database.SettingInformationDao().queryAll()[0].password)
                                    isShowingWarningDialog = true
                                }
                            }) {
                                Text("完成")
                            }
                            if (isShowingWarningDialog)
                                AlertDialog(title = { Text("警告", style = OrbitTheme.typography.title2) },
                                    text = { Text("请注意，本安全密码为确认您身份的最后凭证，请妥善保管", style = OrbitTheme.typography.bodyNormal) },
                                    confirmButton = {
                                        Button(onClick = {
                                            startActivity(Intent(this@WelcomeActivity, VerifyActivity::class.java))
                                            this@WelcomeActivity.finish()
                                        }, content = { Text("知道了") })
                                    },
                                    onDismissRequest = {}
                                )
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断是否支持生物认证
     */
    private fun isFingerPrint(): Boolean {
        return BiometricManager.from(this@WelcomeActivity)
            .canAuthenticate(Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }
}