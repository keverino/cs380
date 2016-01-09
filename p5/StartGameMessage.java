public final class StartGameMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final String player2;

    public StartGameMessage(String player2) {
        super(MessageType.START_GAME);
        this.player2 = player2;
    }

    public String getPlayer2() {
        return player2;
    }
}
