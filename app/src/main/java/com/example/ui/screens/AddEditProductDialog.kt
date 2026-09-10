package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.BrandEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ProductEntity
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TealAccent

val STANDARD_UNITS = listOf("Pcs", "Box", "Packet", "Bottle", "Kg", "Gram", "Litre", "Dozen")
val STANDARD_GST_RATES = listOf(0.0, 5.0, 12.0, 18.0, 28.0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductDialog(
    initialProduct: ProductEntity?,
    categories: List<CategoryEntity>,
    brands: List<BrandEntity>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity, Boolean) -> Unit,
    onGenerateSku: (String) -> String,
    onGenerateInternalBarcode: () -> String,
    onQuickAddCategory: (String) -> Unit,
    onQuickAddBrand: (String) -> Unit
) {
    val isEdit = initialProduct != null

    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.categoryName ?: (categories.firstOrNull()?.name ?: "General")) }
    var selectedBrand by remember { mutableStateOf(initialProduct?.brandName ?: (brands.firstOrNull()?.name ?: "Generic")) }
    var selectedUnit by remember { mutableStateOf(initialProduct?.unit ?: "Pcs") }
    var purchasePriceStr by remember { mutableStateOf(initialProduct?.purchasePrice?.let { if (it > 0) it.toString() else "" } ?: "") }
    var sellingPriceStr by remember { mutableStateOf(initialProduct?.sellingPrice?.let { if (it > 0) it.toString() else "" } ?: "") }
    var mrpStr by remember { mutableStateOf(initialProduct?.mrp?.let { if (it > 0) it.toString() else "" } ?: "") }
    var minStockStr by remember { mutableStateOf(initialProduct?.minStock?.toString() ?: "5.0") }
    var openingStockStr by remember { mutableStateOf(initialProduct?.openingStock?.toString() ?: "0.0") }
    var supplier by remember { mutableStateOf(initialProduct?.supplier ?: "") }
    var gstRate by remember { mutableDoubleStateOf(initialProduct?.gstRate ?: 0.0) }
    var isActive by remember { mutableStateOf(initialProduct?.isActive ?: true) }
    var allowSellingAboveMrp by remember { mutableStateOf(initialProduct?.allowSellingAboveMrp ?: false) }

    var unitDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var brandDropdownExpanded by remember { mutableStateOf(false) }
    var gstDropdownExpanded by remember { mutableStateOf(false) }

    var showQuickAddCatDialog by remember { mutableStateOf(false) }
    var showQuickAddBrandDialog by remember { mutableStateOf(false) }
    var quickCatName by remember { mutableStateOf("") }
    var quickBrandName by remember { mutableStateOf("") }

    val sellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
    val mrp = mrpStr.toDoubleOrNull() ?: 0.0
    val isSellingAboveMrp = mrp > 0 && sellingPrice > mrp && !allowSellingAboveMrp

    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("dialog_add_edit_product"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEdit) "Edit Product" else "Add New Product",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Fill in product details and pricing",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("button_close_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable form body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Product Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_name"),
                        singleLine = true,
                        isError = name.isBlank() && isEdit
                    )

                    // 2. SKU and Auto-generate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it.uppercase() },
                            label = { Text("SKU Code *") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_product_sku"),
                            singleLine = true,
                            placeholder = { Text("e.g. PRD-1001") }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { sku = onGenerateSku(name) },
                            modifier = Modifier.testTag("button_auto_sku")
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto", fontSize = 12.sp)
                        }
                    }

                    // 3. Barcode and Internal Barcode Gen
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode (EAN / Internal)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_product_barcode"),
                            singleLine = true,
                            placeholder = { Text("Scan or enter barcode") }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = { barcode = onGenerateInternalBarcode() },
                            modifier = Modifier.testTag("button_gen_barcode")
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gen", fontSize = 12.sp)
                        }
                    }

                    // 4. Category & Brand Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Category Dropdown
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("dropdown_product_category")
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = {
                                            selectedCategory = cat.name
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("+ Add Category", color = NavyPrimary, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        categoryDropdownExpanded = false
                                        showQuickAddCatDialog = true
                                    }
                                )
                            }
                        }

                        // Brand Dropdown
                        ExposedDropdownMenuBox(
                            expanded = brandDropdownExpanded,
                            onExpandedChange = { brandDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedBrand,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Brand") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("dropdown_product_brand")
                            )
                            ExposedDropdownMenu(
                                expanded = brandDropdownExpanded,
                                onDismissRequest = { brandDropdownExpanded = false }
                            ) {
                                brands.forEach { br ->
                                    DropdownMenuItem(
                                        text = { Text(br.name) },
                                        onClick = {
                                            selectedBrand = br.name
                                            brandDropdownExpanded = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("+ Add Brand", color = NavyPrimary, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        brandDropdownExpanded = false
                                        showQuickAddBrandDialog = true
                                    }
                                )
                            }
                        }
                    }

                    // 5. Unit and GST Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Unit
                        ExposedDropdownMenuBox(
                            expanded = unitDropdownExpanded,
                            onExpandedChange = { unitDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedUnit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("dropdown_product_unit")
                            )
                            ExposedDropdownMenu(
                                expanded = unitDropdownExpanded,
                                onDismissRequest = { unitDropdownExpanded = false }
                            ) {
                                STANDARD_UNITS.forEach { unitItem ->
                                    DropdownMenuItem(
                                        text = { Text(unitItem) },
                                        onClick = {
                                            selectedUnit = unitItem
                                            unitDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // GST Rate
                        ExposedDropdownMenuBox(
                            expanded = gstDropdownExpanded,
                            onExpandedChange = { gstDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = "${gstRate.toInt()}% GST",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("GST Rate") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gstDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("dropdown_product_gst")
                            )
                            ExposedDropdownMenu(
                                expanded = gstDropdownExpanded,
                                onDismissRequest = { gstDropdownExpanded = false }
                            ) {
                                STANDARD_GST_RATES.forEach { rate ->
                                    DropdownMenuItem(
                                        text = { Text("${rate.toInt()}% GST") },
                                        onClick = {
                                            gstRate = rate
                                            gstDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 6. Pricing Row: Purchase Price, MRP, Selling Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = purchasePriceStr,
                            onValueChange = { purchasePriceStr = it },
                            label = { Text("Purchase (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_purchase_price"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = mrpStr,
                            onValueChange = { mrpStr = it },
                            label = { Text("MRP (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_mrp"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = sellingPriceStr,
                            onValueChange = { sellingPriceStr = it },
                            label = { Text("Selling (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_selling_price"),
                            singleLine = true
                        )
                    }

                    // MRP Warning & Allow Override Checkbox
                    if (isSellingAboveMrp) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Selling price (₹$sellingPrice) cannot exceed MRP (₹$mrp) unless allowed below.",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allowSellingAboveMrp,
                            onCheckedChange = { allowSellingAboveMrp = it },
                            modifier = Modifier.testTag("checkbox_allow_above_mrp")
                        )
                        Text(
                            text = "Explicitly allow selling price to exceed MRP",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 7. Stock Fields: Opening Stock & Minimum Stock Alert
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = openingStockStr,
                            onValueChange = { openingStockStr = it },
                            label = { Text("Opening Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_opening_stock"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = minStockStr,
                            onValueChange = { minStockStr = it },
                            label = { Text("Min Stock Alert") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_min_stock"),
                            singleLine = true
                        )
                    }

                    // 8. Supplier Name
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Supplier / Distributor Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_product_supplier"),
                        singleLine = true
                    )

                    // 9. Active Status Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Active Status",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isActive) "Available for billing & sales" else "Hidden from billing",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            modifier = Modifier.testTag("switch_product_active")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("button_cancel_product")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    val canSave = name.isNotBlank() && sku.isNotBlank() && (!isSellingAboveMrp)

                    Button(
                        onClick = {
                            val openStock = openingStockStr.toDoubleOrNull() ?: 0.0
                            val productToSave = ProductEntity(
                                id = initialProduct?.id ?: 0L,
                                name = name.trim(),
                                sku = sku.trim().uppercase(),
                                barcode = barcode.trim(),
                                categoryName = selectedCategory,
                                brandName = selectedBrand,
                                unit = selectedUnit,
                                purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 0.0,
                                sellingPrice = sellingPrice,
                                mrp = mrp,
                                minStock = minStockStr.toDoubleOrNull() ?: 5.0,
                                openingStock = openStock,
                                currentStock = if (isEdit) (initialProduct?.currentStock ?: openStock) else openStock,
                                supplier = supplier.trim(),
                                gstRate = gstRate,
                                isActive = isActive,
                                allowSellingAboveMrp = allowSellingAboveMrp
                            )
                            onSave(productToSave, isEdit)
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("button_save_product")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEdit) "Update Product" else "Save Product", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Quick Add Category Dialog
    if (showQuickAddCatDialog) {
        AlertDialog(
            onDismissRequest = { showQuickAddCatDialog = false },
            title = { Text("Add New Category", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = quickCatName,
                    onValueChange = { quickCatName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_quick_cat_name")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quickCatName.isNotBlank()) {
                            onQuickAddCategory(quickCatName.trim())
                            selectedCategory = quickCatName.trim()
                            quickCatName = ""
                            showQuickAddCatDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickAddCatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Quick Add Brand Dialog
    if (showQuickAddBrandDialog) {
        AlertDialog(
            onDismissRequest = { showQuickAddBrandDialog = false },
            title = { Text("Add New Brand", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = quickBrandName,
                    onValueChange = { quickBrandName = it },
                    label = { Text("Brand Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_quick_brand_name")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quickBrandName.isNotBlank()) {
                            onQuickAddBrand(quickBrandName.trim())
                            selectedBrand = quickBrandName.trim()
                            quickBrandName = ""
                            showQuickAddBrandDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickAddBrandDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
