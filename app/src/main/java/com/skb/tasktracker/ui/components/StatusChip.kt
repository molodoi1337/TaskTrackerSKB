package com.skb.tasktracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skb.tasktracker.data.entity.TaskStatus
import com.skb.tasktracker.ui.theme.StatusDone
import com.skb.tasktracker.ui.theme.StatusInProgress
import com.skb.tasktracker.ui.theme.StatusNew

fun TaskStatus.color(): Color = when (this) {
    TaskStatus.NEW -> StatusNew
    TaskStatus.IN_PROGRESS -> StatusInProgress
    TaskStatus.DONE -> StatusDone
}

@Composable
fun StatusChip(status: TaskStatus, modifier: Modifier = Modifier) {
    Text(
        text = status.title,
        color = Color.White,
        fontSize = 12.sp,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(status.color())
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
