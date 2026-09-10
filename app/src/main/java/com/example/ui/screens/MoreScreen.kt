package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShopDetails
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TealAccent

@Composable
fun MoreScreen(
    shopDetails: ShopDetails,
    onEditShopDetails: () -> Unit,
    onResetShopData: () -> Unit,
    onOpenProducts: () -> Unit = {},
    onOpenCategoriesBrands: () -> Unit = {},
    onOpenAppUpdates: () -> Unit = {}
) {
    var showShopDetailsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showComingSoonDialog by remember { mutableStateOf<String?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_more"),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shop Overview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("more_shop_overview_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = "Shop Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = shopDetails.shopName.ifEmpty { "My Business" },
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Owner: ${shopDetails.ownerName}",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            TextButton(
                                onClick = onEditShopDetails,
                                modifier = Modifier.testTag("button_edit_shop_header")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = TealAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    color = TealAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "📞 ${shopDetails.mobileNumber}   •   📍 ${shopDetails.city}, ${shopDetails.state}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Application Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Options List Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Shop Details
                        MoreOptionItem(
                            icon = Icons.Default.Storefront,
                            iconColor = NavyPrimary,
                            title = "Shop Details",
                            subtitle = "View full address & business information",
                            tag = "more_item_shop_details",
                            onClick = { showShopDetailsDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Products Catalog
                        MoreOptionItem(
                            icon = Icons.Default.Category,
                            iconColor = Color(0xFF1565C0),
                            title = "Products Catalog",
                            subtitle = "Add, edit, search items & stock pricing",
                            tag = "more_item_products",
                            onClick = onOpenProducts
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Categories & Brands
                        MoreOptionItem(
                            icon = Icons.Default.BrandingWatermark,
                            iconColor = Color(0xFF512DA8),
                            title = "Categories & Brands",
                            subtitle = "Organize departments, item types & brand tags",
                            tag = "more_item_categories_brands",
                            onClick = onOpenCategoriesBrands
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // 2. Settings
                        MoreOptionItem(
                            icon = Icons.Default.Settings,
                            iconColor = Color(0xFF1565C0),
                            title = "Settings",
                            subtitle = "Receipt printing, currency & POS preferences",
                            tag = "more_item_settings",
                            onClick = { showSettingsDialog = true }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // App Updates
                        MoreOptionItem(
                            icon = Icons.Default.SystemUpdate,
                            iconColor = NavyPrimary,
                            title = "App Updates",
                            subtitle = "Check for updates & remote version config",
                            tag = "more_item_app_updates",
                            onClick = onOpenAppUpdates
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // 3. Backup & Restore (Coming Soon)
                        MoreOptionItem(
                            icon = Icons.Default.CloudSync,
                            iconColor = Color(0xFF00695C),
                            title = "Backup & Restore (Coming Soon)",
                            subtitle = "Secure cloud sync & offline data export",
                            tag = "more_item_backup",
                            onClick = {
                                showComingSoonDialog = "Backup & Restore feature is coming soon in the next release."
                            }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // 4. License (Coming Soon)
                        MoreOptionItem(
                            icon = Icons.Default.Badge,
                            iconColor = Color(0xFF6A1B9A),
                            title = "License (Coming Soon)",
                            subtitle = "VyaparPOS Retail Standard Edition",
                            tag = "more_item_license",
                            onClick = {
                                showComingSoonDialog = "License and activation details will be available in the upcoming release."
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Reset Shop Setup Option
                OutlinedButton(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("button_reset_shop_setup"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Shop Setup",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Shop Details Dialog
    if (showShopDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showShopDetailsDialog = false },
            title = {
                Text(
                    text = "Shop Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailRow(label = "Shop Name", value = shopDetails.shopName)
                    DetailRow(label = "Owner Name", value = shopDetails.ownerName)
                    DetailRow(label = "Mobile Number", value = shopDetails.mobileNumber)
                    DetailRow(label = "Address", value = shopDetails.address)
                    DetailRow(label = "City", value = shopDetails.city)
                    DetailRow(label = "State", value = shopDetails.state)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showShopDetailsDialog = false
                        onEditShopDetails()
                    }
                ) {
                    Text("Edit Details", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShopDetailsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        var soundEnabled by remember { mutableStateOf(true) }
        var autoPrintEnabled by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailRow(label = "Currency", value = "₹ INR (Indian Rupee)")
                    DetailRow(label = "Thermal Print Format", value = "Standard 80mm (3-inch)")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Scanner Beep Sound", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Audio feedback on item scan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Print Invoice", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Print bill immediately after payment", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoPrintEnabled,
                            onCheckedChange = { autoPrintEnabled = it }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSettingsDialog = false
                                onOpenAppUpdates()
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("App Updates", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Check for updates & remote source", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Coming Soon Dialog
    if (showComingSoonDialog != null) {
        AlertDialog(
            onDismissRequest = { showComingSoonDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Coming Soon", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(showComingSoonDialog ?: "")
            },
            confirmButton = {
                TextButton(onClick = { showComingSoonDialog = null }) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text("Reset Shop Setup?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will clear your local shop details and return to the First Launch Shop Setup screen.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetShopData()
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MoreOptionItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Open",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value.ifEmpty { "Not specified" },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
