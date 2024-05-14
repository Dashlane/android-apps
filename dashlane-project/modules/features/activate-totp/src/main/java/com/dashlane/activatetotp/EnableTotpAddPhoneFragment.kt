package com.dashlane.activatetotp

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.dashlane.activatetotp.databinding.EnableTotpStepAddPhoneContentBinding
import com.dashlane.activatetotp.databinding.EnableTotpStepContainerBinding
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.util.setCurrentPageView
import com.dashlane.vault.model.getDefaultCountry
import com.dashlane.vault.model.getLabel
import com.dashlane.vault.model.isSemanticallyNull
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country
import dagger.hilt.android.AndroidEntryPoint
import java.text.Collator
import javax.inject.Inject

@AndroidEntryPoint
internal class EnableTotpAddPhoneFragment : Fragment() {
    @Inject
    internal lateinit var vaultDataQuery: VaultDataQuery

    private lateinit var selectedCountry: Country

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedCountry = if (savedInstanceState == null) {
            requireContext().getDefaultCountry()
        } else {
            Country.forIsoCode(savedInstanceState.getString(STATE_SELECTED_COUNTRY)!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val navController = findNavController()
        val args = EnableTotpAddPhoneFragmentArgs.fromBundle(requireArguments())
        val containerBinding = EnableTotpStepContainerBinding.inflate(inflater, container, false)
        val contentBinding =
            EnableTotpStepAddPhoneContentBinding.inflate(inflater, containerBinding.content, true)

        containerBinding.setup(
            stepNumber = 2,
            titleResId = R.string.enable_totp_add_phone_title,
            positiveButtonResId = R.string.enable_totp_add_phone_cta,
            onClickPositiveButton = {
                navController.navigate(
                    EnableTotpAddPhoneFragmentDirections.goToFetchInfo(
                        totpLogin = args.totpLogin,
                        phoneNumber = contentBinding.number.text!!.toString(),
                        country = selectedCountry.isoCode
                    )
                )
            }
        )

        contentBinding.number.apply {
            doOnTextChanged { text, _, _, _ ->
                containerBinding.buttonPositive.isEnabled = !text.isNullOrEmpty()
                contentBinding.showPhoneNumberError(false)
            }

            imeOptions = EditorInfo.IME_ACTION_DONE

            setOnEditorActionListener { _, id, _ ->
                if (id == EditorInfo.IME_ACTION_DONE && !text.isNullOrEmpty()) {
                    containerBinding.buttonPositive.performClick()
                } else {
                    false
                }
            }

            requestFocus()
        }

        contentBinding.country.doOnTextChanged { _, _, _, _ ->
            contentBinding.showPhoneNumberError(false)
        }

        if (savedInstanceState == null) {
            contentBinding.number.text = null

            getPrefilledPhone()?.let { (country, number) ->
                selectedCountry = country
                contentBinding.number.setText(number)
            }
        }

        contentBinding.country.setText(selectedCountry.formatSelection())

        contentBinding.countrySpinner.run {
            val spinnerAdapter = createSpinnerAdapter()
            adapter = spinnerAdapter
            setSelection(spinnerAdapter.getPosition(selectedCountry), false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    av: AdapterView<*>?,
                    v: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCountry = spinnerAdapter.getItem(position)!!
                    contentBinding.country.setText(selectedCountry.formatSelection())
                }

                override fun onNothingSelected(av: AdapterView<*>?) = Unit
            }
        }

        setFragmentResultListener(REQUEST_PHONE_NUMBER_VALIDATION) { _, bundle ->
            val error = bundle.getBoolean(KEY_PHONE_NUMBER_ERROR, false)
            contentBinding.showPhoneNumberError(error)
        }

        return containerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentPageView(AnyPage.SETTINGS_SECURITY_TWO_FACTOR_AUTHENTICATION_ENABLE_BACKUP_PHONE_NUMBER)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_SELECTED_COUNTRY, selectedCountry.isoCode)
    }

    private fun getPrefilledPhone(): Pair<Country, String>? = vaultDataQuery
        .queryAll(VaultFilter(dataTypeFilter = SpecificDataTypeFilter(SyncObjectType.PHONE)))
        .map { it.syncObject as SyncObject.Phone }
        .takeIf { it.size == 1 }
        ?.first()
        ?.let { phone ->
            phone.number?.takeUnless { it.isSemanticallyNull() }?.let { number ->
                phone.localeFormat?.let { country -> country to number }
            }
        }

    private fun createSpinnerAdapter() = object : ArrayAdapter<Country>(
        requireContext(),
        android.R.layout.simple_dropdown_item_1line,
        Country.values()
    ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup) =
            (
                super.getView(
                    position,
                    convertView,
                    parent
                ) as TextView
                ).apply { setTextColor(Color.TRANSPARENT) }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup) =
            (super.getView(position, convertView, parent) as TextView).apply {
                setTextColor(context.getColor(R.color.text_neutral_catchy))
                text = getItem(position)!!.formatDropDown(context)
            }
    }.apply {
        val collator = Collator.getInstance()
        sort { c1, c2 -> collator.compare(c1.getLabel(context), c2.getLabel(context)) }
    }

    private fun EnableTotpStepAddPhoneContentBinding.showPhoneNumberError(show: Boolean) {
        error.isInvisible = !show
    }

    companion object {
        private const val STATE_SELECTED_COUNTRY = "selected_country"
        const val REQUEST_PHONE_NUMBER_VALIDATION = "phone_number_validation"
        const val KEY_PHONE_NUMBER_ERROR = "phone_number_error"

        private val Country.emoji: String
            get() {
                val firstLetter = Character.codePointAt(isoCode, 0) - 0x41 + 0x1F1E6
                val secondLetter = Character.codePointAt(isoCode, 1) - 0x41 + 0x1F1E6
                return String(Character.toChars(firstLetter)) + String(
                    Character.toChars(
                        secondLetter
                    )
                )
            }

        fun Country.formatDropDown(context: Context) = buildString {
            append(emoji)
            append(" ")
            append(getLabel(context))
            isoToCallingCodes[isoCode]?.let { callingCode -> append(" (+$callingCode)") }
        }

        fun Country.formatSelection() = buildString {
            append(emoji)
            isoToCallingCodes[isoCode]?.let { callingCode -> append(" +$callingCode") }
        }

        private val isoToCallingCodes = mapOf(
            "AC" to 247,
            "AD" to 376,
            "AE" to 971,
            "AF" to 93,
            "AG" to 1,
            "AI" to 1,
            "AL" to 355,
            "AM" to 374,
            "AO" to 244,
            "AR" to 54,
            "AS" to 1,
            "AT" to 43,
            "AU" to 61,
            "AW" to 297,
            "AX" to 358,
            "AZ" to 994,
            "BA" to 387,
            "BB" to 1,
            "BD" to 880,
            "BE" to 32,
            "BF" to 226,
            "BG" to 359,
            "BH" to 973,
            "BI" to 257,
            "BJ" to 229,
            "BL" to 590,
            "BM" to 1,
            "BN" to 673,
            "BO" to 591,
            "BQ" to 599,
            "BR" to 55,
            "BS" to 1,
            "BT" to 975,
            "BW" to 267,
            "BY" to 375,
            "BZ" to 501,
            "CA" to 1,
            "CC" to 61,
            "CD" to 243,
            "CF" to 236,
            "CG" to 242,
            "CH" to 41,
            "CI" to 225,
            "CK" to 682,
            "CL" to 56,
            "CM" to 237,
            "CN" to 86,
            "CO" to 57,
            "CR" to 506,
            "CU" to 53,
            "CV" to 238,
            "CW" to 599,
            "CX" to 61,
            "CY" to 357,
            "CZ" to 420,
            "DE" to 49,
            "DJ" to 253,
            "DK" to 45,
            "DM" to 1,
            "DO" to 1,
            "DZ" to 213,
            "EC" to 593,
            "EE" to 372,
            "EG" to 20,
            "EH" to 212,
            "ER" to 291,
            "ES" to 34,
            "ET" to 251,
            "FI" to 358,
            "FJ" to 679,
            "FK" to 500,
            "FM" to 691,
            "FO" to 298,
            "FR" to 33,
            "GA" to 241,
            "GB" to 44,
            "GD" to 1,
            "GE" to 995,
            "GF" to 594,
            "GG" to 44,
            "GH" to 233,
            "GI" to 350,
            "GL" to 299,
            "GM" to 220,
            "GN" to 224,
            "GP" to 590,
            "GQ" to 240,
            "GR" to 30,
            "GT" to 502,
            "GU" to 1,
            "GW" to 245,
            "GY" to 592,
            "HK" to 852,
            "HN" to 504,
            "HR" to 385,
            "HT" to 509,
            "HU" to 36,
            "ID" to 62,
            "IE" to 353,
            "IL" to 972,
            "IM" to 44,
            "IN" to 91,
            "IO" to 246,
            "IQ" to 964,
            "IR" to 98,
            "IS" to 354,
            "IT" to 39,
            "JE" to 44,
            "JM" to 1,
            "JO" to 962,
            "JP" to 81,
            "KE" to 254,
            "KG" to 996,
            "KH" to 855,
            "KI" to 686,
            "KM" to 269,
            "KN" to 1,
            "KP" to 850,
            "KR" to 82,
            "KW" to 965,
            "KY" to 1,
            "KZ" to 7,
            "LA" to 856,
            "LB" to 961,
            "LC" to 1,
            "LI" to 423,
            "LK" to 94,
            "LR" to 231,
            "LS" to 266,
            "LT" to 370,
            "LU" to 352,
            "LV" to 371,
            "LY" to 218,
            "MA" to 212,
            "MC" to 377,
            "MD" to 373,
            "ME" to 382,
            "MF" to 590,
            "MG" to 261,
            "MH" to 692,
            "MK" to 389,
            "ML" to 223,
            "MM" to 95,
            "MN" to 976,
            "MO" to 853,
            "MP" to 1,
            "MQ" to 596,
            "MR" to 222,
            "MS" to 1,
            "MT" to 356,
            "MU" to 230,
            "MV" to 960,
            "MW" to 265,
            "MX" to 52,
            "MY" to 60,
            "MZ" to 258,
            "NA" to 264,
            "NC" to 687,
            "NE" to 227,
            "NF" to 672,
            "NG" to 234,
            "NI" to 505,
            "NL" to 31,
            "NO" to 47,
            "NP" to 977,
            "NR" to 674,
            "NU" to 683,
            "NZ" to 64,
            "OM" to 968,
            "PA" to 507,
            "PE" to 51,
            "PF" to 689,
            "PG" to 675,
            "PH" to 63,
            "PK" to 92,
            "PL" to 48,
            "PM" to 508,
            "PR" to 1,
            "PS" to 970,
            "PT" to 351,
            "PW" to 680,
            "PY" to 595,
            "QA" to 974,
            "RE" to 262,
            "RO" to 40,
            "RS" to 381,
            "RU" to 7,
            "RW" to 250,
            "SA" to 966,
            "SB" to 677,
            "SC" to 248,
            "SD" to 249,
            "SE" to 46,
            "SG" to 65,
            "SH" to 290,
            "SI" to 386,
            "SJ" to 47,
            "SK" to 421,
            "SL" to 232,
            "SM" to 378,
            "SN" to 221,
            "SO" to 252,
            "SR" to 597,
            "SS" to 211,
            "ST" to 239,
            "SV" to 503,
            "SX" to 1,
            "SY" to 963,
            "SZ" to 268,
            "TA" to 290,
            "TC" to 1,
            "TD" to 235,
            "TG" to 228,
            "TH" to 66,
            "TJ" to 992,
            "TK" to 690,
            "TL" to 670,
            "TM" to 993,
            "TN" to 216,
            "TO" to 676,
            "TR" to 90,
            "TT" to 1,
            "TV" to 688,
            "TW" to 886,
            "TZ" to 255,
            "UA" to 380,
            "UG" to 256,
            "US" to 1,
            "UY" to 598,
            "UZ" to 998,
            "VA" to 39,
            "VC" to 1,
            "VE" to 58,
            "VG" to 1,
            "VI" to 1,
            "VN" to 84,
            "VU" to 678,
            "WF" to 681,
            "WS" to 685,
            "XK" to 383,
            "YE" to 967,
            "YT" to 262,
            "ZA" to 27,
            "ZM" to 260,
            "ZW" to 263
        )
    }
}