

package com.github.devnied.emvnfccard.model;

import com.github.devnied.emvnfccard.model.enums.ServiceCode1Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode2Enum;
import com.github.devnied.emvnfccard.model.enums.ServiceCode3Enum;
import com.github.devnied.emvnfccard.utils.BitUtils;
import com.github.devnied.emvnfccard.utils.BytesUtils;
import com.github.devnied.emvnfccard.utils.EnumUtils;

import org.apache.commons.lang3.StringUtils;



public class Service extends AbstractData {

    

    private static final long serialVersionUID = 5154895810563519768L;

    

    private ServiceCode1Enum serviceCode1;

    

    private ServiceCode2Enum serviceCode2;

    

    private ServiceCode3Enum serviceCode3;

    

    public Service(final String pData) {
        if (pData != null && pData.length() == 3) {
            BitUtils bit = new BitUtils(BytesUtils.fromString(StringUtils.rightPad(pData, 4, "0")));
            serviceCode1 = EnumUtils.getValue(bit.getNextInteger(4), ServiceCode1Enum.class);
            serviceCode2 = EnumUtils.getValue(bit.getNextInteger(4), ServiceCode2Enum.class);
            serviceCode3 = EnumUtils.getValue(bit.getNextInteger(4), ServiceCode3Enum.class);
        }
    }

    

    public ServiceCode1Enum getServiceCode1() {
        return serviceCode1;
    }

    

    public ServiceCode2Enum getServiceCode2() {
        return serviceCode2;
    }

    

    public ServiceCode3Enum getServiceCode3() {
        return serviceCode3;
    }

}
