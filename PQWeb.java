// For opening URLs
import java.awt.Desktop; 
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public class PQWeb {
	
    public static void navigate(String url) {
        // The JavaScript window.open() function opens a URL in a new window or tab.
        // In Java, you can achieve similar functionality using the Desktop class
        // (for desktop applications) or by interacting with a WebView (if you're
        // using a WebView in a UI framework like JavaFX).

        // Here's how you can do it using the Desktop class (for desktop apps):
		/*
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                // Handle exceptions (e.g., log the error, display a message)
                System.err.println("Error opening URL: " + e.getMessage());
            }
        } else {
            // Handle the case where Desktop is not supported (e.g., log a message)
            System.err.println("Desktop is not supported on this platform.");
        }
		*/
        // If you are using a WebView in a UI framework (like JavaFX), you can use
        // the WebView's engine to load the URL:

        // Example (JavaFX with WebView):
        webView.getEngine().load(url);


        // If you are not using a UI framework (e.g., a server-side application),
        // then this function might not be relevant.  You might use a logging
        // framework to record the URL that "should" have been navigated to.
    }

    public static int lfsr(String pt, int salt) {
        int result = salt;
        for (int k = 0; k < pt.length(); ++k) {
            result = (result << 1) ^ (1 & ((result >> 31) ^ (result >> 5))) ^ pt.charAt(k);
        }
        for (int kk = 0; kk < 10; ++kk) {
            result = (result << 1) ^ (1 & ((result >> 31) ^ (result >> 5)));
        }
        return result;
    }
	

    public static String validator(String url) {
        url = url.substring(url.indexOf("cmd="));
        return String.valueOf(lfsr(url, (int) ((Map<String, Object>) game.get("online")).get("passkey"))); // Cast passkey to int
    }

    public static CompletableFuture<Void> brag(char trigger, boolean andSeeIt) {
        return saveGame().thenRun(() -> {
            if (game.containsKey("online")) {
                Map<String, Object> online = (Map<String, Object>) game.get("online");
                String host = (String) online.get("host");
                int passkey = (int) online.get("passkey");
                String realm = (String) online.get("realm");

                String url = host + "cmd=b&t=" + trigger;

                Map<String, Object> traits = (Map<String, Object>) game.get("Traits"); // Get Traits Map
                for (Map.Entry<String, Object> entry : traits.entrySet()) {
                    String trait = entry.getKey();
                    String value = entry.getValue().toString(); // Get String representation of value
                    try {
                        url += "&" + trait.substring(0, 1).toLowerCase() + "=" + URLEncoder.encode(value, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        System.err.println("Error encoding URL parameter: " + e.getMessage());
                    }
                }

                int exp = (int) ((ProgressBar) expBar).position(); // Cast expBar to ProgressBar
                String bestEquip = (String) game.get("bestequip");
                String bestSpell = (String) game.get("bestspell");
                String bestStat = (String) game.get("beststat");
                String bestPlot = (String) game.get("bestplot");
                String motto = (String) game.getOrDefault("motto", "");

                try {
                    url += "&x=" + exp;
                    url += "&i=" + URLEncoder.encode(bestEquip == null ? "" : bestEquip, "UTF-8"); // Handle nulls
                    url += "&z=" + URLEncoder.encode(bestSpell == null ? "" : bestSpell, "UTF-8"); // Handle nulls
                    url += "&k=" + URLEncoder.encode(bestStat == null ? "" : bestStat, "UTF-8"); // Handle nulls
                    url += "&a=" + URLEncoder.encode(bestPlot == null ? "" : bestPlot, "UTF-8"); // Handle nulls
                    url += "&h=" + URLEncoder.encode(realm, "UTF-8");
                    url += revString; // Make sure revString is a String variable
                    url = standardizeUrl(url); // Implement standardizeUrl
                    url += "&p=" + validator(url);
                    url += "&m=" + URLEncoder.encode(motto, "UTF-8");

                    // Use HttpClient to make the request
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .build();

                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenAccept(response -> {
                                String body = response.body();
                                if (body != null) { // Check for null body
                                    String[] parts = body.split("\\r?\\n", 2); // Split on newline
                                    if (parts.length > 0 && parts[0].equalsIgnoreCase("report")) {
                                        if (parts.length > 1) {
                                            // Update UI on JavaFX thread
                                            Platform.runLater(() -> {
                                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                                alert.setTitle("Report");
                                                alert.setContentText(parts[1]);
                                                alert.showAndWait();
                                            });
                                        }
                                    } else if (andSeeIt) {
                                        navigate(host + "name=" + urlEncode((String) ((Map<String, Object>) game.get("Traits")).get("Name"))); // Cast Traits and Name
                                    }
                                }
                            })
                            .exceptionally(ex -> {
                                System.err.println("Error making HTTP request: " + ex.getMessage());
                                return null; // Or handle the exception as needed
                            });
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Error encoding URL parameter: " + e.getMessage());
                }
            }
        });
        return CompletableFuture.completedFuture(null); // Return a completed future
    }


    private String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println("Error encoding URL: " + e.getMessage());
            return ""; // Or throw an exception
        }
    }

    public static String standardizeUrl(String url) {
        // The JavaScript code creates a dummy <a> element, sets its href to the URL,
        // and then retrieves the normalized href.  This leverages the browser's URL
        // parsing and normalization logic.

        // In Java, you can achieve the same result using the URI class:

        try {
            URI uri = new URI(url);
            return uri.normalize().toString(); // Normalize and convert back to string
        } catch (URISyntaxException e) {
            // Handle the exception (e.g., log the error, return the original URL, etc.)
            System.err.println("Error standardizing URL: " + e.getMessage());
            return url; // Or throw an exception if you want to enforce valid URLs
        }

        // If you are not in a browser environment (e.g., a server-side application),
        // then this function is still useful for normalizing URLs.
    }

    public static CompletableFuture<Void> guildify(String guild) {
        return CompletableFuture.runAsync(() -> {
            if (!game.containsKey("online") || guild == null) return;

            game.put("guild", guild);

            Map<String, Object> online = (Map<String, Object>) game.get("online");
            String host = (String) online.get("host");
            String realm = (String) online.get("realm");

            String url = host + "cmd=guild";

            Map<String, Object> traits = (Map<String, Object>) game.get("Traits");
            for (Map.Entry<String, Object> entry : traits.entrySet()) {
                String trait = entry.getKey();
                String value = entry.getValue().toString();
                try {
                    url += "&" + trait.substring(0, 1).toLowerCase() + "=" + URLEncoder.encode(value, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Error encoding URL parameter: " + e.getMessage());
                }
            }

            try {
                url += "&h=" + URLEncoder.encode(realm, "UTF-8");
                url += revString;
                url += "&guild=" + URLEncoder.encode(guild, "UTF-8");
                url = standardizeUrl(url);
                url += "&p=" + validator(url);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            String body = response.body();
                            if (body != null) {
                                String[] parts = body.split("\\|"); // Split by pipe character

                                if (parts.length > 0 && !parts[0].isEmpty()) {
                                    String alertMessage = parts[0];
                                    Platform.runLater(() -> {
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setTitle("Guild");
                                        alert.setContentText(alertMessage);
                                        alert.showAndWait();
                                    });
                                }

                                if (parts.length > 1 && !parts[1].isEmpty()) {
                                    String navigationUrl = parts[1];
                                    navigate(navigationUrl);
                                }
                            }
                        })
                        .exceptionally(ex -> {
                            System.err.println("Error making HTTP request: " + ex.getMessage());
                            return null;
                        });

            } catch (UnsupportedEncodingException e) {
                System.err.println("Error encoding URL parameter: " + e.getMessage());
            }
        });
        return CompletableFuture.completedFuture(null);
    }
	
}	