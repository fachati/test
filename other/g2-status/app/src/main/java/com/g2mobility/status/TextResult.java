package com.g2mobility.status;

/**
 * Created by fachati on 18/04/16.
 */
public class TextResult {

     private String ligne1;
     private String ligne2;
     private String ligne3;




    public TextResult(String ligne1, String ligne2, String ligne3) {
        this.ligne1 = ligne1;
        this.ligne2 = ligne2;
        this.ligne3 = ligne3;
    }
    public TextResult(String ligne1, String ligne2) {
        this.ligne1 = ligne1;
        this.ligne2 = ligne2;
        this.ligne3 = "";
    }

    @Override
    public String toString() {
        return "TextResult{" +
                "ligne1='" + ligne1 + '\'' +
                ", ligne2='" + ligne2 + '\'' +
                ", ligne3='" + ligne3 + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextResult)) return false;

        TextResult that = (TextResult) o;

        if (!ligne1.equals(that.ligne1)) return false;
        if (!ligne2.equals(that.ligne2)) return false;
        return ligne3.equals(that.ligne3);

    }

    @Override
    public int hashCode() {
        int result = ligne1.hashCode();
        result = 31 * result + ligne2.hashCode();
        result = 31 * result + ligne3.hashCode();
        return result;
    }

    public String getLigne3() {
        return ligne3;
    }

    public void setLigne3(String ligne3) {
        this.ligne3 = ligne3;
    }

    public String getLigne1() {
        return ligne1;
    }

    public String getLigne2() {
        return ligne2;
    }

    public void setLigne2(String ligne2) {
        this.ligne2 = ligne2;
    }

    public void setLigne1(String ligne1) {
        this.ligne1 = ligne1;
    }
}
