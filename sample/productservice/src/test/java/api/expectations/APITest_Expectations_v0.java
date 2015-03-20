package api.expectations;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Created by i303874 on 3/20/15.
 */
public class APITest_Expectations_v0 {
    private String getUrl;

    public APITest_Expectations_v0(String getUrl) {
        this.getUrl = checkNotNull(getUrl);
    }

    public void givenValidInputToGetThenReturnsProduct() {
        get(getUrl).then().assertThat().statusCode(200).assertThat().body(matchesJsonSchemaInClasspath("product_v0.json"));
    }

    public void givenValidInputToGetThenReturns500() {
        get(getUrl).then().assertThat().statusCode(500);
    }
}
