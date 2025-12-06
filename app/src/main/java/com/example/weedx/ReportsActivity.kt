package com.example.weedx

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weedx.presentation.viewmodels.ReportsViewModel
import com.example.weedx.presentation.viewmodels.ReportsViewModel.Period
import com.example.weedx.presentation.viewmodels.ReportsViewModel.ReportsState
import com.example.weedx.presentation.viewmodels.ReportsViewModel.TrendState
import com.example.weedx.presentation.viewmodels.ReportsViewModel.ExportState
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@AndroidEntryPoint
class ReportsActivity : AppCompatActivity() {

    private val viewModel: ReportsViewModel by viewModels()

    // UI Elements
    private lateinit var weeklyTab: TextView
    private lateinit var monthlyTab: TextView
    private lateinit var trendChartRecyclerView: RecyclerView
    private lateinit var distributionRecyclerView: RecyclerView
    private lateinit var downloadButton: CardView

    // Widget TextViews
    private lateinit var totalWeedsValue: TextView
    private lateinit var areaCoveredValue: TextView
    private lateinit var herbicideUsedValue: TextView
    private lateinit var efficiencyValue: TextView

    // Loading/Error views
    private lateinit var progressBar: ProgressBar
    private lateinit var errorLayout: View
    private lateinit var errorMessage: TextView
    private lateinit var retryButton: MaterialButton
    private lateinit var contentLayout: View

