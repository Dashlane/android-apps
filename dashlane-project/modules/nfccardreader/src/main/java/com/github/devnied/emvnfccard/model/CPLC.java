package com.github.devnied.emvnfccard.model;

import com.github.devnied.emvnfccard.parser.apdu.annotation.Data;
import com.github.devnied.emvnfccard.parser.apdu.impl.AbstractByteBean;
import com.github.devnied.emvnfccard.parser.apdu.impl.DataFactory;

import java.io.Serializable;
import java.util.Date;

public class CPLC extends AbstractByteBean<CPLC> implements Serializable {
	
	
	private static final long serialVersionUID = -7955013273912185280L;
	
	
	public static final int SIZE = 42;
	
	
	@Data(index = 1, size = 16)
	private Integer ic_fabricator;
	
	@Data(index = 2, size = 16)
	private Integer ic_type;
	
	@Data(index = 3, size = 16)
	private Integer os;
	
	@Data(index = 4, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date os_release_date;
	
	@Data(index = 5, size = 16)
	private Integer os_release_level;
	
	@Data(index = 6, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date ic_fabric_date;
	
	@Data(index = 7, size = 32)
	private Integer ic_serial_number;
	
	@Data(index = 8, size = 16)
	private Integer ic_batch_id;
	
	@Data(index = 9, size = 16)
	private Integer ic_module_fabricator;
	
	@Data(index = 10, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date ic_packaging_date;
	
	@Data(index = 11, size = 16)
	private Integer icc_manufacturer;
	
	@Data(index = 12, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date ic_embedding_date;
	
	@Data(index = 13, size = 16)
	private Integer preperso_id;
	
	@Data(index = 14, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date preperso_date;
	
	@Data(index = 15, size = 32)
	private Integer preperso_equipment;
	
	@Data(index = 16, size = 16)
	private Integer perso_id;
	
	@Data(index = 17, size = 16, dateStandard = DataFactory.CPCL_DATE)
	private Date perso_date;
	
	@Data(index = 18, size = 32)
	private Integer perso_equipment;
	
	

	public Integer getIcFabricator() {
		return ic_fabricator;
	}

	public Integer getIcType() {
		return ic_type;
	}

	public Integer getOs() {
		return os;
	}

	public Date getOsReleaseDate() {
		return os_release_date;
	}

	public Integer getOsReleaseLevel() {
		return os_release_level;
	}

	public Date getIcFabricDate() {
		return ic_fabric_date;
	}

	public Integer getIcSerialNumber() {
		return ic_serial_number;
	}

	public Integer getIcBatchId() {
		return ic_batch_id;
	}

	public Integer getIcModuleFabricator() {
		return ic_module_fabricator;
	}

	public Date getIcPackagingDate() {
		return ic_packaging_date;
	}

	public Integer getIccManufacturer() {
		return icc_manufacturer;
	}

	public Date getIcEmbeddingDate() {
		return ic_embedding_date;
	}

	public Integer getPrepersoId() {
		return preperso_id;
	}

	public Date getPrepersoDate() {
		return preperso_date;
	}

	public Integer getPrepersoEquipment() {
		return preperso_equipment;
	}

	public Integer getPersoId() {
		return perso_id;
	}

	public Date getPersoDate() {
		return perso_date;
	}

	public Integer getPersoEquipment() {
		return perso_equipment;
	}

}
