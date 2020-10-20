package put.io.selenium.atm;

/**
 * Represents an ATM machine. It maintains the state of the ATM and enables to 
 * perform operations like inserting and withdrawing a credit/debit card, 
 * as well as withdrawing money.
 *
 */
public class AtmCardInfo {

	final int RIGHT_PIN = 1525;
	
	boolean isCardIn = false;
	
	int pinTriesLeft = 3;
	
	int balance = 1000;

	boolean isCardLocked = false;

	boolean pinWasOk = false;
	
	/**
	 * It corresponds to the operation of inserting a credit/debit card 
	 * into the ATM machine. It changes the state of the ATM.
	 */
	public void cardIn() {
		if (isCardIn) {
			return;
		}
		isCardIn = true;
		pinWasOk = false;
	}
	
	/**
	 * It enables to enter a PIN number for a card that was inserted {@link #cardIn()}. 
	 * @param pin is a PIN number of the card.
	 * @return <code>true</code> if the given PIN number is correct for the inserted card.
	 */
	public boolean tryPin(String pin) {
		if (!isCardIn || isCardLocked || pinTriesLeft <= 0) {
			return false;
		}
		
		if (checkPin(pin)) {
			pinWasOk = true;
			pinTriesLeft = 3;
			return true;
		}
		
		--pinTriesLeft;
		if (pinTriesLeft <= 0) {
			isCardLocked = true;
		}
		return false;
	}
	
	private boolean checkPin(String pin) {
		try {
			return Integer.parseInt(pin) == RIGHT_PIN;
		} catch (NumberFormatException nf) {
			return false;
		}
	}

	/**
	 * It enables withdrawing cash from the ATM. 
	 * @param amount is the amount of money to be withdrawn from the ATM.
	 * @return <code>null</code> if operation was successful or an error message if it failed.
	 */
	public String tryWithdraw(String amount) {
		final int intAmount;
		try {
			intAmount = Integer.parseInt(amount);
		} catch (NumberFormatException nf) {
			return "Błędny format kwoty";
		}

		if (intAmount <= 0) {
			return "Błędna wartość kwoty";
		}
		if (intAmount > balance) {
			return "Niewystarczające środki";
		}
		
		balance -= intAmount;
		
		return null;
	}

	/**
	 * Enables to remove the card that was inserted from the ATM.
	 */
	public void cardOut() {
		isCardIn = false;
	}
}
