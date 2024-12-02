package com.pengxh.autodingding.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.pengxh.autodingding.R
import com.pengxh.autodingding.bean.DateTimeBean
import com.pengxh.autodingding.extensions.convertToWeek
import com.pengxh.autodingding.extensions.diffCurrentMillis
import com.pengxh.autodingding.extensions.isEarlierThenCurrent
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateTimeAdapter(
    context: Context,
    private val dataBeans: MutableList<DateTimeBean>
) : RecyclerView.Adapter<DateTimeAdapter.ItemViewHolder>() {

    private val kTag = "DateTimeAdapter"
    private val countDownTimerHashMap by lazy { HashMap<String, CountDownTimer>() }
    private val layoutInflater = LayoutInflater.from(context)

    // 日期格式化器，包含秒数
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    @SuppressLint("NotifyDataSetChanged")
    fun setRefreshData(dataRows: MutableList<DateTimeBean>) {
        this.dataBeans.clear()
        this.dataBeans.addAll(dataRows)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataBeans.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            layoutInflater.inflate(R.layout.item_timer_rv_l, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val timeBean = dataBeans[holder.adapterPosition]

        // 停止之前的定时器，避免重复计时
        stopCountDownTimer(timeBean)

        // 生成随机时间
        val randomTimeStr = generateRandomTime(timeBean)

        // 解析随机时间为日期和时间部分
        val dateTime = try {
            sdf.parse(randomTimeStr)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }

        if (dateTime != null) {
            val randomDate = dateFormatter.format(dateTime)
            val randomTimeOnly = timeFormatter.format(dateTime)

            // 更新界面显示为随机后的日期和时间
            holder.dateView.text = randomDate
            holder.timeView.text = randomTimeOnly
            holder.weekDayView.text = randomDate.convertToWeek()

            if (randomTimeStr.isEarlierThenCurrent()) {
                holder.countDownTextView.text = "任务已过期"
                holder.countDownTextView.setTextColor(Color.RED)
            } else {
                val diffCurrentMillis = randomTimeStr.diffCurrentMillis()

                holder.countDownTextView.setTextColor(Color.BLUE)

                holder.countDownProgress.max = diffCurrentMillis.toInt()

                // 开始倒计时
                val countDownTimer = object : CountDownTimer(diffCurrentMillis, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        // 确保更新对应的视图
                        if (holder.adapterPosition == position) {
                            holder.countDownProgress.progress =
                                (diffCurrentMillis - millisUntilFinished).toInt()

                            holder.countDownTextView.text =
                                "${millisUntilFinished / 1000}秒后执行定时任务"
                        }
                    }

                    override fun onFinish() {
                        if (holder.adapterPosition == position) {
                            holder.countDownTextView.text = "任务已过期"
                            holder.countDownTextView.setTextColor(Color.RED)

                            // 执行任务完成后的操作
                            itemClickListener?.onCountDownFinish()

                            // 延长任务时间一天
                            extendTaskOneDay(timeBean)

                            // 通知界面更新
                            notifyItemChanged(holder.adapterPosition)
                        }
                    }
                }.start()
                countDownTimerHashMap[timeBean.uuid] = countDownTimer
            }
        } else {
            // 日期解析失败，处理异常情况
            holder.dateView.text = timeBean.date
            holder.timeView.text = timeBean.time
            holder.weekDayView.text = timeBean.date.convertToWeek()
            holder.countDownTextView.text = "时间格式错误"
            holder.countDownTextView.setTextColor(Color.RED)
        }

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(holder.adapterPosition)
        }

        // 长按监听
        holder.itemView.setOnLongClickListener {
            itemClickListener?.onItemLongClick(holder.adapterPosition)
            true
        }
    }

    // 生成随机时间，不修改 timeBean
    private fun generateRandomTime(timeBean: DateTimeBean): String {
        val originalDateTimeStr = "${timeBean.date} ${timeBean.time}" // 已包含秒数
        val originalDateTime = sdf.parse(originalDateTimeStr)
        if (originalDateTime != null) {
            calendar.time = originalDateTime

            // 如果当前日期已经过去，延长一天
            if (originalDateTime.before(Date())) {
                extendTaskOneDay(timeBean)
                calendar.time = sdf.parse("${timeBean.date} ${timeBean.time}")!!
            }

            // 添加随机延迟
            val randomMinutes = Random().nextInt(19) // 0 到 20 分钟
            val randomSeconds = Random().nextInt(60) // 0 到 59 秒
            calendar.add(Calendar.MINUTE, randomMinutes)
            calendar.add(Calendar.SECOND, randomSeconds)

            // 生成新的日期时间字符串，不修改 timeBean
            val newDateStr = dateFormatter.format(calendar.time)
            val newTimeStr = timeFormatter.format(calendar.time)

            // 返回新的日期时间字符串
            return "$newDateStr $newTimeStr"
        }
        return originalDateTimeStr // 返回原始时间字符串
    }

    // 延长任务时间一天，跳过周日，并考虑跨月、跨年情况
    private fun extendTaskOneDay(timeBean: DateTimeBean) {
        val originalDateTimeStr = "${timeBean.date} ${timeBean.time}"
        val originalDateTime = sdf.parse(originalDateTimeStr)
        if (originalDateTime != null) {
            calendar.time = originalDateTime
            calendar.add(Calendar.DAY_OF_MONTH, 1) // 增加一天

            val newDateStr = dateFormatter.format(calendar.time)
            val newTimeStr = timeFormatter.format(calendar.time)

            // 更新 timeBean 的日期和时间
            timeBean.date = newDateStr
            timeBean.time = newTimeStr

        }
    }

    // 停止计时器
    fun stopCountDownTimer(bean: DateTimeBean) {
        val downTimer = countDownTimerHashMap[bean.uuid]
        if (downTimer != null) {
            downTimer.cancel()
            countDownTimerHashMap.remove(bean.uuid)
            Log.d(kTag, "stopCountDownTimer: ${bean.uuid}")
        }
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.itemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)

        fun onItemLongClick(position: Int)

        fun onCountDownFinish()
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var timeView: TextView = itemView.findViewById(R.id.timeView)
        var dateView: TextView = itemView.findViewById(R.id.dateView)
        var weekDayView: TextView = itemView.findViewById(R.id.weekDayView)
        var countDownTextView: TextView = itemView.findViewById(R.id.countDownTextView)
        var countDownProgress: LinearProgressIndicator =
            itemView.findViewById(R.id.countDownProgress)
    }
}