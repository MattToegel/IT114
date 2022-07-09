package LifeForLife.common;

public class PRHPayload extends Payload {
    private Vector2 position = new Vector2(0, 0);// current position
    private Vector2 heading = new Vector2(0, 0);// direction of movement
    private float rotation = 0;// player rotation

    public PRHPayload() {
        setPayloadType(PayloadType.SYNC_POSITION_ROTATION);
    }

    /**
     * @return the position
     */
    public Vector2 getPosition() {
        return new Vector2(position.x, position.y);
    }

    /**
     * @param position the position to set
     */
    public void setPosition(Vector2 position) {
        this.position.x = position.x;
        this.position.y = position.y;
    }

    /**
     * @return the rotation
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * @param rotation the rotation to set
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    /**
     * @return the heading
     */
    public Vector2 getHeading() {
        return heading;
    }

    /**
     * Values can be -1, 0, 1 depending on direction of coordinate
     * 
     * @param heading the heading to set
     */
    public void setHeading(Vector2 heading) {
        this.heading.x = GeneralUtils.clamp(heading.x, -1, 1);
        this.heading.y = GeneralUtils.clamp(heading.y, -1, 1);
    }

    @Override
    public String toString() {
        return String.format(
                "ClientId[%s], ClientName[%s], Type[%s], Number[%s], Message[%s], Position[%s], Rotation[%s], Heading[%s]",
                getClientId(),
                getClientName(), getPayloadType().toString(), getNumber(),
                getMessage(), getPosition(), getRotation(), getHeading());
    }
}
