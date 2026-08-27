package com.hashmimotors.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import com.hashmimotors.app.ui.theme.Glass
import com.hashmimotors.app.ui.theme.Gold
import com.hashmimotors.app.ui.theme.GoldDim
import com.hashmimotors.app.ui.theme.GoldSoft
import com.hashmimotors.app.ui.theme.HairlineGold
import com.hashmimotors.app.ui.theme.Ink
import com.hashmimotors.app.ui.theme.Ivory
import com.hashmimotors.app.ui.theme.IvoryMute

@Composable
fun HmTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = Ivory)
            }
        } else {
            Spacer(Modifier.width(8.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Ivory, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, color = IvoryMute, fontSize = 12.sp)
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun HmCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Glass, shape)
            .border(1.dp, HairlineGold, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
    ) { content() }
}

@Composable
fun HmIconWell(
    icon: ImageVector,
    tint: Color = Gold,
    size: Int = 44
) {
    Box(
        Modifier
            .size(size.dp)
            .background(Gold.copy(alpha = 0.12f), CircleShape)
            .border(1.dp, HairlineGold, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size((size * 0.48f).dp))
    }
}

enum class MainTab { HOME, CATALOG, SCAN, BILL, MORE }

@Composable
fun HmBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Ink.copy(alpha = 0.92f), Ink)
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xEE12141C))
            .border(1.dp, HairlineGold, RoundedCornerShape(22.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabItem("Home", Icons.Filled.Home, selected == MainTab.HOME) { onSelect(MainTab.HOME) }
        TabItem("Catalog", Icons.Filled.Search, selected == MainTab.CATALOG) { onSelect(MainTab.CATALOG) }
        ScanTab { onSelect(MainTab.SCAN) }
        TabItem("Bill", Icons.Filled.ReceiptLong, selected == MainTab.BILL) { onSelect(MainTab.BILL) }
        TabItem("More", Icons.Filled.MoreHoriz, selected == MainTab.MORE) { onSelect(MainTab.MORE) }
    }
}

@Composable
private fun RowScope.TabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            label,
            tint = if (selected) GoldSoft else IvoryMute,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            color = if (selected) GoldSoft else IvoryMute,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ScanTab(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(GoldSoft, GoldDim)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.QrCodeScanner, "Scan", tint = Ink, modifier = Modifier.size(26.dp))
    }
}

@Composable
fun hmFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Ivory,
    unfocusedTextColor = Ivory,
    focusedBorderColor = Gold,
    unfocusedBorderColor = Ivory.copy(alpha = 0.28f),
    focusedLabelColor = GoldSoft,
    unfocusedLabelColor = IvoryMute,
    cursorColor = Gold,
    focusedPlaceholderColor = IvoryMute,
    unfocusedPlaceholderColor = IvoryMute,
    focusedTrailingIconColor = Gold,
    unfocusedTrailingIconColor = IvoryMute,
    focusedLeadingIconColor = GoldSoft,
    unfocusedLeadingIconColor = IvoryMute
)

@Composable
fun HmEmptyState(title: String, body: String, icon: ImageVector = Icons.Filled.Inventory2) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HmIconWell(icon, size = 64)
        Spacer(Modifier.height(16.dp))
        Text(title, color = Ivory, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Text(body, color = IvoryMute, fontSize = 13.sp)
    }
}
