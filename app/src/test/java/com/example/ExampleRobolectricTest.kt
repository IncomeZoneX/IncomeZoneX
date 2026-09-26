package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.JsonUtils
import com.example.data.local.LanguageCode
import com.example.data.local.entity.CustomFieldConfig
import com.example.ui.localization.Strings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("IncomeZoneX", appName)
  }

  @Test
  fun `verify translation strings work for both Bangla and English`() {
    val titleEn = Strings.get("app_title", LanguageCode.EN)
    val titleBn = Strings.get("app_title", LanguageCode.BN)

    assertEquals("IncomeZoneX", titleEn)
    assertEquals("ইনকামজোনএক্স", titleBn)

    val withdrawEn = Strings.get("withdraw_btn", LanguageCode.EN)
    val withdrawBn = Strings.get("withdraw_btn", LanguageCode.BN)
    assertNotNull(withdrawEn)
    assertNotNull(withdrawBn)

    val premEn = Strings.get("premium_member", LanguageCode.EN)
    val premBn = Strings.get("premium_member", LanguageCode.BN)
    assertEquals("Premium Member", premEn)
    assertEquals("প্রিমিয়াম মেম্বার", premBn)
  }

  @Test
  fun `verify custom text fields JSON parsing and serialization`() {
    val fields = listOf(
      CustomFieldConfig(fieldId = "f1", label = "Account ID", instruction = "Enter ID", placeholder = "12345", isRequired = true, order = 1),
      CustomFieldConfig(fieldId = "f2", label = "Remarks", instruction = "Notes", placeholder = "Optional", isRequired = false, order = 2)
    )

    val json = JsonUtils.serializeFieldsConfig(fields)
    val parsed = JsonUtils.parseFieldsConfig(json)

    assertEquals(2, parsed.size)
    assertEquals("Account ID", parsed[0].label)
    assertTrue(parsed[0].isRequired)
    assertEquals("Remarks", parsed[1].label)
  }

  @Test
  fun `verify submitted values JSON serialization does not include placeholder`() {
    val submittedMap = mapOf(
      "f1" to "real_user_input_data_123",
      "f2" to "custom proof notes"
    )

    val serialized = JsonUtils.serializeSubmittedValues(submittedMap)
    val parsed = JsonUtils.parseSubmittedValues(serialized)

    assertEquals("real_user_input_data_123", parsed["f1"])
    assertEquals("custom proof notes", parsed["f2"])
  }
}
