package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.AppDatabase
import com.example.data.FirestoreScanRepository
import com.example.data.ScanHistoryEntity
import com.example.data.ScanRecord
import com.example.forensics.AudioForensicsEngine
import com.example.forensics.AudioForensicResult
import com.example.forensics.ForensicAnalysisReport
import com.example.forensics.ForensicAssessment
import com.example.forensics.ForensicMode
import com.example.forensics.ForensicsEngine
import com.example.forensics.MediaType
import com.example.forensics.SamplePreset
import com.example.forensics.SpectralAnalysis
import com.example.forensics.SpectralResult
import com.example.forensics.VideoForensicsEngine
import com.example.forensics.VideoForensicResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class InspectorPixelInfo(
    val x: Int,
    val y: Int,
    val originalR: Int,
    val originalG: Int,
    val originalB: Int,
    val errorValue: Int,
    val isHotspot: Boolean
)

data class ForensicsUiState(
    val activeTab: MediaType = MediaType.IMAGE,
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val currentReport: ForensicAnalysisReport? = null,
    val selectedMode: ForensicMode = ForensicMode.ELA,
    val compareSliderFraction: Float = 0.5f,
    val isCompareSliderEnabled: Boolean = true,
    val quality: Int = 90,
    val scaleFactor: Int = 20,
    val showElaHelp: Boolean = false,
    val showExifSheet: Boolean = false,
    val inspectorInfo: InspectorPixelInfo? = null,
    val historyList: List<ScanHistoryEntity> = emptyList(),
    val isCloudSynced: Boolean = false,
    val errorMessage: String? = null
)

class ForensicsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.scanHistoryDao()
    private val firestoreRepository = FirestoreScanRepository(application)

    private val _uiState = MutableStateFlow(ForensicsUiState())
    val uiState: StateFlow<ForensicsUiState> = _uiState.asStateFlow()

    private var activeUserId: String? = null
    private var firestoreSyncJob: Job? = null

    val samplePresets = listOf(
        // Image Presets
        SamplePreset(
            id = "preset_camera",
            title = "Authentic Portrait",
            subtitle = "Natural skin texture & camera sensor noise",
            drawableRes = R.drawable.sample_camera_1791447180053,
            typeDescription = "DSLR Camera Photo",
            mediaType = MediaType.IMAGE
        ),
        SamplePreset(
            id = "preset_ai",
            title = "Synthetic AI Face",
            subtitle = "Diffusion model with 2D FFT spectral peaks",
            drawableRes = R.drawable.sample_ai_1791447200538,
            typeDescription = "AI Generated Portrait",
            mediaType = MediaType.IMAGE
        ),
        SamplePreset(
            id = "preset_spliced",
            title = "Spliced Composite",
            subtitle = "Pasted object with mismatched compression",
            drawableRes = R.drawable.sample_spliced_1791447215760,
            typeDescription = "Photo Manipulation",
            mediaType = MediaType.IMAGE
        ),

        // Video Presets
        SamplePreset(
            id = "preset_video_ai",
            title = "Deepfake Face Swap (Video)",
            subtitle = "Temporal flickering & boundary warping across frames",
            drawableRes = R.drawable.sample_ai_1791447200538,
            typeDescription = "AI Video Generation",
            mediaType = MediaType.VIDEO
        ),
        SamplePreset(
            id = "preset_video_real",
            title = "Camera Capture (Video)",
            subtitle = "Consistent optical sensor noise and smooth vectors",
            drawableRes = R.drawable.sample_camera_1791447180053,
            typeDescription = "Real Recorded Video",
            mediaType = MediaType.VIDEO
        ),

        // Audio Presets
        SamplePreset(
            id = "preset_audio_tts",
            title = "AI Voice Clone (TTS)",
            subtitle = "11kHz brickwall frequency cutoff & zero digital silences",
            drawableRes = R.drawable.forensics_banner_1791447063909,
            typeDescription = "Neural Voice Synthesis",
            mediaType = MediaType.AUDIO
        ),
        SamplePreset(
            id = "preset_audio_real",
            title = "Acoustic Voice Recording",
            subtitle = "Room ambiance floor & natural 20kHz harmonic spectrum",
            drawableRes = R.drawable.sample_camera_1791447180053,
            typeDescription = "Authentic Microphone",
            mediaType = MediaType.AUDIO
        )
    )

    private var activeRawBitmap: Bitmap? = null

    init {
        viewModelScope.launch {
            dao.getAllScans().collect { scans ->
                _uiState.update { it.copy(historyList = scans) }
            }
        }
    }

    fun setAuthenticatedUser(userId: String?) {
        if (activeUserId == userId) return
        activeUserId = userId
        firestoreSyncJob?.cancel()

        if (userId != null && userId.isNotBlank()) {
            _uiState.update { it.copy(isCloudSynced = true) }
            firestoreSyncJob = viewModelScope.launch {
                firestoreRepository.observeUserScans(userId).collect { cloudScans ->
                    // Sync cloud scans to local cache
                    for (cs in cloudScans) {
                        dao.insertScan(
                            ScanHistoryEntity(
                                id = cs.id,
                                timestamp = cs.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                                imageLabel = cs.imageLabel,
                                syntheticRiskScore = cs.syntheticRiskScore.toInt(),
                                riskLevel = cs.riskLevel,
                                elaScore = cs.elaScore.toInt(),
                                summary = cs.summary,
                                isPreset = cs.isPreset
                            )
                        )
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isCloudSynced = false) }
        }
    }

    fun setActiveTab(mediaType: MediaType) {
        _uiState.update { it.copy(activeTab = mediaType) }
    }

    fun analyzeImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Decoding image securely on-device...",
                    errorMessage = null,
                    inspectorInfo = null
                )
            }
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val stream = context.contentResolver.openInputStream(uri)
                    BitmapFactory.decodeStream(stream)
                } ?: throw IllegalArgumentException("Could not decode image file")

                val normalized = ForensicsEngine.getNormalizedBitmap(bitmap)
                activeRawBitmap = normalized

                val exif = ForensicsEngine.extractExifData(context, uri)
                performImageForensics(
                    bitmap = normalized,
                    uriString = uri.toString(),
                    presetId = null,
                    label = "Gallery Image",
                    exif = exif
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Analysis error: ${e.localizedMessage ?: "Unknown failure"}"
                    )
                }
            }
        }
    }

    fun analyzeCapturedBitmap(context: Context, bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Processing CameraX capture through forensic pipeline...",
                    errorMessage = null,
                    inspectorInfo = null
                )
            }
            try {
                val normalized = ForensicsEngine.getNormalizedBitmap(bitmap)
                activeRawBitmap = normalized

                val exif = com.example.forensics.ExifMetadata(
                    cameraMake = "Live Device Camera",
                    cameraModel = "CameraX Optical Sensor",
                    dateTime = java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss", java.util.Locale.US).format(java.util.Date()),
                    software = "Android CameraX Engine",
                    hasGps = false,
                    isSuspiciousSoftware = false
                )

                performImageForensics(
                    bitmap = normalized,
                    uriString = null,
                    presetId = null,
                    label = "Camera Capture",
                    exif = exif
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Camera capture analysis error: ${e.localizedMessage ?: "Unknown failure"}"
                    )
                }
            }
        }
    }

    fun analyzeVideoUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Sampling video frames across timeline...",
                    errorMessage = null,
                    inspectorInfo = null
                )
            }
            try {
                val videoResult = VideoForensicsEngine.analyzeVideo(context, uri)
                val dummyEla = ForensicsEngine.runErrorLevelAnalysis(videoResult.sampledFrames.first())

                val reportId = UUID.randomUUID().toString()
                val assessment = ForensicAssessment(
                    syntheticRiskScore = videoResult.syntheticRiskScore,
                    riskLevel = videoResult.riskLevel,
                    summary = videoResult.summary,
                    findings = videoResult.findings,
                    elaScore = (videoResult.backgroundMorphingScore * 100).toInt(),
                    noiseScore = (videoResult.temporalNoiseVariance * 5).toInt().coerceIn(0, 100),
                    edgeScore = if (videoResult.faceBoundaryAnomaly) 75 else 20,
                    metadataScore = 20
                )

                val vBytes = java.io.ByteArrayOutputStream().also {
                    videoResult.sampledFrames.first().compress(Bitmap.CompressFormat.PNG, 100, it)
                }.toByteArray()

                val report = ForensicAnalysisReport(
                    id = reportId,
                    timestamp = System.currentTimeMillis(),
                    mediaType = MediaType.VIDEO,
                    imageUriString = uri.toString(),
                    presetId = null,
                    originalBitmap = videoResult.sampledFrames.first(),
                    elaResult = dummyEla,
                    noiseResult = ForensicsEngine.runNoiseAnalysis(videoResult.sampledFrames.first()),
                    edgeResult = ForensicsEngine.runEdgeGradientAnalysis(videoResult.sampledFrames.first()),
                    exifMetadata = com.example.forensics.ExifMetadata(software = "Video Stream", compressionType = "MPEG/H.264"),
                    assessment = assessment,
                    qualityUsed = 90,
                    scaleUsed = 20,
                    sha256Hash = com.example.forensics.CryptoUtil.computeSha256(vBytes),
                    md5Hash = com.example.forensics.CryptoUtil.computeMd5(vBytes),
                    videoResult = videoResult
                )

                persistScanRecord(
                    id = reportId,
                    mediaType = MediaType.VIDEO,
                    label = "Video File (${videoResult.width}x${videoResult.height})",
                    score = assessment.syntheticRiskScore,
                    riskLevel = assessment.riskLevel.name,
                    elaScore = assessment.elaScore,
                    summary = assessment.summary,
                    isPreset = false
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentReport = report,
                        selectedMode = ForensicMode.ORIGINAL
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Video analysis error: ${e.localizedMessage ?: "Unknown failure"}"
                    )
                }
            }
        }
    }

    fun analyzeAudioUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Decoding audio into PCM spectrogram...",
                    errorMessage = null,
                    inspectorInfo = null
                )
            }
            try {
                val (samples, sampleRate) = AudioForensicsEngine.decodeAudio(context, uri)
                val audioResult = AudioForensicsEngine.analyzeAudio(samples, sampleRate)

                val dummyEla = ForensicsEngine.runErrorLevelAnalysis(audioResult.spectrogramBitmap)
                val reportId = UUID.randomUUID().toString()

                val assessment = ForensicAssessment(
                    syntheticRiskScore = audioResult.syntheticRiskScore,
                    riskLevel = audioResult.riskLevel,
                    summary = audioResult.summary,
                    findings = audioResult.findings,
                    elaScore = if (audioResult.cutoffKhz < 14f) 85 else 15,
                    noiseScore = if (audioResult.deadSilenceRatio > 0.08f) 80 else 20,
                    edgeScore = if (audioResult.vocoderArtifactDetected) 75 else 25,
                    metadataScore = 20
                )

                val aBytes = java.io.ByteArrayOutputStream().also {
                    audioResult.spectrogramBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }.toByteArray()

                val report = ForensicAnalysisReport(
                    id = reportId,
                    timestamp = System.currentTimeMillis(),
                    mediaType = MediaType.AUDIO,
                    imageUriString = uri.toString(),
                    presetId = null,
                    originalBitmap = audioResult.spectrogramBitmap,
                    elaResult = dummyEla,
                    noiseResult = ForensicsEngine.runNoiseAnalysis(audioResult.spectrogramBitmap),
                    edgeResult = ForensicsEngine.runEdgeGradientAnalysis(audioResult.spectrogramBitmap),
                    exifMetadata = com.example.forensics.ExifMetadata(software = "Audio Stream", compressionType = "PCM/${sampleRate}Hz"),
                    assessment = assessment,
                    qualityUsed = 90,
                    scaleUsed = 20,
                    sha256Hash = com.example.forensics.CryptoUtil.computeSha256(aBytes),
                    md5Hash = com.example.forensics.CryptoUtil.computeMd5(aBytes),
                    audioResult = audioResult
                )

                persistScanRecord(
                    id = reportId,
                    mediaType = MediaType.AUDIO,
                    label = "Audio Track (${String.format("%.1f", audioResult.durationSeconds)}s)",
                    score = assessment.syntheticRiskScore,
                    riskLevel = assessment.riskLevel.name,
                    elaScore = assessment.elaScore,
                    summary = assessment.summary,
                    isPreset = false
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentReport = report,
                        selectedMode = ForensicMode.ORIGINAL
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Audio analysis error: ${e.localizedMessage ?: "Unknown failure"}"
                    )
                }
            }
        }
    }

    fun analyzePreset(context: Context, preset: SamplePreset) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Loading demo sample for analysis...",
                    errorMessage = null,
                    inspectorInfo = null
                )
            }
            try {
                when (preset.mediaType) {
                    MediaType.IMAGE -> {
                        val bitmap = withContext(Dispatchers.IO) {
                            BitmapFactory.decodeResource(context.resources, preset.drawableRes)
                        } ?: throw IllegalArgumentException("Failed to decode sample resource")

                        val normalized = ForensicsEngine.getNormalizedBitmap(bitmap)
                        activeRawBitmap = normalized

                        val exif = ForensicsEngine.extractExifData(context, null)
                        performImageForensics(
                            bitmap = normalized,
                            uriString = null,
                            presetId = preset.id,
                            label = preset.title,
                            exif = exif
                        )
                    }

                    MediaType.VIDEO -> {
                        val bitmap = withContext(Dispatchers.IO) {
                            BitmapFactory.decodeResource(context.resources, preset.drawableRes)
                        } ?: throw IllegalArgumentException("Failed to decode video demo resource")

                        val normalized = ForensicsEngine.getNormalizedBitmap(bitmap)
                        val sampleFrames = mutableListOf<Bitmap>()
                        val isFake = preset.id == "preset_video_ai"

                        for (i in 0..4) {
                            val f = normalized.copy(Bitmap.Config.ARGB_8888, true)
                            if (isFake && (i == 2 || i == 3)) {
                                for (y in (f.height * 0.3).toInt() until (f.height * 0.6).toInt()) {
                                    for (x in (f.width * 0.3).toInt() until (f.width * 0.6).toInt()) {
                                        val p = f.getPixel(x, y)
                                        f.setPixel(x, y, Color.rgb(Color.red(p), Color.green(p), (Color.blue(p) * 1.2).toInt().coerceAtMost(255)))
                                    }
                                }
                            }
                            sampleFrames.add(f)
                        }

                        val videoResult = VideoForensicsEngine.analyzeVideo(context, null).copy(
                            sampledFrames = sampleFrames,
                            syntheticRiskScore = if (isFake) 88 else 18,
                            riskLevel = if (isFake) com.example.forensics.RiskLevel.HIGH else com.example.forensics.RiskLevel.LOW,
                            backgroundMorphingScore = if (isFake) 0.82f else 0.15f,
                            faceBoundaryAnomaly = isFake,
                            summary = if (isFake) "High probability of AI-generated video or deepfake face swap detected through temporal warping and boundary flickering." else "Consistent temporal continuity, uniform inter-frame sensor noise, and stable facial edges."
                        )

                        val dummyEla = ForensicsEngine.runErrorLevelAnalysis(sampleFrames.first())
                        val reportId = UUID.randomUUID().toString()

                        val assessment = ForensicAssessment(
                            syntheticRiskScore = videoResult.syntheticRiskScore,
                            riskLevel = videoResult.riskLevel,
                            summary = videoResult.summary,
                            findings = videoResult.findings,
                            elaScore = if (isFake) 85 else 15,
                            noiseScore = if (isFake) 75 else 20,
                            edgeScore = if (isFake) 80 else 15,
                            metadataScore = 20
                        )

                        val report = ForensicAnalysisReport(
                            id = reportId,
                            timestamp = System.currentTimeMillis(),
                            mediaType = MediaType.VIDEO,
                            imageUriString = null,
                            presetId = preset.id,
                            originalBitmap = sampleFrames.first(),
                            elaResult = dummyEla,
                            noiseResult = ForensicsEngine.runNoiseAnalysis(sampleFrames.first()),
                            edgeResult = ForensicsEngine.runEdgeGradientAnalysis(sampleFrames.first()),
                            exifMetadata = com.example.forensics.ExifMetadata(software = "Video Demonstration", compressionType = "MPEG/H.264"),
                            assessment = assessment,
                            qualityUsed = 90,
                            scaleUsed = 20,
                            videoResult = videoResult
                        )

                        persistScanRecord(
                            id = reportId,
                            mediaType = MediaType.VIDEO,
                            label = preset.title,
                            score = assessment.syntheticRiskScore,
                            riskLevel = assessment.riskLevel.name,
                            elaScore = assessment.elaScore,
                            summary = assessment.summary,
                            isPreset = true
                        )

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentReport = report,
                                selectedMode = ForensicMode.ORIGINAL
                            )
                        }
                    }

                    MediaType.AUDIO -> {
                        val isFake = preset.id == "preset_audio_tts"
                        val (samples, sampleRate) = if (isFake) {
                            AudioForensicsEngine.generateSyntheticAiSample()
                        } else {
                            AudioForensicsEngine.generateAuthenticSample()
                        }

                        val audioResult = AudioForensicsEngine.analyzeAudio(samples, sampleRate)
                        val dummyEla = ForensicsEngine.runErrorLevelAnalysis(audioResult.spectrogramBitmap)
                        val reportId = UUID.randomUUID().toString()

                        val assessment = ForensicAssessment(
                            syntheticRiskScore = audioResult.syntheticRiskScore,
                            riskLevel = audioResult.riskLevel,
                            summary = audioResult.summary,
                            findings = audioResult.findings,
                            elaScore = if (isFake) 85 else 15,
                            noiseScore = if (isFake) 80 else 20,
                            edgeScore = if (isFake) 75 else 25,
                            metadataScore = 20
                        )

                        val report = ForensicAnalysisReport(
                            id = reportId,
                            timestamp = System.currentTimeMillis(),
                            mediaType = MediaType.AUDIO,
                            imageUriString = null,
                            presetId = preset.id,
                            originalBitmap = audioResult.spectrogramBitmap,
                            elaResult = dummyEla,
                            noiseResult = ForensicsEngine.runNoiseAnalysis(audioResult.spectrogramBitmap),
                            edgeResult = ForensicsEngine.runEdgeGradientAnalysis(audioResult.spectrogramBitmap),
                            exifMetadata = com.example.forensics.ExifMetadata(software = "Audio Demonstration", compressionType = "PCM/${sampleRate}Hz"),
                            assessment = assessment,
                            qualityUsed = 90,
                            scaleUsed = 20,
                            audioResult = audioResult
                        )

                        persistScanRecord(
                            id = reportId,
                            mediaType = MediaType.AUDIO,
                            label = preset.title,
                            score = assessment.syntheticRiskScore,
                            riskLevel = assessment.riskLevel.name,
                            elaScore = assessment.elaScore,
                            summary = assessment.summary,
                            isPreset = true
                        )

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currentReport = report,
                                selectedMode = ForensicMode.ORIGINAL
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Preset error: ${e.localizedMessage ?: "Unknown failure"}"
                    )
                }
            }
        }
    }

    private suspend fun performImageForensics(
        bitmap: Bitmap,
        uriString: String?,
        presetId: String?,
        label: String,
        exif: com.example.forensics.ExifMetadata
    ) {
        val q = _uiState.value.quality
        val s = _uiState.value.scaleFactor

        _uiState.update { it.copy(loadingMessage = "Computing Error Level Analysis (ELA $q% @ ${s}x)...") }
        val ela = ForensicsEngine.runErrorLevelAnalysis(bitmap, quality = q, scaleFactor = s)

        _uiState.update { it.copy(loadingMessage = "Extracting Laplacian sensor noise residuals...") }
        val noise = ForensicsEngine.runNoiseAnalysis(bitmap)

        _uiState.update { it.copy(loadingMessage = "Computing 2D Fourier (FFT) Spectral Analysis...") }
        val spectral = SpectralAnalysis.compute2dFft(bitmap)

        _uiState.update { it.copy(loadingMessage = "Measuring edge gradients and seam contours...") }
        val edge = ForensicsEngine.runEdgeGradientAnalysis(bitmap)

        _uiState.update { it.copy(loadingMessage = "Synthesizing multi-layer forensic markers...") }
        val assessment = ForensicsEngine.calculateAssessment(ela, noise, edge, exif, spectral)

        val byteOut = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteOut)
        val imgBytes = byteOut.toByteArray()
        val sha256 = com.example.forensics.CryptoUtil.computeSha256(imgBytes)
        val md5 = com.example.forensics.CryptoUtil.computeMd5(imgBytes)

        val reportId = UUID.randomUUID().toString()
        val report = ForensicAnalysisReport(
            id = reportId,
            timestamp = System.currentTimeMillis(),
            mediaType = MediaType.IMAGE,
            imageUriString = uriString,
            presetId = presetId,
            originalBitmap = bitmap,
            elaResult = ela,
            noiseResult = noise,
            edgeResult = edge,
            exifMetadata = exif,
            assessment = assessment,
            qualityUsed = q,
            scaleUsed = s,
            sha256Hash = sha256,
            md5Hash = md5,
            spectralResult = spectral
        )

        persistScanRecord(
            id = reportId,
            mediaType = MediaType.IMAGE,
            label = label,
            score = assessment.syntheticRiskScore,
            riskLevel = assessment.riskLevel.name,
            elaScore = assessment.elaScore,
            summary = assessment.summary,
            isPreset = presetId != null
        )

        _uiState.update {
            it.copy(
                isLoading = false,
                currentReport = report,
                selectedMode = ForensicMode.ELA
            )
        }
    }

    private fun persistScanRecord(
        id: String,
        mediaType: MediaType,
        label: String,
        score: Int,
        riskLevel: String,
        elaScore: Int,
        summary: String,
        isPreset: Boolean
    ) {
        viewModelScope.launch {
            // Local Room Cache
            dao.insertScan(
                ScanHistoryEntity(
                    id = id,
                    timestamp = System.currentTimeMillis(),
                    imageLabel = label,
                    syntheticRiskScore = score,
                    riskLevel = riskLevel,
                    elaScore = elaScore,
                    summary = summary,
                    isPreset = isPreset
                )
            )

            // Cloud Firestore Persistence
            val uid = activeUserId
            if (!uid.isNullOrBlank()) {
                firestoreRepository.saveScan(
                    ScanRecord(
                        id = id,
                        userId = uid,
                        mediaType = mediaType.name,
                        imageLabel = label,
                        syntheticRiskScore = score.toLong(),
                        riskLevel = riskLevel,
                        elaScore = elaScore.toLong(),
                        summary = summary,
                        isPreset = isPreset
                    )
                )
            }
        }
    }

    fun updateParameters(quality: Int, scaleFactor: Int) {
        val raw = activeRawBitmap ?: return
        val report = _uiState.value.currentReport ?: return

        _uiState.update {
            it.copy(
                quality = quality,
                scaleFactor = scaleFactor,
                isLoading = true,
                loadingMessage = "Re-analyzing at $quality% quality with ${scaleFactor}x gain..."
            )
        }

        viewModelScope.launch {
            try {
                val newEla = ForensicsEngine.runErrorLevelAnalysis(raw, quality, scaleFactor)
                val newAssessment = ForensicsEngine.calculateAssessment(
                    newEla,
                    report.noiseResult,
                    report.edgeResult,
                    report.exifMetadata,
                    report.spectralResult
                )

                val updatedReport = report.copy(
                    elaResult = newEla,
                    assessment = newAssessment,
                    qualityUsed = quality,
                    scaleUsed = scaleFactor
                )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentReport = updatedReport
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Parameter recalculation failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun setForensicMode(mode: ForensicMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun setCompareFraction(fraction: Float) {
        _uiState.update { it.copy(compareSliderFraction = fraction.coerceIn(0f, 1f)) }
    }

    fun toggleCompareSlider() {
        _uiState.update { it.copy(isCompareSliderEnabled = !it.isCompareSliderEnabled) }
    }

    fun inspectPoint(normalizedX: Float, normalizedY: Float) {
        val report = _uiState.value.currentReport ?: return
        val orig = report.originalBitmap
        val ela = report.elaResult.elaBitmap

        val px = (normalizedX * orig.width).toInt().coerceIn(0, orig.width - 1)
        val py = (normalizedY * orig.height).toInt().coerceIn(0, orig.height - 1)

        val origColor = orig.getPixel(px, py)
        val elaColor = ela.getPixel(px, py)

        val r = Color.red(origColor)
        val g = Color.green(origColor)
        val b = Color.blue(origColor)

        val err = (Color.red(elaColor) + Color.green(elaColor) + Color.blue(elaColor)) / 3
        val isHotspot = err > 90

        _uiState.update {
            it.copy(
                inspectorInfo = InspectorPixelInfo(
                    x = px,
                    y = py,
                    originalR = r,
                    originalG = g,
                    originalB = b,
                    errorValue = err,
                    isHotspot = isHotspot
                )
            )
        }
    }

    fun clearInspector() {
        _uiState.update { it.copy(inspectorInfo = null) }
    }

    fun setElaHelpVisible(visible: Boolean) {
        _uiState.update { it.copy(showElaHelp = visible) }
    }

    fun setExifSheetVisible(visible: Boolean) {
        _uiState.update { it.copy(showExifSheet = visible) }
    }

    fun deleteScan(id: String) {
        viewModelScope.launch {
            dao.deleteScan(id)
            val uid = activeUserId
            if (!uid.isNullOrBlank()) {
                firestoreRepository.deleteScan(uid, id)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }

    fun resetToHome() {
        _uiState.update {
            it.copy(
                currentReport = null,
                inspectorInfo = null,
                errorMessage = null
            )
        }
    }
}
