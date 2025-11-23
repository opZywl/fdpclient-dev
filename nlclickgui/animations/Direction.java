package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations;

public enum Direction {
    FORWARDS,
    BACKWARDS;

    public Direction opposite() {
        if (this == Direction.FORWARDS) {
            return Direction.BACKWARDS;
        } else return Direction.FORWARDS;
    }

}
