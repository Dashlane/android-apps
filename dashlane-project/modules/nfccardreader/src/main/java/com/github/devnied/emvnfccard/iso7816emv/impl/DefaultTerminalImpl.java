

package com.github.devnied.emvnfccard.iso7816emv.impl;

import android.annotation.SuppressLint;

import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITerminal;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.iso7816emv.TerminalTransactionQualifiers;
import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.utils.BytesUtils;

import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public final class DefaultTerminalImpl implements ITerminal {

    

    private static final SecureRandom random = new SecureRandom();

    

    private CountryCodeEnum countryCode = CountryCodeEnum.FR;

    

    @SuppressLint("JavaUtilDateUsage")
    @Override
    public byte[] constructValue(final TagAndLength pTagAndLength) {
        byte ret[] = new byte[pTagAndLength.getLength()];
        byte val[] = null;
        if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_QUALIFIERS) {
            TerminalTransactionQualifiers terminalQual = new TerminalTransactionQualifiers();
            terminalQual.setContactlessVSDCsupported(true);
            terminalQual.setContactEMVsupported(true);

            terminalQual.setMagneticStripeSupported(true);
            terminalQual.setContactlessEMVmodeSupported(true);
            terminalQual.setOnlinePINsupported(true);
            terminalQual.setReaderIsOfflineOnly(false);
            terminalQual.setSignatureSupported(true);
            terminalQual.setContactChipOfflinePINsupported(true);
            terminalQual.setIssuerUpdateProcessingSupported(true);
            terminalQual.setConsumerDeviceCVMsupported(true);
            val = terminalQual.getBytes();
        } else if (pTagAndLength.getTag() == EmvTags.TERMINAL_COUNTRY_CODE) {
            val = BytesUtils.fromString(
                    StringUtils.leftPad(String.valueOf(countryCode.getNumeric()), pTagAndLength.getLength() * 2,
                                        "0"));
        } else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_CURRENCY_CODE) {
            val = BytesUtils.fromString(StringUtils.leftPad(
                    String.valueOf(CurrencyEnum.find(countryCode, CurrencyEnum.EUR).getISOCodeNumeric()),
                    pTagAndLength.getLength() * 2, "0"));
        } else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_DATE) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.US);
            val = BytesUtils.fromString(sdf.format(new Date()));
        } else if (pTagAndLength.getTag() == EmvTags.TRANSACTION_TYPE ||
                   pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_TYPE) {
            val = new byte[]{(byte) TransactionTypeEnum.PURCHASE.getKey()};
        } else if (pTagAndLength.getTag() == EmvTags.AMOUNT_AUTHORISED_NUMERIC) {
            val = BytesUtils.fromString("01");
        } else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TYPE) {
            val = new byte[]{0x22};
        } else if (pTagAndLength.getTag() == EmvTags.TERMINAL_CAPABILITIES) {
            val = new byte[]{(byte) 0xE0, (byte) 0xA0, 0x00};
        } else if (pTagAndLength.getTag() == EmvTags.ADDITIONAL_TERMINAL_CAPABILITIES) {
            val = new byte[]{(byte) 0x8e, (byte) 0, (byte) 0xb0, 0x50, 0x05};
        } else if (pTagAndLength.getTag() == EmvTags.DS_REQUESTED_OPERATOR_ID) {
            val = BytesUtils.fromString("7A45123EE59C7F40");
        } else if (pTagAndLength.getTag() == EmvTags.UNPREDICTABLE_NUMBER) {
            random.nextBytes(ret);
        } else if (pTagAndLength.getTag() == EmvTags.MERCHANT_TYPE_INDICATOR) {
            val = new byte[]{0x01};
        } else if (pTagAndLength.getTag() == EmvTags.TERMINAL_TRANSACTION_INFORMATION) {
            val = new byte[]{(byte) 0xC0, (byte) 0x80, 0};
        }
        if (val != null) {
            System.arraycopy(val, 0, ret, Math.max(ret.length - val.length, 0), Math.min(val.length, ret.length));
        }
        return ret;
    }

    

    public void setCountryCode(final CountryCodeEnum countryCode) {
        if (countryCode != null) {
            this.countryCode = countryCode;
        }
    }


}
