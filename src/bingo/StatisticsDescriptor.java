package bingo;

/**
 * <p>Class that contains data necessary to describe statistical test</p>
 */
public class StatisticsDescriptor {
    //type of test
    private String test;
    //correction method
    private String correctionTest;
    //over or under representation
    private String representation;
    //p value
    private String pValue;

    private String referenceSet;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getCorrectionTest() {
        return correctionTest;
    }

    public void setCorrectionTest(String correctionTest) {
        this.correctionTest = correctionTest;
    }

    public String getRepresentation() {
        return representation;
    }

    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    public String getpValue() {
        return pValue;
    }

    public void setpValue(String pValue) {
        this.pValue = pValue;
    }

    public String getReferenceSet() {
        return referenceSet;
    }

    public void setReferenceSet(String referenceSet) {
        this.referenceSet = referenceSet;
    }
}
