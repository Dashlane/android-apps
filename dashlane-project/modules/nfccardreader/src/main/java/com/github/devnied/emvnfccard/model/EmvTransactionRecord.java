

package com.github.devnied.emvnfccard.model;

import com.github.devnied.emvnfccard.model.enums.CountryCodeEnum;
import com.github.devnied.emvnfccard.model.enums.CurrencyEnum;
import com.github.devnied.emvnfccard.model.enums.TransactionTypeEnum;
import com.github.devnied.emvnfccard.parser.apdu.annotation.Data;
import com.github.devnied.emvnfccard.parser.apdu.impl.AbstractByteBean;
import com.github.devnied.emvnfccard.parser.apdu.impl.DataFactory;

import java.io.Serializable;
import java.util.Date;



public class EmvTransactionRecord extends AbstractByteBean<EmvTransactionRecord> implements Serializable {

	

	private static final long serialVersionUID = -7050737312961921452L;

	

	@Data(index = 1, size = 48, format = DataFactory.BCD_FORMAT, tag = "9f02")
	private Float amount;

	

	@Data(index = 2, size = 8, readHexa = true, tag = "9f27")
	private String cryptogramData;

	

	@Data(index = 3, size = 16, tag = "9f1a")
	private CountryCodeEnum terminalCountry;

	

	@Data(index = 4, size = 16, tag = "5f2a")
	private CurrencyEnum currency;

	

	@Data(index = 5, size = 24, dateStandard = DataFactory.BCD_DATE, format = "yyMMdd", tag = "9a")
	private Date date;

	

	@Data(index = 6, size = 8, readHexa = true, tag = "9c")
	private TransactionTypeEnum transactionType;

	

	@Data(index = 7, size = 24, dateStandard = DataFactory.BCD_DATE, format = "HHmmss", tag = "9f21")
	private Date time;

	

	public Float getAmount() {
		return amount;
	}

	

	public String getCryptogramData() {
		return cryptogramData;
	}

	

	public CurrencyEnum getCurrency() {
		return currency;
	}

	

	public TransactionTypeEnum getTransactionType() {
		return transactionType;
	}

	

	public CountryCodeEnum getTerminalCountry() {
		return terminalCountry;
	}

	

	public void setAmount(final Float amount) {
		this.amount = amount;
	}

	

	public void setCryptogramData(final String cryptogramData) {
		this.cryptogramData = cryptogramData;
	}

	

	public void setTerminalCountry(final CountryCodeEnum terminalCountry) {
		this.terminalCountry = terminalCountry;
	}

	

	public void setCurrency(final CurrencyEnum currency) {
		this.currency = currency;
	}

	

	public void setTransactionType(final TransactionTypeEnum transactionType) {
		this.transactionType = transactionType;
	}

	

	public Date getDate() {
		return date;
	}

	

	public void setDate(final Date date) {
		this.date = date;
	}

	

	public Date getTime() {
		return time;
	}

	

	public void setTime(final Date time) {
		this.time = time;
	}

}
