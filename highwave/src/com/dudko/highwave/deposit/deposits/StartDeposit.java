package com.dudko.highwave.deposit.deposits;

import java.util.*;

import org.joda.time.*;

import com.dudko.highwave.bank.*;
import com.dudko.highwave.deposit.*;
import com.dudko.highwave.globalize.*;
import com.dudko.highwave.utils.*;

public abstract class StartDeposit extends Deposit {
	protected long minOpenAmount;
	protected float minDepositAmount;

	public StartDeposit() {
		bank = BankFactory.GetBank(BankCode.BelAgroPromBank);
		name = DepositNames.MSG_007_Start;
		url = "http://www.belapb.by/natural/deposit/start_/";
	}

	@Override
	public DepositAccount calculateDeposit(long amount, int period) {
		int depositTerm = 95;
		int capitalizationPeriod = 30;

		if (amount < minOpenAmount || period < capitalizationPeriod) {
			return null;
		} else if (amount < minDepositAmount && period < depositTerm) {
			return null;
		}

		LocalDate currentDate = MinskLocalDate.now();
		float _amount = amount;

		DepositAccount account = new DepositAccount(this);
		account.addRecord(currentDate, _amount, interestRate, RecordDescriptions.MSG_000_Open_Deposit);

		Set<Integer> setOfDays = new TreeSet<Integer>(Arrays.asList(0, 30, 60, 90, 95, period));
		Integer[] days = setOfDays.toArray(new Integer[0]);

		for (int i = 0; days[i] < depositTerm; i++) {
			int day = days[i + 1];
			int _period = day - days[i];

			currentDate = currentDate.plusDays(_period);
			_amount = calculatePeriod(_amount, interestRate, _period);

			boolean isLast = day == period || (day == depositTerm && depositTerm < period);

			if (day % capitalizationPeriod == 0) {
				account.addRecord(currentDate, _amount, interestRate, RecordDescriptions.MSG_001_Capitalization);
			}

			if (day == depositTerm) {
				account.addRecord(currentDate, _amount, interestRate, RecordDescriptions.MSG_003_Close_Deposit, isLast);
			} else if (day == period) {
				account.addRecord(currentDate, _amount, interestRate, RecordDescriptions.MSG_006_Partial_Withdrawal_Of_Deposit, isLast);
				_amount = minDepositAmount;
			}
		}

		account.fillData();
		return account;
	}
}