

package com.github.devnied.emvnfccard.parser.impl;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.Afl;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.parser.EmvTemplate;
import com.github.devnied.emvnfccard.utils.BytesUtils;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;
import com.github.devnied.emvnfccard.utils.TrackUtils;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;



public class EmvParser extends AbstractParser {

    

    private static final Pattern PATTERN = Pattern.compile(".*");

    

    public EmvParser(EmvTemplate pTemplate) {
        super(pTemplate);
    }

    @Override
    public Pattern getId() {
        return PATTERN;
    }

    @Override
    public boolean parse(Application pApplication) throws CommunicationException {
        return extractPublicData(pApplication);
    }

    

    protected boolean extractPublicData(final Application pApplication) throws CommunicationException {
        boolean ret = false;
        
        byte[] data = selectAID(pApplication.getAid());
        
        
        if (ResponseUtils.contains(data, SwEnum.SW_9000, SwEnum.SW_6285)) {
            
            pApplication.setReadingStep(ApplicationStepEnum.SELECTED);
            
            ret = parse(data, pApplication);
            if (ret) {
                
                String aid = BytesUtils.bytesToStringNoSpace(TlvUtil.getValue(data, EmvTags.DEDICATED_FILE_NAME));
                String applicationLabel = extractApplicationLabel(data);
                if (applicationLabel == null) {
                    applicationLabel = pApplication.getApplicationLabel();
                }
                template.get().getCard().setType(findCardScheme(aid, template.get().getCard().getCardNumber()));
                pApplication.setAid(BytesUtils.fromString(aid));
                pApplication.setApplicationLabel(applicationLabel);
                pApplication.setLeftPinTry(getLeftPinTry());
                pApplication.setTransactionCounter(getTransactionCounter());
                template.get().getCard().setState(CardStateEnum.ACTIVE);
            }
        }
        return ret;
    }

    

    protected EmvCardScheme findCardScheme(final String pAid, final String pCardNumber) {
        EmvCardScheme type = EmvCardScheme.getCardTypeByAid(pAid);
        
        if (type == EmvCardScheme.CB) {
            type = EmvCardScheme.getCardTypeByCardNumber(pCardNumber);
        }
        return type;
    }


    

    protected boolean parse(final byte[] pSelectResponse, final Application pApplication)
            throws CommunicationException {
        boolean ret = false;
        
        byte[] logEntry = getLogEntry(pSelectResponse);
        
        byte[] pdol = TlvUtil.getValue(pSelectResponse, EmvTags.PDOL);
        
        byte[] gpo = getGetProcessingOptions(pdol);
        
        extractBankData(pSelectResponse);

        
        if (!ResponseUtils.isSucceed(gpo)) {
            if (pdol != null) {
                gpo = getGetProcessingOptions(null);
            }

            
            if (pdol == null || !ResponseUtils.isSucceed(gpo)) {
                
                gpo = template.get().getProvider()
                              .transceive(new CommandApdu(CommandEnum.READ_RECORD, 1, 0x0C, 0).toBytes());
                if (!ResponseUtils.isSucceed(gpo)) {
                    return false;
                }
            }
        }
        
        pApplication.setReadingStep(ApplicationStepEnum.READ);

        
        if (extractCommonsCardData(gpo)) {
            
            pApplication.setListTransactions(extractLogEntry(logEntry));
            ret = true;
        }

        return ret;
    }

    

    protected boolean extractCommonsCardData(final byte[] pGpo) throws CommunicationException {
        boolean ret = false;
        
        byte data[] = TlvUtil.getValue(pGpo, EmvTags.RESPONSE_MESSAGE_TEMPLATE_1);
        if (data != null) {
            data = ArrayUtils.subarray(data, 2, data.length);
        } else { 
            ret = extractTrackData(template.get().getCard(), pGpo);
            if (!ret) {
                data = TlvUtil.getValue(pGpo, EmvTags.APPLICATION_FILE_LOCATOR);
            } else {
                extractCardHolderName(pGpo);
            }
        }

        if (data != null) {
            
            List<Afl> listAfl = extractAfl(data);
            
            for (Afl afl : listAfl) {
                
                for (int index = afl.getFirstRecord(); index <= afl.getLastRecord(); index++) {
                    byte[] info = template.get().getProvider()
                                          .transceive(
                                                  new CommandApdu(CommandEnum.READ_RECORD, index, afl.getSfi() << 3 | 4,
                                                                  0).toBytes());
                    
                    if (ResponseUtils.isSucceed(info)) {
                        extractCardHolderName(info);
                        if (extractTrackData(template.get().getCard(), info)) {
                            return true;
                        }
                    }
                }
            }
        }
        return ret;
    }


    

    protected List<Afl> extractAfl(final byte[] pAfl) {
        List<Afl> list = new ArrayList<Afl>();
        ByteArrayInputStream bai = new ByteArrayInputStream(pAfl);
        while (bai.available() >= 4) {
            Afl afl = new Afl();
            afl.setSfi(bai.read() >> 3);
            afl.setFirstRecord(bai.read());
            afl.setLastRecord(bai.read());
            afl.setOfflineAuthentication(bai.read() == 1);
            list.add(afl);
        }
        return list;
    }

    

    protected byte[] getGetProcessingOptions(final byte[] pPdol) throws CommunicationException {
        
        List<TagAndLength> list = TlvUtil.parseTagAndLength(pPdol);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(EmvTags.COMMAND_TEMPLATE.getTagBytes()); 
            
            out.write(TlvUtil.getLength(list)); 
            if (list != null) {
                for (TagAndLength tl : list) {
                    out.write(template.get().getTerminal().constructValue(tl));
                }
            }
        } catch (IOException ioe) {
            
        }
        return template.get().getProvider()
                       .transceive(new CommandApdu(CommandEnum.GPO, out.toByteArray(), 0).toBytes());
    }

    

    protected boolean extractTrackData(final EmvCard pEmvCard, final byte[] pData) {
        template.get().getCard().setTrack1(TrackUtils.extractTrack1Data(TlvUtil.getValue(pData, EmvTags.TRACK1_DATA)));
        template.get().getCard().setTrack2(TrackUtils.extractTrack2EquivalentData(
                TlvUtil.getValue(pData, EmvTags.TRACK_2_EQV_DATA, EmvTags.TRACK2_DATA)));
        return pEmvCard.getTrack1() != null || pEmvCard.getTrack2() != null;
    }


}
