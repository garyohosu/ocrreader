package com.garyohosu.ocrreader

import com.garyohosu.ocrreader.domain.ScanPhase
import com.garyohosu.ocrreader.domain.ScanResult
import com.garyohosu.ocrreader.domain.SoundEvent
import com.garyohosu.ocrreader.viewmodel.ScanViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // ── 初期状態 ────────────────────────────────────────────────

    @Test
    fun tc_vm_001_initialState() {
        val vm = ScanViewModel()
        val s = vm.state.value
        assertEquals(ScanPhase.IDLE, s.phase)
        assertNull(s.ocr1)
        assertNull(s.ocr2)
        assertNull(s.result)
        assertNull(s.errorMessage)
        assertFalse(s.permissionDenied)
    }

    // ── フェーズ遷移 ────────────────────────────────────────────

    @Test
    fun tc_vm_002_onScanStart_movesToWaitingForFirst() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart()
        runCurrent()
        assertEquals(ScanPhase.WAITING_FOR_FIRST, vm.state.value.phase)
    }

    @Test
    fun tc_vm_003_firstValidScan_savesOcr1AndMovesToConfirmingFirst() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        assertEquals("ABC", vm.state.value.ocr1)
        assertEquals(ScanPhase.CONFIRMING_FIRST, vm.state.value.phase)
    }

    @Test
    fun tc_vm_004_secondValidScan_matchOk() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        assertEquals("ABC", vm.state.value.ocr2)
        assertEquals(ScanResult.OK, vm.state.value.result)
        assertEquals(ScanPhase.RESULT, vm.state.value.phase)
    }

    @Test
    fun tc_vm_005_secondValidScan_mismatchNg() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        vm.onOcrDetected("XYZ"); runCurrent()
        assertEquals("XYZ", vm.state.value.ocr2)
        assertEquals(ScanResult.NG, vm.state.value.result)
        assertEquals(ScanPhase.RESULT, vm.state.value.phase)
    }

    @Test
    fun tc_vm_006_onRetry_resetsToWaitingForFirst() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onRetry(); runCurrent()
        val s = vm.state.value
        assertEquals(ScanPhase.WAITING_FOR_FIRST, s.phase)
        assertNull(s.ocr1)
        assertNull(s.ocr2)
        assertNull(s.result)
    }

    @Test
    fun tc_vm_007_onCancel_fromScan_resetsToIdle() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onCancel(); runCurrent()
        val s = vm.state.value
        assertEquals(ScanPhase.IDLE, s.phase)
        assertNull(s.ocr1)
        assertNull(s.ocr2)
        assertNull(s.errorMessage)
    }

    @Test
    fun tc_vm_008_onCancel_fromResult_resetsToIdle() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        vm.onOcrDetected("XYZ"); runCurrent()
        vm.onCancel(); runCurrent()
        assertEquals(ScanPhase.IDLE, vm.state.value.phase)
        assertNull(vm.state.value.result)
    }

    @Test
    fun tc_vm_011_sameValueTwice_isOk() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("SAME"); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        vm.onOcrDetected("SAME"); runCurrent()
        assertEquals(ScanResult.OK, vm.state.value.result)
    }

    // ── 空文字 / null 読み取り ──────────────────────────────────

    @Test
    fun tc_vm_012_nullScan_keepsPhaseAndSetsError() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected(null); runCurrent()
        assertEquals(ScanPhase.WAITING_FOR_FIRST, vm.state.value.phase)
        assertNull(vm.state.value.ocr1)
        assertNotNull(vm.state.value.errorMessage)
    }

    @Test
    fun tc_vm_013_emptyStringScan_keepsPhaseAndSetsError() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected(""); runCurrent()
        assertEquals(ScanPhase.WAITING_FOR_FIRST, vm.state.value.phase)
        assertNull(vm.state.value.ocr1)
        assertNotNull(vm.state.value.errorMessage)
    }

    @Test
    fun tc_vm_014_blankStringScan_keepsPhaseAndSetsError() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("   "); runCurrent()
        assertEquals(ScanPhase.WAITING_FOR_FIRST, vm.state.value.phase)
        assertNull(vm.state.value.ocr1)
        assertNotNull(vm.state.value.errorMessage)
    }

    @Test
    fun tc_vm_015_nullScanInSecondPhase_keepsPhase() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        vm.onOcrDetected(null); runCurrent()
        assertEquals(ScanPhase.WAITING_FOR_SECOND, vm.state.value.phase)
        assertNull(vm.state.value.ocr2)
        assertNotNull(vm.state.value.errorMessage)
    }

    @Test
    fun tc_vm_016_validScanAfterError_clearsErrorMessage() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected(null); runCurrent()
        assertNotNull(vm.state.value.errorMessage)
        vm.onOcrDetected("ABC"); runCurrent()
        assertNull(vm.state.value.errorMessage)
    }

    // ── SoundEvent 発火 ─────────────────────────────────────────

    @Test
    fun tc_vm_017_firstValidScan_emitsBeep() = runTest {
        val vm = ScanViewModel()
        val events = mutableListOf<SoundEvent>()
        val job = launch { vm.soundEvent.collect { events.add(it) } }
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        assertTrue(SoundEvent.BEEP in events)
        job.cancel()
    }

    @Test
    fun tc_vm_018_okScan_emitsBeepThenOk() = runTest {
        val vm = ScanViewModel()
        val events = mutableListOf<SoundEvent>()
        val job = launch { vm.soundEvent.collect { events.add(it) } }
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        assertEquals(listOf(SoundEvent.BEEP, SoundEvent.BEEP, SoundEvent.OK), events)
        job.cancel()
    }

    @Test
    fun tc_vm_019_ngScan_emitsBeepThenNg() = runTest {
        val vm = ScanViewModel()
        val events = mutableListOf<SoundEvent>()
        val job = launch { vm.soundEvent.collect { events.add(it) } }
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        vm.onOcrDetected("XYZ"); runCurrent()
        assertEquals(listOf(SoundEvent.BEEP, SoundEvent.BEEP, SoundEvent.NG), events)
        job.cancel()
    }

    @Test
    fun tc_vm_020_nullScan_emitsNoSoundEvent() = runTest {
        val vm = ScanViewModel()
        val events = mutableListOf<SoundEvent>()
        val job = launch { vm.soundEvent.collect { events.add(it) } }
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected(null); runCurrent()
        assertTrue(events.isEmpty())
        job.cancel()
    }

    // ── 権限・入力ガード ────────────────────────────────────────

    @Test
    fun tc_vm_021_onPermissionDenied_setsFlag() = runTest {
        val vm = ScanViewModel()
        vm.onPermissionDenied(); runCurrent()
        assertTrue(vm.state.value.permissionDenied)
        assertEquals(ScanPhase.IDLE, vm.state.value.phase)
    }

    @Test
    fun tc_vm_022_idleScan_isIgnored() = runTest {
        val vm = ScanViewModel()
        vm.onOcrDetected("ABC"); runCurrent()
        assertEquals(ScanPhase.IDLE, vm.state.value.phase)
        assertNull(vm.state.value.ocr1)
    }

    @Test
    fun tc_vm_023_resultScan_isIgnored() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        assertEquals(ScanPhase.RESULT, vm.state.value.phase)
        vm.onOcrDetected("NEW"); runCurrent()
        assertEquals(ScanPhase.RESULT, vm.state.value.phase)
    }

    @Test
    fun tc_vm_024_onScanStart_clearsDeniedFlag() = runTest {
        val vm = ScanViewModel()
        vm.onPermissionDenied(); runCurrent()
        assertTrue(vm.state.value.permissionDenied)
        vm.onScanStart(); runCurrent()
        assertFalse(vm.state.value.permissionDenied)
        assertEquals(ScanPhase.WAITING_FOR_FIRST, vm.state.value.phase)
    }

    @Test
    fun tc_vm_025_onCancel_clearsDeniedFlag() = runTest {
        val vm = ScanViewModel()
        vm.onPermissionDenied(); runCurrent()
        vm.onCancel(); runCurrent()
        assertFalse(vm.state.value.permissionDenied)
        assertEquals(ScanPhase.IDLE, vm.state.value.phase)
    }

    @Test
    fun tc_vm_026_deniedThenStartThenDeniedAgain() = runTest {
        val vm = ScanViewModel()
        vm.onPermissionDenied(); runCurrent()
        vm.onScanStart(); runCurrent()
        vm.onPermissionDenied(); runCurrent()
        assertTrue(vm.state.value.permissionDenied)
        assertEquals(ScanPhase.IDLE, vm.state.value.phase)
    }

    @Test
    fun tc_vm_027_onConfirmFirst_movesToWaitingForSecond() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("ABC"); runCurrent()
        assertEquals(ScanPhase.CONFIRMING_FIRST, vm.state.value.phase)
        vm.onConfirmFirst(); runCurrent()
        assertEquals(ScanPhase.WAITING_FOR_SECOND, vm.state.value.phase)
    }

    @Test
    fun tc_vm_028_onConfirmFirst_fromOtherPhase_isIgnored() = runTest {
        val vm = ScanViewModel()
        vm.onScanStart(); runCurrent()
        vm.onConfirmFirst(); runCurrent()
        assertEquals(ScanPhase.WAITING_FOR_FIRST, vm.state.value.phase)
    }

    // ── 読み取り数設定 ──────────────────────────────────────────

    @Test
    fun tc_vm_029_initialTargetCount_isZero() {
        val vm = ScanViewModel()
        assertEquals(0, vm.targetCount.value)
    }

    @Test
    fun tc_vm_030_onSaveSettings_updatesTargetCount() = runTest {
        val vm = ScanViewModel()
        vm.onSaveSettings(100, 0, ""); runCurrent()
        assertEquals(100, vm.targetCount.value)
    }

    // ── OCRバリデーション ────────────────────────────────

    @Test
    fun tc_vm_031_wrongLength_setsErrorAndKeepsPhase() = runTest {
        val vm = ScanViewModel()
        vm.onSaveSettings(1, 5, ""); runCurrent()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("AB"); runCurrent()
        assertEquals(ScanPhase.WAITING_FOR_FIRST, vm.state.value.phase)
        assertNotNull(vm.state.value.errorMessage)
        assertTrue(vm.state.value.errorMessage!!.contains("2"))
    }

    @Test
    fun tc_vm_032_wrongHeader_setsErrorAndKeepsPhase() = runTest {
        val vm = ScanViewModel()
        vm.onSaveSettings(1, 0, "FOO"); runCurrent()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("BARXYZ"); runCurrent()
        assertEquals(ScanPhase.WAITING_FOR_FIRST, vm.state.value.phase)
        assertNotNull(vm.state.value.errorMessage)
    }

    @Test
    fun tc_vm_033_correctLengthAndHeader_advancesPhase() = runTest {
        val vm = ScanViewModel()
        vm.onSaveSettings(1, 6, "FOO"); runCurrent()
        vm.onScanStart(); runCurrent()
        vm.onOcrDetected("FOOBAR"); runCurrent()
        assertEquals(ScanPhase.CONFIRMING_FIRST, vm.state.value.phase)
        assertNull(vm.state.value.errorMessage)
    }
}
