package com.dudko.highwave.deposit.deposits;

import org.joda.time.*;

import com.dudko.highwave.bank.*;
import com.dudko.highwave.deposit.*;
import com.dudko.highwave.globalize.*;
import com.dudko.highwave.utils.*;

public class OnWaveDeposit extends Deposit {
	public OnWaveDeposit() {
		bank = BankFactory.GetBank(BankCode.HomeCreditBank);
		name = DepositNames.MSG_004_OnWave;
		url = "http://www.homecredit.by/loans_and_services/na_volne/";
		currency = Currency.BYR;
		interestRate = 29.0f;
	}

	@Override
	public DepositAccount calculateDeposit(long amount, int period) {
		long minOpenAmount = 1000000;
		float lowInterestRate = 0.1f;
		if (amount < minOpenAmount) {
			return null;
		}

		int depositTerm = 10;
		int term = Math.min(period, 30);
		LocalDate currentDate = MinskLocalDate.now();
		LocalDate endDate = currentDate.plusDays(term);

		float _amount = amount;

		DepositAccount account = new DepositAccount(this);
		account.addRecord(currentDate, _amount, interestRate, RecordDescriptions.MSG_000_Open_Deposit);

		LocalDate previousDate = currentDate;
		currentDate = currentDate.plusDays(depositTerm);
		while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
			_amount = calculatePeriod(_amount, interestRate, depositTerm);
			account.addRecord(currentDate, _amount, interestRate, RecordDescriptions.MSG_001_Capitalization);

			previousDate = currentDate;
			currentDate = currentDate.plusDays(depositTerm);
		}

		int _period = Days.daysBetween(previousDate, endDate).getDays();
		if (_period == 0) {
			account.addRecord(endDate, _amount, interestRate, RecordDescriptions.MSG_003_Close_Deposit, true);
		} else {
			_amount = calculatePeriod(_amount, lowInterestRate, _period);
			account.addRecord(endDate, _amount, lowInterestRate, RecordDescriptions.MSG_002_Accrual_Of_Interest);
			account.addRecord(endDate, _amount, lowInterestRate, RecordDescriptions.MSG_005_Early_Withdrawal_Of_Deposit, true);
		}

		account.fillData();
		return account;
	}
}