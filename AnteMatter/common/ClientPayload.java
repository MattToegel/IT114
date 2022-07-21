package AnteMatter.common;

public class ClientPayload extends Payload{
    private String formattedName;

    /**
     * @return the formattedName
     */
    public String getFormattedName() {
        return formattedName;
    }

    /**
     * @param formattedName the formattedName to set
     */
    public void setFormattedName(String formattedName) {
        this.formattedName = formattedName;
    }
    @Override
    public String toString() {
        return String.format("ClientId[%s], ClientName[%s], FormattedName[%s], Type[%s], Message[%s]", getClientId(),
                getClientName(), getFormattedName(), getPayloadType().toString(),
                getMessage());
    }
}
