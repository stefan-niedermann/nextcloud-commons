package it.niedermann.nextcloud.sso.glide;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.bumptech.glide.load.model.Headers;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static it.niedermann.nextcloud.sso.glide.SingleSignOnStreamFetcher.X_HEADER_SSO_ACCOUNT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SingleSignOnOriginHeaderTest {
    @Test
    public void addSpecialHeader() {
        final String testAccountName = "foo@example.com";

        final Headers headers = new SingleSignOnOriginHeader(testAccountName);
        assertEquals(2, headers.getHeaders().size());
        assertTrue(headers.getHeaders().containsKey(X_HEADER_SSO_ACCOUNT_NAME));

        final Headers headersWithExistingHeaders = new SingleSignOnOriginHeader(testAccountName, () -> new HashMap<String, String>() {{
            put("FOO", "BAR");
            put("MEOW", "WOOF");
        }});
        assertEquals(4, headersWithExistingHeaders.getHeaders().size());
        assertTrue(headersWithExistingHeaders.getHeaders().containsKey(X_HEADER_SSO_ACCOUNT_NAME));
    }
}
