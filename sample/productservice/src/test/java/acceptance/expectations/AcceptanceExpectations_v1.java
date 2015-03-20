package acceptance.expectations;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

/**
 * Created by i303874 on 3/20/15.
 */
public class AcceptanceExpectations_v1 {
    private String postUrl;

    private String getUrl;

    public AcceptanceExpectations_v1(String postUrl, String getUrl) {
        this.postUrl = checkNotNull(postUrl);
        this.getUrl = checkNotNull(getUrl);
    }

    private String getGetUrl(String id) {
        return getUrl + id;
    }

    private String getPostUrl() {
        return postUrl;
    }

    public void givenValidInputToGetThenReturnsProduct() {
        // prepare
        Response postResponse = given().body("{\"name\":\"test\", \"description\":\"test\"}").and().contentType(ContentType.JSON).when().post(getPostUrl());
        postResponse.then().assertThat().statusCode(200);
        JsonPath postPath = new JsonPath(postResponse.asString());
        String id = postPath.get("id");
        String description = postPath.get("description");

        // test
        Response getResponse = get(getGetUrl(id));
        getResponse.then().assertThat().statusCode(200);
        JsonPath getPath = new JsonPath(getResponse.asString());
        assertEquals(id, getPath.get("id"));
        assertEquals(description, getPath.get("description"));
    }
}
