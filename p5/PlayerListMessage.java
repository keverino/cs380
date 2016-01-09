public final class PlayerListMessage extends Message {

    private static final long serialVersionUID = 0L;

    private final String[] players;

    public PlayerListMessage(String[] players) {
        super(MessageType.PLAYER_LIST);
        this.players = players;
    }

    public String[] getPlayers() {
        return players;
    }
}
