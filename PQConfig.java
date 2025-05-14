import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Pour Storage et Roster
import java.util.prefs.Preferences;

// Pour la classe CookieStorage
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/*  Pour URLEncode
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
*/

// Pour SqlStorage
import java.sql.*;

// Pour Roaster
import java.util.HashMap;
//import java.util.prefs.Preferences;
//import java.util.Map;
//import com.google.gson.Gson; // Import Gson library
// Remplacé par
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// Pour la partie conversion
import java.math.BigDecimal;
import java.math.RoundingMode;


public class PQConfig {

	protected String PrefName = "ProgressQuestJava_V1"; // protected pour utilisation dans la sous classe LocalStorage
	
    public static String tabulate(List<?> list) {
        StringBuilder result = new StringBuilder();
        for (Object item : list) {
            if (item instanceof List) {
                List<?> subList = (List<?>) item;
                if (subList.size() == 2 && subList.get(1) instanceof String && !((String) subList.get(1)).isEmpty()) {
                    result.append("   ").append(subList.get(0)).append(": ").append(subList.get(1)).append("\n");
                }
            } else {
                result.append("   ").append(item).append("\n");
            }
        }
        return result.toString();
    }

    public static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String template(String tmpl, Map<String, ?> data) {
        Pattern pattern = Pattern.compile("\\$([_A-Za-z.]+)");
        Matcher matcher = pattern.matcher(tmpl);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String p1 = matcher.group(1);
            Map<String, ?> dict = data;
            String[] parts = p1.split("\\.");

            for (String v : parts) {
                if (dict == null) break; // Important : gérer le cas où dict devient null en cours de parcours
                if (v.equals("___")) {
                    dict = (Map<String, ?>) tabulate((List<?>) dict); // Cast vers List<?>
                } else {
                    v = v.replace("_", " ");
                    Object value = dict.get(v);

                    if (value instanceof String) {
                        value = escapeHtml((String) value);
                    }
                    dict = (Map<String, ?>) value; // On continue à parcourir l'objet.
                }
            }

            String replacement = (dict == null) ? "" : dict.toString(); // Utiliser toString() pour afficher la valeur
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static class Mash {
        private int n = 0xefc8249d;

        public double mash(String data) {
            for (int i = 0; i < data.length(); i++) {
                n += data.charAt(i);
                double h = 0.02519603282416938 * n;
                n = (int) h;
                h -= n;
                h *= n;
                n = (int) h;
                h -= n;
                n += h * 0x100000000L; // 2^32
            }
            return (n >>> 0) * 2.3283064365386963e-10; // 2^-32
        }

        public String getVersion() {
            return "Mash 0.9";
        }
    }


    private double s0 = 0;
    private double s1 = 0;
    private double s2 = 0;
    private int c = 1;
    private char[] args; // Changed to char[]

    public Alea(char... args) { // Changed to char... args
        if (args.length == 0) {
            args = String.valueOf(System.currentTimeMillis()).toCharArray(); // Use current time as seed if no arguments
        }
        this.args = args;

        mash = new Mash();
        s0 = mash.mash(" ");
        s1 = mash.mash(" ");
        s2 = mash.mash(" ");

        for (char arg : args) { // Iterate over char[]
            s0 -= mash.mash(String.valueOf(arg)); // Convert char to String
            if (s0 < 0) {
                s0 += 1;
            }
            s1 -= mash.mash(String.valueOf(arg)); // Convert char to String
            if (s1 < 0) {
                s1 += 1;
            }
            s2 -= mash.mash(String.valueOf(arg)); // Convert char to String
            if (s2 < 0) {
                s2 += 1;
            }
        }
        mash = null;
    }
	
    public double random() {
        double t = 2091639.0 * s0 + c * 2.3283064365386963e-10; // 2^-32
        s0 = s1;
        s1 = s2;
        s2 = t - (c = (int) t);
        return s2;
    }

    public long uint32() {
        return (long) (random() * 0x100000000L); // 2^32
    }

    public double fract53() {
        return random() + ((long) (random() * 0x200000) | 0) * 1.1102230246251565e-16; // 2^-53
    }

    public String getVersion() {
        return "Alea 0.9";
    }

    public Object[] getArgs() {
        return args;
    }

    public double[] getState() {
        return new double[]{s0, s1, s2, c};
    }

    public void setState(double[] newState) {
        s0 = newState[0];
        s1 = newState[1];
        s2 = newState[2];
        c = (int) newState[3];
    }

    private static Alea seed = new Alea(); // Static seed instance

    public static int Random(int n) {  // Changed to int for consistency
        return (int) (seed.uint32() % n); // Cast to int
    }

    public static double[] randseed(double[] set) {
        seed.setState(set);
        return seed.getState();
    }

    public static <T> T Pick(List<T> a) { // Generic method
        if (a == null || a.isEmpty()) {
          return null; // Or throw an exception, depending on how you want to handle empty lists.
        }
        return a.get(Random(a.size()));
    }


    private static final List<String> KParts1 = Arrays.asList("br", "cr", "dr", "fr", "gr", "j", "kr", "l", "m", "n", "pr", "", "", "r", "sh", "tr", "v", "wh", "x", "y", "z");
    private static final List<String> KParts2 = Arrays.asList("a", "a", "e", "e", "i", "i", "o", "o", "u", "u", "ae", "ie", "oo", "ou");
    private static final List<String> KParts3 = Arrays.asList("b", "ck", "d", "g", "k", "m", "n", "p", "t", "v", "x", "z");

    private static final List<List<String>> KParts = Arrays.asList(KParts1, KParts2, KParts3); // List of Lists

    public static String GenerateName() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <= 5; ++i) {
            result.append(Pick(KParts.get(i % 3))); // Use Pick with the correct List
        }
        String name = result.toString();
        return name.substring(0, 1).toUpperCase() + name.substring(1); // Improved capitalization
    }


	public class LocalStorage {

		private Preferences prefs;

		public LocalStorage() {
			// Use the application's node by default
			prefs = Preferences.userRoot().node(PrefName); 
		}
		
		public LocalStorage(String nodeName) {
			// Use a specific node for your application to avoid conflicts
			prefs = Preferences.userRoot().node(nodeName); 
		}

		public String getItem(String key) {
			return prefs.get(key, null); // Returns null if the key doesn't exist
		}

		public void setItem(String key, String value) {
			prefs.put(key, value);
		}

		public void removeItem(String key) {
			prefs.remove(key);
		}

	}



	// In servlet environment only
	public class CookieStorage {

		private HttpServletRequest request;
		private HttpServletResponse response;

		public CookieStorage(HttpServletRequest request, HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}

		public String getItem(String key) {
			String result = null;
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(key)) {
						try {
							result = URLDecoder.decode(cookie.getValue(), "UTF-8"); // Decode
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace(); // Handle the exception appropriately
						}
						break;
					}
				}
			}
			return result;
		}


		public void setItem(String key, String value) {
			try {
				String encodedValue = URLEncoder.encode(value, "UTF-8"); // Encode
				Cookie cookie = new Cookie(key, encodedValue);
				response.addCookie(cookie);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(); // Handle the exception appropriately
			}
		}

		public void removeItem(String key) {
			Cookie cookie = new Cookie(key, "");
			cookie.setMaxAge(0); // Set the max age to 0 to delete the cookie
			response.addCookie(cookie);
		}

	}




	public class SqlStorage {

		private Connection db;

		public SqlStorage(String dbPath) throws SQLException {
			// Use JDBC to connect to the SQLite database
			db = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

			// Create the table if it doesn't exist
			try (Statement stmt = db.createStatement()) {
				stmt.execute("CREATE TABLE IF NOT EXISTS Storage(key TEXT UNIQUE, value TEXT)");
			}
		}

		public String getItem(String key) throws SQLException {
			try (PreparedStatement stmt = db.prepareStatement("SELECT value FROM Storage WHERE key=?")) {
				stmt.setString(1, key);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						return rs.getString("value");
					} else {
						return null; // Return null if key not found
					}
				}
			}
		}

		public void setItem(String key, String value) throws SQLException {
			try (PreparedStatement stmt = db.prepareStatement("INSERT OR REPLACE INTO Storage (key,value) VALUES (?,?)")) {
				stmt.setString(1, key);
				stmt.setString(2, value);
				stmt.executeUpdate();
			}
		}

		public void removeItem(String key) throws SQLException {
			try (PreparedStatement stmt = db.prepareStatement("DELETE FROM Storage WHERE key=?")) {
				stmt.setString(1, key);
				stmt.executeUpdate();
			}
		}

		public void close() throws SQLException {
			if (db != null) {
				db.close();
			}
		}
	}


    public static String UrlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replace("%20", "+");
        } catch (UnsupportedEncodingException e) {
            // Handle the exception appropriately (e.g., log it, throw a custom exception)
            //e.printStackTrace();
            return null; // Or throw an exception
        }
    }


	public class Roster extends LocalStorage {

		private transient Map<String, Object> games = new HashMap<>(); // Transient: not stored

		public void loadRoster(RosterCallback callback) {
			String rosterStr = getItem("roster");
			Map<String, Object> roster = new HashMap<>(); // Initialize to an empty map

			if (rosterStr != null) {
				try {
					//Gson gson = new Gson(); // Use Gson for JSON parsing
					//roster = gson.fromJson(rosterStr, Map.class); // Deserialize to Map<String, Object>
					ObjectMapper objectMapper = new ObjectMapper(); // Use Jackson's ObjectMapper
					roster = objectMapper.readValue(rosterStr, Map.class); // Deserialize using Jackson
				} catch (JsonProcessingException e) { //(com.google.gson.JsonSyntaxException e) {
					// Handle JSON parsing errors
					System.err.println("Error parsing roster: " + e.getMessage());
					// You might want to provide a default value or handle the error differently
				}
			}

			games = roster; // Update the games field
			if (callback != null) {
				callback.onRosterLoaded(roster);
			}
		}


		public void loadSheet(String name, SheetCallback callback) {
			loadRoster(roster -> {
				if (callback != null) {
					callback.onSheetLoaded((Map<String, Object>) roster.get(name)); // Cast to Map<String, Object>
				}
			});
		}

		public void storeRoster(Map<String, Object> roster, Callback callback) {
			this.games = roster;
			//Gson gson = new Gson();
			//String rosterStr = gson.toJson(roster);
			ObjectMapper objectMapper = new ObjectMapper();
			String rosterStr = objectMapper.writeValueAsString(roster); // Serialize with Jackson

			try {
				setItem("roster", rosterStr);
				if (callback != null) {
					callback.onComplete();
				}
			} catch (Exception e) { // Catch a broader exception type for this example.  Be specific in production code.
				// Simulate quota exceeded error (in a real app, you would check for the actual error)
				if (e.getMessage().contains("QUOTA_EXCEEDED_ERR")) {  // Replace with actual quota check if possible
					System.err.println("Quota exceeded!"); // Log the error.  Use a proper logging framework in production.
					// Reset the storeRoster function (as in the JavaScript example)
					// In Java, you can't dynamically redefine methods like in JavaScript.
					// A better approach would be to handle this error in the calling code.
					if (callback != null) {
						callback.onComplete(); // Still call the callback for consistency
					}
				} else {
					throw new RuntimeException("Error storing roster", e); // Re-throw as unchecked exception
				}
			}
		}

		// TODO : Implementer l'interface Game=>Roster
		public void addToRoster(Map<String, Object> newguy, Callback callback) {
			loadRoster(games -> {
				String name = (String) ((Map<String, Object>) newguy.get("Traits")).get("Name"); // Assuming "Traits" and "Name" exist
				games.put(name, newguy);
				storeRoster(games, callback);
			});
		}


		public interface RosterCallback {
			void onRosterLoaded(Map<String, Object> roster);
		}

		public interface SheetCallback {
			void onSheetLoaded(Map<String, Object> sheet);
		}

		public interface Callback {
			void onComplete();
		}
	}

	// COnvesrion

    // Equivalent of JavaScript's Number.prototype.div
    public static int div(double dividend, double divisor) {
        double result = dividend / divisor;
        return (result < 0) ? (int) Math.ceil(result) : (int) Math.floor(result);
    }

    // Using BigDecimal for precise calculations (recommended)
    public static int divBigDecimal(BigDecimal dividend, BigDecimal divisor) {
        BigDecimal result = dividend.divide(divisor, 0, RoundingMode.FLOOR); // Round towards negative infinity
        return result.intValue();
    }

    public static int LevelUpTime(int level) { // seconds
        // 20 minutes for level 1
        // exponential increase after that
		// TODO : ajouter la possibilité d'accélérer avec la triche (CHEAT)
        return Math.round((20 + Math.pow(1.15, level)) * 60);
    }

	

    public static void main(String[] args) {
        List<List<String>> myList = Arrays.asList(
                Arrays.asList("Name", "John Doe"),
                Arrays.asList("Age", "30"),
                Arrays.asList("City", "New York")
        );

        String tabulated = tabulate(myList);
        System.out.println(tabulated);

        Map<String, Object> data = Map.of(
                "character", Map.of(
                        "Name", "Bob",
                        "Race", "Half-elf",
                        "Stats", myList
                )
        );


        String tmpl = "Character: $character.Name ($character.Race)\nStats:\n$character.Stats.___";
        String result = template(tmpl, data);
        System.out.println(result);

        Mash m = new Mash();
        System.out.println(m.mash("test"));
		
		
        Alea alea = new Alea('1', 't', 'e', 's', 't', '3', '.', '1', '4'); // Pass char[]
        System.out.println(alea.random());
        System.out.println(alea.uint32());
        System.out.println(alea.fract53());
        System.out.println(alea.getVersion());

        double[] state = alea.getState();
        System.out.println("State: " + Arrays.toString(state));

        Alea alea2 = new Alea(); // No arguments, seed will be the current time
        System.out.println(alea2.random());
		
		System.out.println("Generated name: " + GenerateName());
        System.out.println("Generated name: " + GenerateName());
        System.out.println("Generated name: " + GenerateName());


		// Test Storage
        LocalStorage storage = new LocalStorage(); // was ("myApplication"); 

        // Setting items
        storage.setItem("name", "John Doe");
        storage.setItem("age", "30");

        // Getting items
        String name = storage.getItem("name");
        String age = storage.getItem("age");

        System.out.println("Name: " + name); // Output: Name: John Doe
        System.out.println("Age: " + age);   // Output: Age: 30

        // Removing an item
        storage.removeItem("age");
        String ageAfterRemoval = storage.getItem("age");
        System.out.println("Age after removal: " + ageAfterRemoval); // Output: Age after removal: null

        //Demonstration of a default value:
        String city = storage.getItem("city"); //Key does not exist
        System.out.println("City: " + city); //Output: City: null

        //You can provide a default value if you want to:
        String cityWithDefault = storage.getItem("city");
        System.out.println("City with default: " + cityWithDefault); //Output: City with default: null




        // This example requires a servlet environment, so it can't be run directly like this.
        // You would typically use this class in a servlet's doGet or doPost method.

        // Example (Illustrative - requires a servlet context)
        /*
        HttpServletRequest request = ...; // Get request from servlet context
        HttpServletResponse response = ...; // Get response from servlet context

        CookieStorage storage = new CookieStorage(request, response);

        storage.setItem("name", "John Doe");
        String name = storage.getItem("name");
        System.out.println("Name: " + name);

        storage.removeItem("name");
        String nameAfterRemoval = storage.getItem("name");
        System.out.println("Name after removal: " + nameAfterRemoval);
        */


		// SqlStorage - needs  SQLite JDBC driver (https://github.com/xerial/sqlite-jdbc#download)
        try {
            // Load the SQLite JDBC driver (you'll need to add it to your project)
            Class.forName("org.sqlite.JDBC"); // Or another suitable driver

            // Path to your SQLite database file
            String dbPath = "pq.db"; // Or any path you want

            try (SqlStorage storage = new SqlStorage(dbPath)) { // Try-with-resources for auto-closing the connection
				
                // Example usage
				// Item Creation - before use
                storage.setItem("name", "John Doe");
				
                String name = storage.getItem("name");
                System.out.println("Name: " + name);

                storage.removeItem("name");
                String nameAfterRemoval = storage.getItem("name");
                System.out.println("Name after removal: " + nameAfterRemoval);

            } // The connection is automatically closed here thanks to try-with-resources

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
		
		
		
		// Test Roster Storage
        Roster storage = new Roster("PGRoster");

        // Example Usage (requires Gson library - add it to your project)
        Map<String, Object> newGuy = new HashMap<>();
        Map<String, Object> traits = new HashMap<>();
        traits.put("Name", "Alice");
        newGuy.put("Traits", traits);


        storage.addToRoster(newGuy, () -> System.out.println("Added to roster"));

        storage.loadRoster(roster -> System.out.println("Loaded roster: " + roster));

        storage.loadSheet("Alice", sheet -> System.out.println("Loaded sheet for Alice: " + sheet));


        // Example demonstrating Quota Exceeded.
        Map<String, Object> hugeRoster = new HashMap<>();
        for (int i = 0; i < 1000; i++) { // Simulate a large roster
            hugeRoster.put("Player" + i, newGuy);
        }

        storage.storeRoster(hugeRoster, () -> System.out.println("Attempted to store huge roster"));
		
 
    }
}