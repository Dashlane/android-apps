

package com.github.devnied.emvnfccard.parser.impl;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.EmvTransactionRecord;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.utils.BytesUtils;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;



public class GeldKarteParser extends AbstractParser {

    

    private static final Pattern PATTERN =
            Pattern.compile(StringUtils.deleteWhitespace(EmvCardScheme.GELDKARTE.getAid()[2]) + ".*");

    public GeldKarteParser(EmvTemplate pTemplate) {
        super(pTemplate);
    }

    @Override
    public Pattern getId() {
        return PATTERN;
    }

    @Override
    public boolean parse(Application pApplication) throws CommunicationException {
        boolean ret = false;
        
        byte[] data = selectAID(pApplication.getAid());
        
        if (ResponseUtils.isSucceed(data)) {
            pApplication.setReadingStep(ApplicationStepEnum.SELECTED);
            
            byte[] logEntry = getLogEntry(data);
            
            pApplication.setAid(TlvUtil.getValue(data, EmvTags.DEDICATED_FILE_NAME));
            
            pApplication.setApplicationLabel(extractApplicationLabel(data));
            
            template.get().getCard().setType(EmvCardScheme.getCardTypeByAid(
                    BytesUtils.bytesToStringNoSpace(pApplication.getAid())));
            
            extractBankData(data);

            
            extractEF_ID(pApplication);

            
            readEfBetrag(pApplication);

            
            readEF_BLOG(pApplication);

            pApplication.setLeftPinTry(getLeftPinTry());
            pApplication.setTransactionCounter(getTransactionCounter());
            
            pApplication.getListTransactions().addAll(extractLogEntry(logEntry));
            
            template.get().getCard().setState(CardStateEnum.ACTIVE);
            ret = true;
        }

        return ret;
    }

    

    protected void readEF_BLOG(final Application pApplication) throws CommunicationException {
        List<EmvTransactionRecord> list = new ArrayList<EmvTransactionRecord>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
        
        for (int i = 1; i < 16; i++) {
            byte[] data = template.get().getProvider()
                                  .transceive(new CommandApdu(CommandEnum.READ_RECORD, i, 0xEC, 0).toBytes());
            
            if (ResponseUtils.isSucceed(data)) {
                if (data.length < 35) {
                    continue;
                }
                EmvTransactionRecord record = new EmvTransactionRecord();
                record.setCurrency(CurrencyEnum.EUR);
                record.setTransactionType(getType(data[0]));
                record.setAmount(
                        Float.parseFloat(BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(data, 21, 24))) / 100L);

                try {
                    record.setDate(dateFormat.parse(String.format("%02x.%02x.%02x%02x", data[32], data[31], data[29],
                                                                  data[30])));
                    record.setTime(timeFormat.parse(String.format("%02x:%02x:%02x", data[33], data[34], data[35])));
                } catch (ParseException e) {
                    
                }
                list.add(record);
            } else {
                break;
            }
        }
        pApplication.setListTransactions(list);
    }

    protected TransactionTypeEnum getType(byte logstate) {
        switch ((logstate & 0x60) >> 5) {
            case 0:
                return TransactionTypeEnum.LOADED;
            case 1:
                return TransactionTypeEnum.UNLOADED;
            case 2:
                return TransactionTypeEnum.PURCHASE;
            case 3:
                return TransactionTypeEnum.REFUND;
        }
        return null;
    }

    

    protected void readEfBetrag(final Application pApplication) throws CommunicationException {
        
        byte[] data = template.get().getProvider()
                              .transceive(new CommandApdu(CommandEnum.READ_RECORD, 0x01, 0xC4, 0).toBytes());
        
        if (ResponseUtils.isSucceed(data)) {
            pApplication.setAmount(Float.parseFloat(String.format("%02x%02x%02x", data[0], data[1], data[2])) / 100.0f);
        }
    }

    

    protected void extractEF_ID(final Application pApplication) throws CommunicationException {
        
        byte[] data = template.get().getProvider()
                              .transceive(new CommandApdu(CommandEnum.READ_RECORD, 0x01, 0xBC, 0).toBytes());
        if (ResponseUtils.isSucceed(data)) {
            pApplication.setReadingStep(ApplicationStepEnum.READ);
            
            SimpleDateFormat format = new SimpleDateFormat("MM/yy", Locale.getDefault());

            
            EmvTrack2 track2 = new EmvTrack2();
            track2.setCardNumber(BytesUtils.bytesToStringNoSpace(Arrays.copyOfRange(data, 4, 9)));
            try {
                track2.setExpireDate(format.parse(String.format("%02x/%02x", data[11], data[10])));
            } catch (ParseException e) {
                
            }
            template.get().getCard().setTrack2(track2);
        }
    }

}
