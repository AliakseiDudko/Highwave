package com.dudko.highwave.deposit.deposits;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.*;

import com.dudko.highwave.bank.*;
import com.dudko.highwave.deposit.*;

public class TwoByTwoDeposit extends Deposit {
	public TwoByTwoDeposit() {
		id = 5;
		bank = BankFactory.GetBank(BankCode.BelGazPromBank);
		name = "2х2";
		url = "http://belgazprombank.by/personal_banking/vkladi_depoziti/v_nacional_noj_valjute/vklad_2h2/";
		currency = Currency.BYR;
		interestRate = 30.0f;
	}

	@Override
	public DepositAccount calculateDeposit(float amount, int period) {
		DateTime currentDate = DateTime.now();
		DateTime endDate = currentDate.plusMonths(2);

		if (endDate.isAfter(currentDate.plusDays(period))) {
			return null;
		}

		List<AccountStatementRecord> list = new ArrayList<AccountStatementRecord>();

		float _amount = amount;
		addRecord(list, currentDate, _amount, interestRate, "Открытие вклада.");

		for (int i = 0; i < 2; i++) {
			DateTime previousDate = currentDate;
			currentDate = currentDate.plusMonths(1);

			int _period = Days.daysBetween(previousDate, currentDate).getDays();
			_amount = calculatePeriod(_amount, interestRate, _period);
			addRecord(list, currentDate, _amount, interestRate, "Капитализация.");
		}

		addRecord(list, endDate, _amount, interestRate, "Закрытие вклада.", true);

		return new DepositAccount(this, list);
	}
}
