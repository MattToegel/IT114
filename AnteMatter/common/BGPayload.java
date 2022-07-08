package AnteMatter.common;

public class BGPayload extends Payload{
    private long bet;//holds the bet or the players matter total
    private long guess;//holds the player's guess or the max guess allowed
    /**
     * @return the bet
     */
    public long getBet() {
        return bet;
    }
    /**
     * @param bet the bet to set
     */
    public void setBet(long bet) {
        this.bet = bet;
    }
    /**
     * @return the guess
     */
    public long getGuess() {
        return guess;
    }
    /**
     * @param guess the guess to set
     */
    public void setGuess(long guess) {
        this.guess = guess;
    }
    @Override
    public String toString() {
        return String.format("ClientId[%s], ClientName[%s], Type[%s], Message[%s], Bet[%s], Guess[%s]", getClientId(),
                getClientName(), getPayloadType().toString(),
                getMessage(), getBet(), getGuess());
    }
}
