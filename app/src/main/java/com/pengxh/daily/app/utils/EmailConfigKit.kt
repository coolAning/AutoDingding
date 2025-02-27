package com.pengxh.daily.app.utils

import com.pengxh.daily.app.model.EmailConfigModel
import com.pengxh.daily.app.utils.Constant.MAIL_SERVER
import com.pengxh.daily.app.utils.Constant.MAIL_SERVER_PORT
import com.pengxh.daily.app.utils.Constant.PERMISSION_CODE
import com.pengxh.daily.app.utils.Constant.USER_MAIL_ACCOUNT
import com.pengxh.kt.lite.utils.SaveKeyValues

object EmailConfigKit {
    fun getConfig(): EmailConfigModel {
        val emailAddress = SaveKeyValues.getValue(Constant.EMAIL_SEND_ADDRESS_KEY, USER_MAIL_ACCOUNT) as String
        val emailCode = SaveKeyValues.getValue(Constant.EMAIL_SEND_CODE_KEY, PERMISSION_CODE) as String
        val emailServer = SaveKeyValues.getValue(Constant.EMAIL_SEND_SERVER_KEY, MAIL_SERVER) as String
        val emailPort = SaveKeyValues.getValue(Constant.EMAIL_SEND_PORT_KEY, MAIL_SERVER_PORT) as String
        val emailInBox = SaveKeyValues.getValue(Constant.EMAIL_IN_BOX_KEY, "") as String
        val emailTitle = SaveKeyValues.getValue(Constant.EMAIL_TITLE_KEY, "打卡结果通知") as String
        return EmailConfigModel(
            emailAddress, emailCode, emailServer, emailPort, emailInBox, emailTitle
        )
    }

    fun isEmailConfigured(): Boolean {
        val config = getConfig()
        return config.emailSender.isNotEmpty() && config.authCode.isNotEmpty()
                && config.senderServer.isNotEmpty() && config.emailPort.isNotEmpty()
                && config.inboxEmail.isNotEmpty()
    }
}