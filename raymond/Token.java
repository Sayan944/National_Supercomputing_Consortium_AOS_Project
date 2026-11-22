
package raymond;

public class Token {

    private final String tokenId;

    public Token(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenId() {
        return tokenId;
    }

    @Override
    public String toString() {
        return "Token ID: " + tokenId;
    }
}
