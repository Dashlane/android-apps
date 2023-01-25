

package com.github.devnied.emvnfccard.parser.impl;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.parser.IParser;
import com.github.devnied.emvnfccard.utils.BytesUtils;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.TrackUtils;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;



public abstract class AbstractParser implements IParser {

    

    public static final int UNKNOW = -1;

    

    protected final WeakReference<EmvTemplate> template;

    

    protected AbstractParser(EmvTemplate pTemplate) {
        template = new WeakReference<EmvTemplate>(pTemplate);
    }

    

    protected byte[] selectAID(final byte[] pAid) throws CommunicationException {
        return template.get().getProvider().transceive(new CommandApdu(CommandEnum.SELECT, pAid, 0).toBytes());
    }

    

    protected String extractApplicationLabel(final byte[] pData) {
        String label = null;
        
        byte[] labelByte = TlvUtil.getValue(pData, EmvTags.APPLICATION_PREFERRED_NAME);
        
        if (labelByte == null) {
            labelByte = TlvUtil.getValue(pData, EmvTags.APPLICATION_LABEL);
        }
        
        if (labelByte != null) {
            label = new String(labelByte);
        }
        return label;
    }

    

    protected void extractBankData(final byte[] pData) {
        
        byte[] bic = TlvUtil.getValue(pData, EmvTags.BANK_IDENTIFIER_CODE);
        if (bic != null) {
            template.get().getCard().setBic(new String(bic));
        }
        
        byte[] iban = TlvUtil.getValue(pData, EmvTags.IBAN);
        if (iban != null) {
            template.get().getCard().setIban(new String(iban));
        }
    }

    

    protected void extractCardHolderName(final byte[] pData) {
        
        byte[] cardHolderByte = TlvUtil.getValue(pData, EmvTags.CARDHOLDER_NAME);
        if (cardHolderByte != null) {
            String[] name = StringUtils.split(new String(cardHolderByte).trim(), TrackUtils.CARD_HOLDER_NAME_SEPARATOR);
            if (name != null && name.length > 0) {
                template.get().getCard().setHolderLastname(StringUtils.trimToNull(name[0]));
                if (name.length == 2) {
                    template.get().getCard().setHolderFirstname(StringUtils.trimToNull(name[1]));
                }
            }
        }
    }

    

    protected byte[] getLogEntry(final byte[] pSelectResponse) {
        return TlvUtil.getValue(pSelectResponse, EmvTags.LOG_ENTRY, EmvTags.VISA_LOG_ENTRY);
    }

    

    protected int getTransactionCounter() throws CommunicationException {
        int ret = UNKNOW;
        byte[] data =
                template.get().getProvider().transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x36, 0).toBytes());
        if (ResponseUtils.isSucceed(data)) {
            
            byte[] val = TlvUtil.getValue(data, EmvTags.APP_TRANSACTION_COUNTER);
            if (val != null) {
                ret = BytesUtils.byteArrayToInt(val);
            }
        }
        return ret;
    }

    

    protected int getLeftPinTry() throws CommunicationException {
        int ret = UNKNOW;
        
        byte[] data =
                template.get().getProvider().transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x17, 0).toBytes());
        if (ResponseUtils.isSucceed(data)) {
            
            byte[] val = TlvUtil.getValue(data, EmvTags.PIN_TRY_COUNTER);
            if (val != null) {
                ret = BytesUtils.byteArrayToInt(val);
            }
        }
        return ret;
    }

    

    protected List<TagAndLength> getLogFormat() throws CommunicationException {
        List<TagAndLength> ret = new ArrayList<TagAndLength>();
        
        byte[] data =
                template.get().getProvider().transceive(new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x4F, 0).toBytes());
        if (ResponseUtils.isSucceed(data)) {
            ret = TlvUtil.parseTagAndLength(TlvUtil.getValue(data, EmvTags.LOG_FORMAT));
        }
        return ret;
    }

    

    protected List<EmvTransactionRecord> extractLogEntry(final byte[] pLogEntry) throws CommunicationException {
        List<EmvTransactionRecord> listRecord = new ArrayList<EmvTransactionRecord>();
        
        if (template.get().getConfig().readTransactions && pLogEntry != null) {
            List<TagAndLength> tals = getLogFormat();
            if (tals != null && !tals.isEmpty()) {
                
                for (int rec = 1; rec <= pLogEntry[1]; rec++) {
                    byte[] response = template.get().getProvider()
                                              .transceive(new CommandApdu(CommandEnum.READ_RECORD, rec,
                                                                          pLogEntry[0] << 3 | 4, 0).toBytes());
                    
                    if (ResponseUtils.isSucceed(response)) {
                        try {
                            EmvTransactionRecord record = new EmvTransactionRecord();
                            record.parse(response, tals);

                            if (record.getAmount() != null) {
                                
                                if (record.getAmount() >= 1500000000) {
                                    record.setAmount(record.getAmount() - 1500000000);
                                }

                                
                                if (record.getAmount() == null || record.getAmount() <= 1) {
                                    continue;
                                }
                            }

                            if (record != null) {
                                
                                if (record.getCurrency() == null) {
                                    record.setCurrency(CurrencyEnum.XXX);
                                }
                                listRecord.add(record);
                            }
                        } catch (Exception e) {
                            
                        }
                    } else {
                        
                        break;
                    }
                }
            }
        }
        return listRecord;
    }

}
