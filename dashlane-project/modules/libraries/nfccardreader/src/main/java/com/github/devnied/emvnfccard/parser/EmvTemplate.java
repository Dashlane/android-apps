package com.github.devnied.emvnfccard.parser;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITerminal;
import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.iso7816emv.impl.DefaultTerminalImpl;
import com.github.devnied.emvnfccard.model.Application;
import com.github.devnied.emvnfccard.model.EmvCard;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;
import com.github.devnied.emvnfccard.parser.impl.EmvParser;
import com.github.devnied.emvnfccard.parser.impl.GeldKarteParser;
import com.github.devnied.emvnfccard.parser.impl.ProviderWrapper;
import com.github.devnied.emvnfccard.utils.AtrUtils;
import com.github.devnied.emvnfccard.utils.BytesUtils;
import com.github.devnied.emvnfccard.utils.CPLCUtils;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;
import com.github.devnied.emvnfccard.utils.TlvUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class EmvTemplate {

    public static final int MAX_RECORD_SFI = 16;

    private static final byte[] PPSE = "2PAY.SYS.DDF01".getBytes();

    private static final byte[] PSE = "1PAY.SYS.DDF01".getBytes();

    private ITerminal terminal;

    private IProvider provider;

    private List<IParser> parsers;

    private Config config;

    private EmvCard card;

    public static Builder Builder() {
        return new Builder();
    }

    public static Config Config() {
        return new Config();
    }

    public static class Config {

        public boolean contactLess = true;

        public boolean readTransactions = true;

        public boolean readAllAids = true;

        public boolean readAt = true;

        public boolean readCplc = false;

        public boolean removeDefaultParsers;

        Config() {
        }

        public Config setContactLess(final boolean contactLess) {
            this.contactLess = contactLess;
            return this;
        }

        public Config setReadTransactions(final boolean readTransactions) {
            this.readTransactions = readTransactions;
            return this;
        }

        public Config setReadAllAids(final boolean readAllAids) {
            this.readAllAids = readAllAids;
            return this;
        }

        public Config setRemoveDefaultParsers(boolean removeDefaultParsers) {
            this.removeDefaultParsers = removeDefaultParsers;
            return this;
        }

        public Config setReadAt(boolean readAt) {
            this.readAt = readAt;
            return this;
        }

        public Config setReadCplc(boolean readCplc) {
            this.readCplc = readCplc;
            return this;
        }

    }

    public static class Builder {

        private IProvider provider;
        private ITerminal terminal;
        private Config config;

        Builder() {
        }

        public Builder setProvider(final IProvider provider) {
            this.provider = provider;
            return this;
        }

        public Builder setTerminal(final ITerminal terminal) {
            this.terminal = terminal;
            return this;
        }

        public Builder setConfig(Config config) {
            this.config = config;
            return this;
        }

        public EmvTemplate build() {
            if (provider == null) {
                throw new IllegalArgumentException("Provider may not be null.");
            }
            
            if (terminal == null) {
                terminal = new DefaultTerminalImpl();
            }
            return new EmvTemplate(provider, terminal, config);
        }

    }

    private EmvTemplate(final IProvider pProvider, final ITerminal pTerminal, final Config pConfig) {
        provider = new ProviderWrapper(pProvider);
        terminal = pTerminal;
        config = pConfig;
        if (config == null) {
            config = Config();
        }
        if (!config.removeDefaultParsers) {
            addDefaultParsers();
        }
        card = new EmvCard();
    }

    private void addDefaultParsers() {
        parsers = new ArrayList<IParser>();
        parsers.add(new GeldKarteParser(this));
        parsers.add(new EmvParser(this));
    }

    public EmvTemplate addParsers(final IParser... pParsers) {
        if (pParsers != null) {
            for (IParser parser : pParsers) {
                parsers.add(0, parser);
            }
        }
        return this;
    }

    public EmvCard readEmvCard(Calendar now) throws CommunicationException {
        
        if (config.readCplc) {
            readCPLCInfos();
        }

        
        if (config.readAt) {
            card.setAt(BytesUtils.bytesToStringNoSpace(provider.getAt()));
            card.setAtrDescription(config.contactLess ? AtrUtils.getDescriptionFromAts(card.getAt()) :
                                   AtrUtils.getDescription(card.getAt()));
        }
        
        if (!readWithPSE(now)) {
            
            readWithAID(now);
        }

        return card;
    }

    protected void readCPLCInfos() throws CommunicationException {
        card.setCplc(CPLCUtils.parse(provider.transceive(
                new CommandApdu(CommandEnum.GET_DATA, 0x9F, 0x7F, null, 0).toBytes()), Calendar.getInstance()));
    }

    protected boolean readWithPSE(Calendar now) throws CommunicationException {
        boolean ret = false;
        
        byte[] data = selectPaymentEnvironment();
        if (ResponseUtils.isSucceed(data)) {
            
            card.getApplications().addAll(parseFCIProprietaryTemplate(data));
            Collections.sort(card.getApplications());
            
            for (Application app : card.getApplications()) {
                boolean status = false;
                String applicationAid = BytesUtils.bytesToStringNoSpace(app.getAid());
                for (IParser impl : parsers) {
                    if (impl.getId() != null && impl.getId().matcher(applicationAid).matches()) {
                        status = impl.parse(app, now);
                        break;
                    }
                }
                if (!ret && status) {
                    ret = status;
                    if (!config.readAllAids) {
                        break;
                    }
                }
            }
            if (!ret) {
                card.setState(CardStateEnum.LOCKED);
            }
        }

        return ret;
    }

    protected List<Application> parseFCIProprietaryTemplate(final byte[] pData) throws CommunicationException {
        List<Application> ret = new ArrayList<Application>();
        
        byte[] data = TlvUtil.getValue(pData, EmvTags.SFI);

        
        if (data != null) {
            int sfi = BytesUtils.byteArrayToInt(data);
            
            for (int rec = 0; rec < MAX_RECORD_SFI; rec++) {
                data = provider.transceive(new CommandApdu(CommandEnum.READ_RECORD, rec, sfi << 3 | 4, 0).toBytes());
                
                if (ResponseUtils.isSucceed(data)) {
                    
                    ret.addAll(getApplicationTemplate(data));
                } else {
                    
                    break;
                }
            }
        } else {
            
            ret.addAll(getApplicationTemplate(pData));
        }
        return ret;
    }

    protected List<Application> getApplicationTemplate(final byte[] pData) {
        List<Application> ret = new ArrayList<Application>();
        
        List<TLV> listTlv = TlvUtil.getlistTLV(pData, EmvTags.APPLICATION_TEMPLATE);
        
        for (TLV tlv : listTlv) {
            Application application = new Application();
            
            List<TLV> listTlvData = TlvUtil.getlistTLV(tlv.getValueBytes(), EmvTags.AID_CARD,
                                                       EmvTags.APPLICATION_LABEL,
                                                       EmvTags.APPLICATION_PRIORITY_INDICATOR);
            
            for (TLV data : listTlvData) {
                if (data.getTag() == EmvTags.APPLICATION_PRIORITY_INDICATOR) {
                    application.setPriority(BytesUtils.byteArrayToInt(data.getValueBytes()));
                } else if (data.getTag() == EmvTags.APPLICATION_LABEL) {
                    application.setApplicationLabel(new String(data.getValueBytes()));
                } else {
                    application.setAid(data.getValueBytes());
                    ret.add(application);
                }
            }
        }
        return ret;
    }

    protected void readWithAID(Calendar now) throws CommunicationException {
        
        Application app = new Application();
        for (EmvCardScheme type : EmvCardScheme.values()) {
            for (byte[] aid : type.getAidByte()) {
                app.setAid(aid);
                app.setApplicationLabel(type.getName());
                String applicationAid = BytesUtils.bytesToStringNoSpace(aid);
                for (IParser impl : parsers) {
                    if (impl.getId() != null && impl.getId().matcher(applicationAid).matches() && impl.parse(app, now)) {
                        
                        card.getApplications().clear();
                        
                        card.getApplications().add(app);
                        return;
                    }
                }
            }
        }
    }

    protected byte[] selectPaymentEnvironment() throws CommunicationException {
        
        return provider.transceive(new CommandApdu(CommandEnum.SELECT, config.contactLess ? PPSE : PSE, 0).toBytes());
    }

    public EmvCard getCard() {
        return card;
    }

    public IProvider getProvider() {
        return provider;
    }

    public Config getConfig() {
        return config;
    }

    public ITerminal getTerminal() {
        return terminal;
    }

    public List<IParser> getParsers() {
        return Collections.unmodifiableList(parsers);
    }

}
