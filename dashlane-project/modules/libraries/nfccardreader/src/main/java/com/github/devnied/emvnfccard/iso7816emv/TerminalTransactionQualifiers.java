package com.github.devnied.emvnfccard.iso7816emv;

import com.github.devnied.emvnfccard.utils.BytesUtils;

import java.util.Arrays;

public class TerminalTransactionQualifiers {

    private byte[] data = new byte[4];

    public TerminalTransactionQualifiers() {
    }

    public boolean contactlessMagneticStripeSupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 7);
    }

    public boolean contactlessVSDCsupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 6);
    }

    public boolean contactlessEMVmodeSupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 5);
    }

    public boolean contactEMVsupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 4);
    }

    public boolean readerIsOfflineOnly() {
        return BytesUtils.matchBitByBitIndex(data[0], 3);
    }

    public boolean onlinePINsupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 2);
    }

    public boolean signatureSupported() {
        return BytesUtils.matchBitByBitIndex(data[0], 1);
    }

    public boolean onlineCryptogramRequired() {
        return BytesUtils.matchBitByBitIndex(data[1], 7);
    }

    public boolean cvmRequired() {
        return BytesUtils.matchBitByBitIndex(data[1], 6);
    }

    public boolean contactChipOfflinePINsupported() {
        return BytesUtils.matchBitByBitIndex(data[1], 5);
    }

    public boolean issuerUpdateProcessingSupported() {
        return BytesUtils.matchBitByBitIndex(data[2], 7);
    }

    public boolean consumerDeviceCVMsupported() {
        return BytesUtils.matchBitByBitIndex(data[2], 6);
    }

    public void setMagneticStripeSupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 7, value);
    }

    public void setContactlessVSDCsupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 6, value);
        if (value) {
            setContactlessEMVmodeSupported(false);
        }
    }

    public void setContactlessEMVmodeSupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 5, value);
    }

    public void setContactEMVsupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 4, value);
    }

    public void setReaderIsOfflineOnly(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 3, value);
    }

    public void setOnlinePINsupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 2, value);
    }

    public void setSignatureSupported(final boolean value) {
        data[0] = BytesUtils.setBit(data[0], 1, value);
    }

    public void setOnlineCryptogramRequired(final boolean value) {
        data[1] = BytesUtils.setBit(data[1], 7, value);
    }

    public void setCvmRequired(final boolean value) {
        data[1] = BytesUtils.setBit(data[1], 6, value);
    }

    public void setContactChipOfflinePINsupported(final boolean value) {
        data[1] = BytesUtils.setBit(data[1], 5, value);
    }

    public void setIssuerUpdateProcessingSupported(final boolean value) {
        data[2] = BytesUtils.setBit(data[2], 7, value);
    }

    public void setConsumerDeviceCVMsupported(final boolean value) {
        data[2] = BytesUtils.setBit(data[2], 6, value);
    }

    

    public byte[] getBytes() {
        return Arrays.copyOf(data, data.length);
    }
}