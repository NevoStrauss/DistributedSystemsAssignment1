public class Message {

    private final String pdfPath;
    private final String operation;

    public Message(String command) throws IllegalAccessException {
        String[] arrCommands = command.split(" ");
        validateCommand(arrCommands);
        this.pdfPath = arrCommands[0];
        this.operation =arrCommands[1];
    }

    public String getOperation() {
        return operation;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    private void validateCommand(String[] arrCommands) throws IllegalAccessException {
        if (arrCommands.length != 2)
            throw new IllegalAccessException();
    }
}
