package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShopDetails
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TealAccent

@Composable
fun ShopSetupScreen(
    initialDetails: ShopDetails? = null,
    onSaveSuccess: (ShopDetails) -> Unit
) {
    var shopName by remember { mutableStateOf(initialDetails?.shopName ?: "") }
    var ownerName by remember { mutableStateOf(initialDetails?.ownerName ?: "") }
    var mobileNumber by remember { mutableStateOf(initialDetails?.mobileNumber ?: "") }
    var address by remember { mutableStateOf(initialDetails?.address ?: "") }
    var city by remember { mutableStateOf(initialDetails?.city ?: "") }
    var state by remember { mutableStateOf(initialDetails?.state ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // App Brand Icon & Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PointOfSale,
                        contentDescription = "VyaparPOS Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "VyaparPOS",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealAccent,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Set Up Your Shop",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Enter your store profile to start billing and inventory",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Shop Name
                        OutlinedTextField(
                            value = shopName,
                            onValueChange = {
                                shopName = it
                                errorMessage = null
                            },
                            label = { Text("Shop Name *") },
                            placeholder = { Text("e.g. Apex Supermarket") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = "Shop Name",
                                    tint = NavyPrimary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_shop_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                focusedLabelColor = NavyPrimary
                            )
                        )

                        // 2. Owner Name
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = {
                                ownerName = it
                                errorMessage = null
                            },
                            label = { Text("Owner Name *") },
                            placeholder = { Text("e.g. Ramesh Patel") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Owner Name",
                                    tint = NavyPrimary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_owner_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                focusedLabelColor = NavyPrimary
                            )
                        )

                        // 3. Mobile Number
                        OutlinedTextField(
                            value = mobileNumber,
                            onValueChange = {
                                mobileNumber = it
                                errorMessage = null
                            },
                            label = { Text("Mobile Number *") },
                            placeholder = { Text("e.g. 9876543210") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "Mobile Number",
                                    tint = NavyPrimary
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_mobile_number"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                focusedLabelColor = NavyPrimary
                            )
                        )

                        // 4. Address
                        OutlinedTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                errorMessage = null
                            },
                            label = { Text("Address *") },
                            placeholder = { Text("e.g. Shop #12, Main Market Road") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = "Address",
                                    tint = NavyPrimary
                                )
                            },
                            singleLine = false,
                            maxLines = 2,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_address"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NavyPrimary,
                                focusedLabelColor = NavyPrimary
                            )
                        )

                        // 5. City and 6. State
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = city,
                                onValueChange = {
                                    city = it
                                    errorMessage = null
                                },
                                label = { Text("City *") },
                                placeholder = { Text("Mumbai") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LocationCity,
                                        contentDescription = "City",
                                        tint = NavyPrimary
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_city"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    focusedLabelColor = NavyPrimary
                                )
                            )

                            OutlinedTextField(
                                value = state,
                                onValueChange = {
                                    state = it
                                    errorMessage = null
                                },
                                label = { Text("State *") },
                                placeholder = { Text("Maharashtra") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Map,
                                        contentDescription = "State",
                                        tint = NavyPrimary
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_state"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NavyPrimary,
                                    focusedLabelColor = NavyPrimary
                                )
                            )
                        }

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // "Save & Continue" button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (shopName.isBlank()) {
                            errorMessage = "Please enter your Shop Name"
                        } else if (ownerName.isBlank()) {
                            errorMessage = "Please enter the Owner Name"
                        } else if (mobileNumber.isBlank()) {
                            errorMessage = "Please enter Mobile Number"
                        } else if (address.isBlank()) {
                            errorMessage = "Please enter Address"
                        } else if (city.isBlank()) {
                            errorMessage = "Please enter City"
                        } else if (state.isBlank()) {
                            errorMessage = "Please enter State"
                        } else {
                            errorMessage = null
                            val details = ShopDetails(
                                shopName = shopName.trim(),
                                ownerName = ownerName.trim(),
                                mobileNumber = mobileNumber.trim(),
                                address = address.trim(),
                                city = city.trim(),
                                state = state.trim()
                            )
                            onSaveSuccess(details)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_and_continue_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text(
                        text = "Save & Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