    // Adapters
    private lateinit var trendAdapter: TrendChartAdapter
    private lateinit var distributionAdapter: WeedDistributionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_reports)
        
        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupAdapters()
        setupTabs()
        setupDownloadButton()
        setupBottomNavigation()
        observeState()
        
        // Load data
        viewModel.loadReports()
        viewModel.loadTrend()
    }

    private fun initViews() {
        weeklyTab = findViewById(R.id.weeklyTab)
        monthlyTab = findViewById(R.id.monthlyTab)
        trendChartRecyclerView = findViewById(R.id.trendChartRecyclerView)
        distributionRecyclerView = findViewById(R.id.distributionRecyclerView)
        downloadButton = findViewById(R.id.downloadButton)

        // Widgets
        totalWeedsValue = findViewById(R.id.totalWeedsValue)
        areaCoveredValue = findViewById(R.id.areaCoveredValue)
        herbicideUsedValue = findViewById(R.id.herbicideUsedValue)
        efficiencyValue = findViewById(R.id.efficiencyValue)

        // Loading/Error
        progressBar = findViewById(R.id.progressBar)
        errorLayout = findViewById(R.id.errorLayout)
        errorMessage = findViewById(R.id.errorMessage)
        retryButton = findViewById(R.id.retryButton)
        contentLayout = findViewById(R.id.contentLayout)

        retryButton.setOnClickListener {
            viewModel.refresh()
        }
    }

    private fun setupAdapters() {
        trendAdapter = TrendChartAdapter()
        trendChartRecyclerView.layoutManager = LinearLayoutManager(this)
        trendChartRecyclerView.adapter = trendAdapter

        distributionAdapter = WeedDistributionAdapter()
        distributionRecyclerView.layoutManager = LinearLayoutManager(this)
        distributionRecyclerView.adapter = distributionAdapter
    }

    private fun setupTabs() {
        weeklyTab.setOnClickListener {
            viewModel.selectPeriod(Period.WEEKLY)
        }

        monthlyTab.setOnClickListener {
            viewModel.selectPeriod(Period.MONTHLY)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is ReportsState.Idle -> {
                        // Initial state
                    }
                    is ReportsState.Loading -> {
                        showLoading()
                    }
                    is ReportsState.Success -> {
                        showContent()
                        updateWidgets(state.data.widgets)
                        updateDistribution()
                    }
                    is ReportsState.Error -> {
                        showError(state.message)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.trendState.collectLatest { state ->
                when (state) {
                    is TrendState.Idle -> {}
                    is TrendState.Loading -> {
                        // Optionally show trend-specific loading
                    }
                    is TrendState.Success -> {
                        trendAdapter.submitListWithMax(state.data)
                    }
                    is TrendState.Error -> {
                        Toast.makeText(this@ReportsActivity, "Failed to load trend: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.exportState.collectLatest { state ->
                when (state) {
                    is ExportState.Idle -> {}
                    is ExportState.Loading -> {
                        Toast.makeText(this@ReportsActivity, "Generating report...", Toast.LENGTH_SHORT).show()
                    }
                    is ExportState.Success -> {
                        downloadReport(state.data.downloadUrl, state.data.filename)
                        viewModel.resetExportState()
                    }
                    is ExportState.Error -> {
                        Toast.makeText(this@ReportsActivity, "Export failed: ${state.message}", Toast.LENGTH_SHORT).show()
                        viewModel.resetExportState()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.selectedPeriod.collectLatest { period ->
                updateTabSelection(period)
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
        contentLayout.visibility = View.GONE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        errorLayout.visibility = View.GONE
        contentLayout.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        contentLayout.visibility = View.GONE
        errorMessage.text = message
    }

    private fun updateWidgets(widgets: com.example.weedx.data.models.response.ReportWidgets) {
        totalWeedsValue.text = viewModel.formatNumber(widgets.totalWeeds)
        areaCoveredValue.text = viewModel.formatArea(widgets.areaCovered)
        herbicideUsedValue.text = viewModel.formatHerbicide(widgets.herbicideUsed)
        efficiencyValue.text = viewModel.formatEfficiency(widgets.efficiency)
    }

    private fun updateDistribution() {
        val distribution = viewModel.getDistributionByWeedType()
        val items = WeedDistributionAdapter.createFromDistribution(distribution)
        distributionAdapter.submitList(items)
    }

    private fun updateTabSelection(period: Period) {
        when (period) {
            Period.WEEKLY -> {
                weeklyTab.setBackgroundResource(R.drawable.tab_selected_green)
                weeklyTab.setTextColor(Color.WHITE)
                monthlyTab.background = null
                monthlyTab.setTextColor(Color.BLACK)
            }
            Period.MONTHLY -> {
                monthlyTab.setBackgroundResource(R.drawable.tab_selected_green)
                monthlyTab.setTextColor(Color.WHITE)
                weeklyTab.background = null
                weeklyTab.setTextColor(Color.BLACK)
            }
        }
    }

    private fun setupDownloadButton() {
        downloadButton.setOnClickListener {
            showExportFormatDialog()
        }
    }

    private fun showExportFormatDialog() {
        val formats = arrayOf("CSV (Spreadsheet)", "PDF (Document)")
        AlertDialog.Builder(this)
            .setTitle("Export Format")
            .setItems(formats) { _, which ->
                when (which) {
                    0 -> viewModel.exportReport("csv")
                    1 -> viewModel.exportReport("pdf")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun downloadReport(downloadUrl: String, filename: String) {
        try {
            // Determine MIME type and data prefix based on file extension
            when {
                filename.endsWith(".csv") -> {
                    // Handle CSV download
                    if (downloadUrl.startsWith("data:text/csv;base64,")) {
                        val base64Data = downloadUrl.substring("data:text/csv;base64,".length)
                        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                        saveFileToDownloads(decodedBytes, filename, "text/csv")
                    } else {
                        downloadWithDownloadManager(downloadUrl, filename, "text/csv")
                    }
                }
                filename.endsWith(".pdf") -> {
                    // Handle PDF generation from HTML
                    if (downloadUrl.startsWith("data:text/html;base64,")) {
                        val base64Data = downloadUrl.substring("data:text/html;base64,".length)
                        val htmlContent = String(Base64.decode(base64Data, Base64.DEFAULT))
                        convertHtmlToPdf(htmlContent, filename)
                    } else if (downloadUrl.startsWith("data:application/pdf;base64,")) {
                        val base64Data = downloadUrl.substring("data:application/pdf;base64,".length)
                        val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                        saveFileToDownloads(decodedBytes, filename, "application/pdf")
                    } else {
                        downloadWithDownloadManager(downloadUrl, filename, "application/pdf")
                    }
                }
                else -> {
                    Toast.makeText(this, "Unsupported file format", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to download: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveFileToDownloads(data: ByteArray, filename: String, mimeType: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(data)
                    }
                    Toast.makeText(this, "Report saved to Downloads/$filename", Toast.LENGTH_LONG).show()
                } ?: run {
                    Toast.makeText(this, "Failed to create file", Toast.LENGTH_LONG).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val file = File(downloadsDir, filename)
                FileOutputStream(file).use { it.write(data) }
                Toast.makeText(this, "Report saved to Downloads/$filename", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun downloadWithDownloadManager(url: String, filename: String, mimeType: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("WeedX Report")
            .setDescription("Downloading report...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
            .setMimeType(mimeType)
        
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(this, "Downloading report...", Toast.LENGTH_SHORT).show()
    }

    private fun convertHtmlToPdf(htmlContent: String, filename: String) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint()
            
            // Draw title
            paint.textSize = 24f
            paint.color = android.graphics.Color.parseColor("#2d5016")
            paint.isFakeBoldText = true
            canvas.drawText("WeedX Detection Report", 50f, 50f, paint)
            
            // Draw timestamp
            paint.textSize = 12f
            paint.color = android.graphics.Color.GRAY
            paint.isFakeBoldText = false
            val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            canvas.drawText("Generated: $dateStr", 50f, 80f, paint)
            
            // Draw table header
            var yPos = 120f
            paint.textSize = 10f
            paint.color = android.graphics.Color.BLACK
            paint.isFakeBoldText = true
            
            canvas.drawText("ID", 50f, yPos, paint)
            canvas.drawText("Weed Type", 90f, yPos, paint)
            canvas.drawText("Crop", 200f, yPos, paint)
            canvas.drawText("Conf.", 280f, yPos, paint)
            canvas.drawText("Treated", 340f, yPos, paint)
            canvas.drawText("Date", 420f, yPos, paint)
            
            // Draw line under header
            yPos += 5f
            paint.strokeWidth = 2f
            canvas.drawLine(50f, yPos, pageWidth - 50f, yPos, paint)
            yPos += 15f
            
            // Parse HTML to extract table data
            paint.isFakeBoldText = false
            paint.textSize = 9f
            paint.strokeWidth = 1f
            
            val rows = htmlContent.split("<tr>").drop(2) // Skip header rows
            
            for (row in rows) {
                if (row.contains("</tr>") && row.contains("<td>")) {
                    val cells = row.split("<td>")
                        .filter { it.contains("</td>") }
                        .map { it.substringBefore("</td>").replace("&nbsp;", " ").trim() }
                    
                    if (cells.size >= 7) {
                        // Check if we need a new page
                        if (yPos > pageHeight - 50) {
                            pdfDocument.finishPage(page)
                            pageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            yPos = 50f
                            
                            // Redraw header on new page
                            paint.isFakeBoldText = true
                            canvas.drawText("ID", 50f, yPos, paint)
                            canvas.drawText("Weed Type", 90f, yPos, paint)
                            canvas.drawText("Crop", 200f, yPos, paint)
                            canvas.drawText("Conf.", 280f, yPos, paint)
                            canvas.drawText("Treated", 340f, yPos, paint)
                            canvas.drawText("Date", 420f, yPos, paint)
                            yPos += 5f
                            canvas.drawLine(50f, yPos, pageWidth - 50f, yPos, paint)
                            yPos += 15f
                            paint.isFakeBoldText = false
                        }
                        
                        canvas.drawText(cells[0], 50f, yPos, paint) // ID
                        canvas.drawText(cells[1].take(14), 90f, yPos, paint) // Weed Type
                        canvas.drawText(cells[2].take(10), 200f, yPos, paint) // Crop
                        canvas.drawText(cells[3], 280f, yPos, paint) // Confidence
                        canvas.drawText(cells[5], 340f, yPos, paint) // Treated
                        canvas.drawText(cells[6].take(19), 420f, yPos, paint) // Date
                        
                        yPos += 20f
                    }
                }
            }
            
            // Footer
            paint.textSize = 10f
            paint.color = android.graphics.Color.LTGRAY
            canvas.drawText("WeedX - Precision Farming Report", 50f, pageHeight - 30f, paint)
            
            pdfDocument.finishPage(page)
            
            // Save PDF
            val pdfBytes = java.io.ByteArrayOutputStream()
            pdfDocument.writeTo(pdfBytes)
            pdfDocument.close()
            
            saveFileToDownloads(pdfBytes.toByteArray(), filename, "application/pdf")
        } catch (e: Exception) {
            Toast.makeText(this, "PDF generation failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav?.let { nav ->
            nav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_weather -> {
                        startActivity(Intent(this, LiveMonitoringActivity::class.java))
                        finish()
                        true
                    }
                    R.id.nav_profile -> {
                        startActivity(Intent(this, ProfileActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
        }
    }
}
