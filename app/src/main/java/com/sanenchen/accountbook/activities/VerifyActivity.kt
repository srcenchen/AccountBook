package com.sanenchen.accountbook.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.sanenchen.accountbook.R
import com.sanenchen.accountbook.databases.AppDatabase
import com.sanenchen.accountbook.ui.theme.AccountBookTheme
import kiwi.orbit.compose.ui.OrbitTheme
import kiwi.orbit.compose.ui.controls.ButtonPrimarySubtle

/**
 * @author sanenchen
 */
class VerifyActivity : FragmentActivity() {
    var autoVerify = true // 是否自动弹出验证框
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccountBookTheme {
                // 开发测试专用
                //startActivity(Intent(this@VerifyActivity, MainActivity::class.java))
                //this@VerifyActivity.finish()
                MainUI()
            }
        }
    }

    @Preview
    @Composable
    fun MainUI() {
        var password by remember { mutableStateOf("") }
        val isUsingBiometric = AppDatabase.database.SettingInformationDao().queryAll()[0].isUsingBiometric
        if (isUsingBiometric && autoVerify) {
            autoVerify = false
            verifyBiometric() // 自动弹出验证框
        }

        Column(
            Modifier
                .background(OrbitTheme.colors.surface.main)
                .fillMaxSize()
                .padding(all = 32.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, Modifier.size(48.dp))
                if (isUsingBiometric)
                // 手动点击验证框
                    IconButton(onClick = {
                        verifyBiometric()
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            modifier = Modifier.size(48.dp),
                            painter = painterResource(id = R.drawable.baseline_fingerprint_24),
                            contentDescription = "指纹识别"
                        )
                    }
            }
            Spacer(modifier = Modifier.padding(top = 32.dp))
            Text("输入安全密码", fontSize = 32.sp, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.padding(top = 24.dp))
            Text("输入您的安全密码以验证身份", fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = password, onValueChange = { password = it },
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .width(192.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (verifyPassword(password)) { // 密码正确
                                // 跳转
                                startActivity(Intent(this@VerifyActivity, MainActivity::class.java))
                                this@VerifyActivity.finish()
                            } else { // 密码错误
                                Toast.makeText(this@VerifyActivity, "密码错误", Toast.LENGTH_SHORT).show()
                                password = ""
                            }
                        } // 监听确认键
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(onClick = {
                    if (verifyPassword(password)) { // 密码正确
                        // 跳转
                        startActivity(Intent(this@VerifyActivity, MainActivity::class.java))
                        this@VerifyActivity.finish()
                    } else { // 密码错误
                        Toast.makeText(this@VerifyActivity, "密码错误", Toast.LENGTH_SHORT).show()
                        password = ""
                    }
                }, content = { Text("鉴定") })
            }
        }
    }

    /**
     * 验证输入密码的正确性
     */
    private fun verifyPassword(password: String): Boolean {
        val rightPassword = AppDatabase.database.SettingInformationDao().queryAll()[0].password // 获取密码
        return rightPassword == password
    }

    /**
     * 生物验证
     */
    private fun verifyBiometric() {
        val biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("需要鉴定").setDescription("鉴定以登入 AccountBook")
            .setNegativeButtonText("取消").build()
        val executor = ContextCompat.getMainExecutor(this)
        val biometric = BiometricPrompt(this@VerifyActivity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                startActivity(Intent(this@VerifyActivity, MainActivity::class.java))
                this@VerifyActivity.finish()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@VerifyActivity, errString, Toast.LENGTH_SHORT).show()
            }
        })
        // 开始验证
        biometric.authenticate(biometricPromptInfo)
    }
}