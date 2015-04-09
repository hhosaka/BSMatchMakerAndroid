package com.nag.android.bs_match_maker;

import com.nag.android.util.AbstractCharSequence;

import java.io.Serializable;

class Result implements Serializable{
    private static final long serialVersionUID = Game.serialVersionUID;
    private int[] point;

    Result(int point1, int point2){
        this.point = new int[]{point1, point2};
    }

    Result(){
        this(0,0);
    }

    public int getPoint(int index) {
        return point[index];
    }
    public int getDiff(){return point[0]-point[1];}

    @Override
    public String toString() {
        return point[0]+"-"+point[1];
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Result) {
            return point[0] == ((Result) o).point[0]
                    && point[1] == ((Result) o).point[1];
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (point[0]<<8)|point[1];
    }
}

