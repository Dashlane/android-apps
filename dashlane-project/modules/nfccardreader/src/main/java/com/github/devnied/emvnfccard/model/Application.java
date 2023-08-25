package com.github.devnied.emvnfccard.model;

import com.github.devnied.emvnfccard.model.enums.ApplicationStepEnum;

import java.util.List;

public class Application extends AbstractData implements Comparable<Application> {

	private static final long serialVersionUID = 2917341864815087679L;

	private byte[] aid;
	
	private ApplicationStepEnum readingStep =  ApplicationStepEnum.NOT_SELECTED;

	private String applicationLabel;

	private int transactionCounter = UNKNOWN;

	private int leftPinTry = UNKNOWN;

	private int priority = 1;
	
	private float amount = UNKNOWN;

	private List<EmvTransactionRecord> listTransactions;

	public String getApplicationLabel() {
		return applicationLabel;
	}

	public void setApplicationLabel(final String applicationLabel) {
		this.applicationLabel = applicationLabel;
	}

	public int getTransactionCounter() {
		return transactionCounter;
	}

	public void setTransactionCounter(final int transactionCounter) {
		this.transactionCounter = transactionCounter;
	}

	public int getLeftPinTry() {
		return leftPinTry;
	}

	public void setLeftPinTry(final int leftPinTry) {
		this.leftPinTry = leftPinTry;
	}

	public List<EmvTransactionRecord> getListTransactions() {
		return listTransactions;
	}

	public void setListTransactions(final List<EmvTransactionRecord> listTransactions) {
		this.listTransactions = listTransactions;
	}

	public byte[] getAid() {
		return aid;
	}

	public void setAid(final byte[] aid) {
		if( aid != null) {
			this.aid = aid;
		}
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(final int priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(final Application arg0) {
		return priority - arg0.getPriority();
	}

	public ApplicationStepEnum getReadingStep() {
		return readingStep;
	}

	public void setReadingStep(ApplicationStepEnum readingStep) {
		this.readingStep = readingStep;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}
}
