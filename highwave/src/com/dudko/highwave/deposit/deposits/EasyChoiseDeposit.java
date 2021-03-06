package com.dudko.highwave.deposit.deposits;

import org.joda.time.*;

import com.dudko.highwave.bank.*;
import com.dudko.highwave.deposit.*;
import com.dudko.highwave.globalize.*;
import com.dudko.highwave.utils.*;

public class EasyChoiseDeposit extends Deposit {
	private int depositTerm;
	private float lowInterestRate = 0.1f;

	public EasyChoiseDeposit(DepositNames name, int depositTerm) {
		bank = BankFactory.GetBank(BankCode.HomeCreditBank);
		url = "http://www.homecredit.by/loans_and_services/legkiy_vybor/index.htm";
		currency = Currency.BYR;

		this.name = name;
		this.depositTerm = depositTerm;
		this.interestRate = this.interestRate(this.depositTerm);
	}

	@Override
	public DepositAccount calculateDeposit(long amount, int period) {
		long minOpenAmount = 1000000;
		if (amount < minOpenAmount) {
			return null;
		}

		int term = Math.min(period, depositTerm);
		LocalDate currentDate = MinskLocalDate.now();
		LocalDate endDate = currentDate.plusDays(term);

		float _interestRate = interestRate(term);
		float depositAmount = amount;

		DepositAccount account = new DepositAccount(this);
		account.addRecord(currentDate, depositAmount, interestRate, RecordDescriptions.MSG_000_Open_Deposit);

		currentDate = currentDate.plusDays(term);
		depositAmount = calculatePeriod(depositAmount, _interestRate, term);
		if (term == depositTerm) {
			account.addRecord(currentDate, depositAmount, interestRate, RecordDescriptions.MSG_001_Capitalization);
		} else {
			account.addRecord(currentDate, depositAmount, _interestRate, RecordDescriptions.MSG_002_Accrual_Of_Interest);
		}

		if (depositTerm < period) {
			int _period = period - depositTerm;
			currentDate = endDate.plusDays(_period);
			depositAmount = calculatePeriod(depositAmount, lowInterestRate, _period);
			account.addRecord(currentDate, depositAmount, lowInterestRate, RecordDescriptions.MSG_002_Accrual_Of_Interest);
			account.addRecord(currentDate, depositAmount, lowInterestRate, RecordDescriptions.MSG_003_Close_Deposit, true);
		} else if (term < depositTerm) {
			account.addRecord(endDate, depositAmount, _interestRate, RecordDescriptions.MSG_005_Early_Withdrawal_Of_Deposit, true);
		} else if (term == depositTerm) {
			account.addRecord(endDate, depositAmount, _interestRate, RecordDescriptions.MSG_003_Close_Deposit, true);
		}

		account.fillData();
		return account;
	}

	private float interestRate(int _period) {
		switch (depositTerm) {
		case 30:
			return interestRate30(_period);
		case 90:
			return interestRate90(_period);
		case 180:
			return interestRate180(_period);
		default:
			return 0.0f;
		}
	}

	private float interestRate30(int _period) {
		if (_period < 30) {
			return 0.1f;
		} else if (_period == 30) {
			return 27.0f;
		}

		return lowInterestRate;
	}

	private float interestRate90(int _period) {
		if (_period <= 30) {
			return 0.1f;
		} else if (_period <= 60) {
			return 1.0f;
		} else if (_period < 90) {
			return 5.0f;
		} else if (_period == 90) {
			return 28.0f;
		}

		return lowInterestRate;
	}

	private float interestRate180(int _period) {
		if (_period <= 30) {
			return 0.1f;
		} else if (_period <= 60) {
			return 1.0f;
		} else if (_period <= 90) {
			return 5.0f;
		} else if (_period < 180) {
			return 10.0f;
		} else if (_period == 180) {
			return 29.0f;
		}

		return lowInterestRate;
	}
}