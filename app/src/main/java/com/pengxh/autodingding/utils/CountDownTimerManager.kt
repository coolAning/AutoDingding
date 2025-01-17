package com.pengxh.autodingding.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.pengxh.autodingding.extensions.createTextMail
import com.pengxh.autodingding.extensions.sendTextMail
import com.pengxh.autodingding.service.FloatingWindowService
import com.pengxh.autodingding.service.ForegroundRunningService
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.kt.lite.extensions.show
import com.pengxh.kt.lite.utils.SaveKeyValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CountDownTimerManager private constructor() : LifecycleOwner {

    private val kTag = "CountDownTimerManager"
    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return registry
    }

    companion object {
        val get by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            CountDownTimerManager()
        }
    }

    private var timer: CountDownTimer? = null

    // 启动前台服务
    private fun startForegroundService(context: Context) {
        if (!isServiceRunning(context, ForegroundRunningService::class.java)) {
            val intent = Intent(context, ForegroundRunningService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    // 检查服务是否在运行
    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun startTimer(context: Context, millisInFuture: Long, countDownInterval: Long) {
        Log.d(kTag, "startTimer: 开始倒计时")
        FloatingWindowService.weakReferenceHandler?.sendEmptyMessage(Constant.SHOW_FLOATING_WINDOW_CODE)
        startForegroundService(context) // 启动前台服务
        timer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val tick = millisUntilFinished / 1000
                val handler = FloatingWindowService.weakReferenceHandler ?: return
                val message = handler.obtainMessage()
                message.what = 2024071701
                message.obj = tick
                handler.sendMessage(message)
            }

            override fun onFinish() {
                cancelTimer()
                Log.d(kTag, "onFinish: 倒计时结束")
                if (SaveKeyValues.getValue(Constant.BACK_TO_HOME, false) as Boolean) {
                    // 使用ActivityManager返回自己的应用
                    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)
                    for (task in tasks) {
                        val baseActivity = task.baseActivity
                        if (baseActivity != null && baseActivity.packageName == context.packageName) {
                            activityManager.moveTaskToFront(task.id, 0)
                            Log.d(kTag, "返回自己的应用任务")
                            break
                        }
                    }
                    Thread.sleep(2000)
                }

                val intent = Intent(context, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                FloatingWindowService.weakReferenceHandler?.sendEmptyMessage(Constant.HIDE_FLOATING_WINDOW_CODE)

                val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_ADDRESS, "") as String
                if (emailAddress.isEmpty()) {
                    "邮箱地址为空".show(context)
                    return
                }

                "未监听到打卡通知，即将发送异常日志邮件，请注意查收".show(context)
                lifecycleScope.launch(Dispatchers.IO) {
                    val subject = SaveKeyValues.getValue(
                        Constant.EMAIL_TITLE, "打卡结果通知"
                    ) as String
                    "".createTextMail(subject, emailAddress).sendTextMail()
                }
            }
        }.start()
    }

    fun cancelTimer() {
        timer?.cancel()
        Log.d(kTag, "cancelTimer: 取消超时定时器")
    }
}