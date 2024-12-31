package net.flectone.pulse.util;

public record Pair<L, R>(L left, R right) {

    public L getKey() {
        return this.left();
    }

    public R getValue() {
        return this.right();
    }

}
